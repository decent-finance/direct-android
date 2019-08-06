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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.IdentityFragmentFactory
import com.cexdirect.lib._network.Failure
import com.cexdirect.lib._network.Loading
import com.cexdirect.lib._network.Resource
import com.cexdirect.lib._network.Success
import com.cexdirect.lib._network.models.OrderStatus
import com.cexdirect.lib._util.EmailStatus
import com.cexdirect.lib.databinding.FragmentIdentityBinding
import com.cexdirect.lib.error.locationNotSupported
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.error.verificationError
import com.cexdirect.lib.verification.BaseVerificationFragment
import com.cexdirect.lib.verification.SourceClickEvent
import com.cexdirect.lib.verification.StickyViewEvent
import com.cexdirect.lib.verification.identity._util.CreditCardFormatTextWatcher
import com.cexdirect.lib.verification.identity._util.DateWatcher
import com.cexdirect.lib.verification.identity._util.convertAndSet
import com.cexdirect.lib.views.CollapsibleLayout
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import com.mukesh.countrypicker.CountryPicker
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@RuntimePermissions
class IdentityFragment : BaseVerificationFragment() {

    @field:[Inject IdentityFragmentFactory]
    lateinit var fragmentModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var event: SourceClickEvent

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    @Inject
    lateinit var currentOrderStatus: AtomicReference<OrderStatus>

    private val fragmentModel by fragmentViewModelProvider<IdentityFragmentViewModel> { fragmentModelFactory }

    private lateinit var binding: FragmentIdentityBinding

    private var currentPhotoPath: String = ""

    private val operationObserver = Observer<Resource<*>> { resource ->
        when (resource) {
            is Loading -> showLoader()
            is Success -> hideLoader()
            is Failure -> {
                hideLoader()
                showPurchaseErrorScreen(resource)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        FragmentIdentityBinding.inflate(inflater, container, false).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fiInputCardNumber.apply {
                addTextChangedListener(CreditCardFormatTextWatcher(this))
                filters = arrayOf(InputFilter.LengthFilter(16))
            }
            fiInputCvv.apply {
                filters = arrayOf(InputFilter.LengthFilter(4))
            }
            fiInputDate.apply {
                filters = arrayOf(InputFilter.LengthFilter(5))
                addTextChangedListener(DateWatcher(this))
            }
            fiContent.layoutTransition = LayoutTransition().apply {
                disableTransitionType(LayoutTransition.DISAPPEARING)
                enableTransitionType(LayoutTransition.CHANGING)
            }
        }

        event.observe(this, Observer {
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (it) {
                SourceClickType.PHOTO -> choosePhotoFromGalleryWithPermissionCheck()
                SourceClickType.CAMERA -> takePhotoWithPermissionCheck()
                SourceClickType.CANCEL -> {
                    // ignore. event is handled within dialog
                }
            }
        })

        fragmentModel.apply {
            walletCurrency = model.selectedCryptoCurrency.get()!!
            uploadImage.observe(this@IdentityFragment, operationObserver)
            paymentData.observe(this@IdentityFragment, operationObserver)
            updatePaymentData.observe(this@IdentityFragment, operationObserver)
            processingResult.observe(this@IdentityFragment, operationObserver)
            createOrder.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> {
                        hideLoader()
                        if (it.code == COUNTRY_NOT_SUPPORTED) {
                            context!!.locationNotSupported()
                            finish()
                        } else {
                            showPurchaseErrorScreen(it)
                        }
                    }
                    is Success -> {
                        it.data!!.orderId.let { model.updateOrderId(it) }
                        fragmentModel.setPaymentBase()
                        subscribeToOrderInfoUpdates()
                        hideLoader()
                    }
                    is Loading -> showLoader()
                }
            })
            nextClickEvent.observe(this@IdentityFragment, Observer {
                when (fragmentModel.verificationStep.get()) {
                    VerificationStep.LOCATION_EMAIL -> {
                        if (fragmentModel.emailStatus.get() == EmailStatus.EMPTY) {
                            toast("Please, enter your e-mail address")
                            return@Observer
                        } else if (fragmentModel.emailStatus.get() == EmailStatus.INVALID) {
                            toast("Please, enter valid e-mail address")
                            return@Observer
                        }
                        if (!fragmentModel.isCountrySelected()) {
                            toast("Please, fill in country information")
                            return@Observer
                        }
                        Direct.userEmail = fragmentModel.userEmail.get()!!
                        fragmentModel.createOrder.execute()
                    }
                    VerificationStep.PAYMENT_BASE -> {
                        if (fragmentModel.canSendPaymentData()) {
                            if (!fragmentModel.termsAccepted.get()) {
                                toast("Please, accept our Terms of Use")
                                return@Observer
                            }
                            if (currentOrderStatus.get() == OrderStatus.INCOMPLETE) {
                                fragmentModel.uploadPaymentData()
                            } else if (currentOrderStatus.get() == OrderStatus.IVS_READY) {
                                fragmentModel.startVerificationChain()
                            }
                        } else {
                            toast("Please, fill in payment information")
                        }
                    }
                    VerificationStep.PAYMENT_EXTRA -> {
                        if (fragmentModel.extrasValid()) {
                            fragmentModel.updatePaymentData()
                        } else {
                            toast("Please, fill in all fields")
                        }
                    }
                }
            })
            orderInfo.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> showPurchaseErrorScreen(it)
                    is Success -> fragmentModel.setRequiredImages(it.data!!.basic.images)
                }
            })
            chooseCountryEvent.observe(this@IdentityFragment, Observer {
                CountryPicker.Builder()
                    .with(context!!)
                    .listener {
                        fragmentModel.userCountry.set(it)
                    }.style(R.style.Direct_CountryPickerStyle)
                    .build()
                    .showBottomSheet(activity as AppCompatActivity)
            })
            uploadPhotoEvent.observe(this@IdentityFragment, Observer {
                PhotoSourceDialog().show(fragmentManager, "choose")
            })
            verificationResult.observe(this@IdentityFragment, Observer {
                when (it) {
                    is Failure -> {
                        if (it.message == "Error while executing 'Validate wallet address for crypto currency'") {
                            fragmentModel.isWalletValid.set(false)
                            hideLoader()
                            toast("Invalid wallet address")
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

        fragmentModel.subscribeToOrderInfo().observe(this, Observer {
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
                            fragmentModel.sendToVerification()
                        }
                    }
                    OrderStatus.PSS_WAITDATA -> {
                        if (currentOrderStatus.get() != OrderStatus.PSS_WAITDATA) {
                            currentOrderStatus.set(OrderStatus.PSS_WAITDATA)
                            fragmentModel.apply {
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
                            fragmentModel.apply {
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
                            fragmentModel.unsubscribeFromOrderInfo()
                            requestNextStep()
                            currentOrderStatus.set(OrderStatus.INCOMPLETE)
                        }
                    }
                    OrderStatus.WAITING_FOR_CONFIRMATION -> {
                        if (currentOrderStatus.get() != OrderStatus.WAITING_FOR_CONFIRMATION) {
                            currentOrderStatus.set(OrderStatus.WAITING_FOR_CONFIRMATION)
                            fragmentModel.unsubscribeFromOrderInfo()
                            requestNextStep()
                            currentOrderStatus.set(OrderStatus.INCOMPLETE)
                        }
                    }
                    OrderStatus.COMPLETE -> {
                        if (currentOrderStatus.get() != OrderStatus.COMPLETE) {
                            currentOrderStatus.set(OrderStatus.COMPLETE)
                            fragmentModel.unsubscribeFromOrderInfo()
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

    private fun showPurchaseErrorScreen(resource: Failure<*>) {
        context!!.purchaseFailed(resource.message)
        finish()
    }

    private fun requestNextStep() {
        hideLoader()
        model.nextStep()
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
            val targetView = when (fragmentModel.currentPhotoType) {
                PhotoType.ID -> binding.fiDocument
                PhotoType.ID_BACK -> binding.fiDocumentBack
                PhotoType.SELFIE -> binding.fiSelfie
                else -> error("Illegal state")
            }

            when (requestCode) {
                RQ_CHOOSE_PIC -> {
                    data?.data?.let {
                        convertAndSet(context!!, it) { setImage(it) }

                        Glide.with(this)
                            .load(it)
                            .thumbnail(0.25f)
                            .into(targetView)
                    }
                }
                RQ_TAKE_PHOTO -> {
                    convertAndSet(currentPhotoPath) { setImage(it) }

                    Glide.with(this)
                        .load(File(currentPhotoPath))
                        .thumbnail(0.25f)
                        .into(targetView)
                }
            }
        }
    }

    private fun setImage(imageBase64: String) {
        when (fragmentModel.currentPhotoType) {
            PhotoType.SELFIE -> fragmentModel.setSelfie(imageBase64)
            PhotoType.ID -> fragmentModel.documentPhotos.setFrontPhoto(imageBase64)
            PhotoType.ID_BACK -> fragmentModel.documentPhotos.setBackPhoto(imageBase64)
        }
    }
}

const val RQ_TAKE_PHOTO = 1000
const val RQ_CHOOSE_PIC = 1001
const val COUNTRY_NOT_SUPPORTED = 475
