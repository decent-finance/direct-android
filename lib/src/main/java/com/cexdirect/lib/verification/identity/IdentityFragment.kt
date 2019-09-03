/*
 * Copyright 2019 CEX.​IO Ltd (UK)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.cexdirect.lib.verification.identity

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.FragmentIdentityBinding
import com.cexdirect.lib.error.locationNotSupported
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.error.verificationError
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.OrderStatus
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.events.SourceClickEvent
import com.cexdirect.lib.verification.events.StickyViewEvent
import com.cexdirect.lib.verification.identity._util.CreditCardFormatTextWatcher
import com.cexdirect.lib.verification.identity._util.DateWatcher
import com.cexdirect.lib.verification.identity._util.convertAndSet
import com.cexdirect.lib.verification.identity.country.CountryPickerDialog
import com.cexdirect.lib.verification.identity.country.StatePickerDialog
import com.cexdirect.lib.views.CollapsibleLayout
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@RuntimePermissions
class IdentityFragment : BaseVerificationFragment() {

    @Inject
    lateinit var event: SourceClickEvent

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    @Inject
    lateinit var currentOrderStatus: AtomicReference<OrderStatus>

    private lateinit var binding: FragmentIdentityBinding

    private var currentPhotoPath: String = ""

    private val operationObserver = Observer<Resource<*>> { resource ->
        when (resource) {
            is Loading -> showLoader()
            is Success -> hideLoader()
            is Failure -> {
                hideLoader()
                purchaseFailed(resource.message)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = FragmentIdentityBinding.inflate(inflater, container, false).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fiCard.fiInputCardNumber.apply {
                addTextChangedListener(CreditCardFormatTextWatcher(this))
                filters = arrayOf(InputFilter.LengthFilter(MAX_CARD_LENGTH))
            }
            fiCard.fiInputCvv.apply {
                filters = arrayOf(InputFilter.LengthFilter(MAX_CVV_LENGTH))
            }
            fiCard.fiInputDate.apply {
                filters = arrayOf(InputFilter.LengthFilter(MAX_EXP_DATE_LENGTH))
                addTextChangedListener(DateWatcher(this))
            }
            fiContent.layoutTransition = LayoutTransition().apply {
                disableTransitionType(LayoutTransition.DISAPPEARING)
                enableTransitionType(LayoutTransition.CHANGING)
            }
        }

        event.observe(this, Observer {
            when (it!!) {
                SourceClickType.PHOTO -> choosePhotoFromGalleryWithPermissionCheck()
                SourceClickType.CAMERA -> takePhotoWithPermissionCheck()
                SourceClickType.CANCEL -> {
                    // ignore. event is handled within dialog
                }
            }
        })

        model.apply {
            uploadImage.observe(this@IdentityFragment, Observer {
                userDocs.currentPhotoType
            })
            paymentData.observe(this@IdentityFragment, Observer {
                if (it is Failure) operationObserver.onChanged(it)
            })
            updatePaymentData.observe(this@IdentityFragment, Observer {
                if (it is Failure) operationObserver.onChanged(it)
            })
            processingResult.observe(this@IdentityFragment, operationObserver)
            createOrder.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> {
                        hideLoader()
                        if (it.code == COUNTRY_NOT_SUPPORTED) {
                            context!!.locationNotSupported()
                            finish()
                        } else {
                            purchaseFailed(it.message)
                        }
                    }
                    is Success -> {
                        it.data!!.orderId.let { model.updateOrderId(it) }
                        setPaymentBase()
                        subscribeToOrderInfoUpdates()
                        hideLoader()
                    }
                    is Loading -> showLoader()
                }
            })
            nextClickEvent.observe(this@IdentityFragment, Observer {
                when (verificationStep.get()) {
                    VerificationStep.LOCATION_EMAIL -> createOrder()
                    VerificationStep.PAYMENT_BASE -> {
                        if (currentOrderStatus.get() == OrderStatus.INCOMPLETE) {
                            showLoader()
                            uploadPaymentData()
                        } else if (currentOrderStatus.get() == OrderStatus.IVS_READY) {
                            startVerificationChain()
                        }
                    }
                    VerificationStep.PAYMENT_EXTRA -> {
                        showLoader()
                        updatePaymentData()
                    }
                }
            })
            orderInfo.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> purchaseFailed(it.message)
                    is Success -> setRequiredImages(it.data!!.basic.images)
                }
            })
            chooseCountryEvent.observe(this@IdentityFragment, Observer {
                CountryPickerDialog().show(fragmentManager, "country")
            })
            chooseStateEvent.observe(this@IdentityFragment, Observer {
                StatePickerDialog().show(fragmentManager, "state")
            })
            uploadPhotoEvent.observe(this@IdentityFragment, Observer {
                PhotoSourceDialog().show(fragmentManager, "choose")
            })
            verificationResult.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> {
                        if (it.message == "Error while executing 'Validate wallet address for crypto currency'") {
                            userWallet.walletStatus = FieldStatus.INVALID
                            hideLoader()
                        } else {
                            operationObserver.onChanged(it)
                        }
                    }
                    is Success -> hideLoader()
                    is Loading -> showLoader()
                }
            })
            cvvInfoEvent.observe(this@IdentityFragment, Observer {
                CvvInfoDialog().show(fragmentManager, "cvv")
            })
        }.let { binding.model = it }
        stickyViewEvent.postValue(R.id.fiNext)
    }

    private fun subscribeToOrderInfoUpdates() {
        currentOrderStatus.set(OrderStatus.INCOMPLETE)

        model.subscribeToOrderInfo().observe(this, Observer {
            if (it is Success) {
                val data = it.data!!
                when (data.orderStatus) {
                    OrderStatus.REJECTED -> {
                        if (currentOrderStatus.get() != OrderStatus.REJECTED) {
                            currentOrderStatus.set(OrderStatus.REJECTED)
                            context!!.verificationError("Rejected")
                            finish()
                        }
                    }
                    OrderStatus.IVS_READY -> {
                        if (currentOrderStatus.get() != OrderStatus.IVS_READY) {
                            currentOrderStatus.set(OrderStatus.IVS_READY)
                            model.startVerificationChain()
                        }
                    }
                    OrderStatus.PSS_WAITDATA -> {
                        if (currentOrderStatus.get() != OrderStatus.PSS_WAITDATA) {
                            currentOrderStatus.set(OrderStatus.PSS_WAITDATA)
                            hideLoader()
                            model.apply {
                                it.data.additional
                                        .takeIf { it.filter { it.value.req }.isNotEmpty() }
                                        .let {
                                            additionalFields.set(it.orEmpty())
                                            verificationStep.set(VerificationStep.PAYMENT_EXTRA)
                                            paymentBaseContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
                                            paymentExtraContentState.set(CollapsibleLayout.ContentState.EXPANDED)
                                        }
                            }
                        }
                    }
                    OrderStatus.PSS_READY -> {
                        if (currentOrderStatus.get() != OrderStatus.PSS_READY) {
                            currentOrderStatus.set(OrderStatus.PSS_READY)
                            model.apply {
                                processingKey.execute()
                            }
                        }
                    }
                    OrderStatus.PSS_PENDING -> {
                        if (currentOrderStatus.get() != OrderStatus.PSS_PENDING) {
                            currentOrderStatus.set(OrderStatus.PSS_PENDING)
                        }
                    }
                    OrderStatus.PSS_3DS_REQUIRED -> {
                        if (currentOrderStatus.get() != OrderStatus.PSS_3DS_REQUIRED) {
                            currentOrderStatus.set(OrderStatus.PSS_3DS_REQUIRED)
                            model.unsubscribeFromOrderInfo()
                            requestNextStep()
                            currentOrderStatus.set(OrderStatus.INCOMPLETE)
                        }
                    }
                    OrderStatus.WAITING_FOR_CONFIRMATION -> {
                        if (currentOrderStatus.get() != OrderStatus.WAITING_FOR_CONFIRMATION) {
                            currentOrderStatus.set(OrderStatus.WAITING_FOR_CONFIRMATION)
                            model.unsubscribeFromOrderInfo()
                            requestNextStep()
                            currentOrderStatus.set(OrderStatus.INCOMPLETE)
                        }
                    }
                    OrderStatus.COMPLETE -> {
                        if (currentOrderStatus.get() != OrderStatus.COMPLETE) {
                            currentOrderStatus.set(OrderStatus.COMPLETE)
                            model.unsubscribeFromOrderInfo()
                            requestNextStep()
                            currentOrderStatus.set(OrderStatus.INCOMPLETE)
                        }
                    }
                    else -> {
                        Log.d("STATUS", data.orderStatus.name)
                    }
                }
            }
        })
    }

    private fun requestNextStep() {
        hideLoader()
        model.next()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File
                .createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
                .apply { currentPhotoPath = absolutePath }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { takePictureIntent ->
            takePictureIntent.resolveActivity(context!!.packageManager)?.let {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    toast("Cannot take photo. ${ex.message ?: ""}")
                    null
                }
                photoFile?.let {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            context!!,
                            "${Direct.context.packageName}.directfile",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RQ_TAKE_PHOTO)
                }
            }
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun choosePhotoFromGallery() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }.let { chooseIntent ->
            chooseIntent.resolveActivity(context!!.packageManager)?.let {
                startActivityForResult(
                        Intent.createChooser(chooseIntent, "Select Picture"),
                        RQ_CHOOSE_PIC
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val targetView = when (model.userDocs.currentPhotoType) {
                PhotoType.ID -> binding.fiDocs.fiDocument
                PhotoType.ID_BACK -> binding.fiDocs.fiDocumentBack
                PhotoType.SELFIE -> binding.fiDocs.fiSelfie
            }

            when (requestCode) {
                RQ_CHOOSE_PIC -> {
                    data?.data?.let { uri ->
                        convertAndSet(context!!, uri) { model.setImage(it) }

                        Glide.with(this)
                                .load(uri)
                                .thumbnail(THUMBNAIL_SCALE_FACTOR)
                                .into(targetView)
                    }
                }
                RQ_TAKE_PHOTO -> {
                    try {
                        convertAndSet(currentPhotoPath) { model.setImage(it) }

                        Glide.with(this)
                                .load(File(currentPhotoPath))
                                .thumbnail(THUMBNAIL_SCALE_FACTOR)
                                .into(targetView)
                    } catch (e: FileNotFoundException) {
                        toast(R.string.cexd_file_not_found)
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_CARD_LENGTH = 16
        const val MAX_CVV_LENGTH = 4
        const val MAX_EXP_DATE_LENGTH = 5
        const val THUMBNAIL_SCALE_FACTOR = 0.25f
        const val RQ_TAKE_PHOTO = 1000
        const val RQ_CHOOSE_PIC = 1001
        const val COUNTRY_NOT_SUPPORTED = 475
    }
}
