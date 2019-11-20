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

package com.cexdirect.lib.order.identity

import android.Manifest
import android.animation.LayoutTransition
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.cexdirect.lib.Direct
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.FragmentIdentityBinding
import com.cexdirect.lib.error.locationNotSupported
import com.cexdirect.lib.error.paymentRejected
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.models.OrderInfoData
import com.cexdirect.lib.order.BaseOrderFragment
import com.cexdirect.lib.order.events.SourceClickEvent
import com.cexdirect.lib.order.identity.country.CountryPickerDialog
import com.cexdirect.lib.order.identity.country.StatePickerDialog
import com.cexdirect.lib.order.identity.img.CameraImageReference
import com.cexdirect.lib.order.identity.img.GalleryImageReference
import com.cexdirect.lib.order.identity.util.*
import com.cexdirect.lib.order.scanner.QrScannerActivity
import com.cexdirect.lib.util.FieldStatus
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OpenForTesting
@RuntimePermissions
class IdentityFragment : BaseOrderFragment() {

    @Inject
    lateinit var event: SourceClickEvent

    private lateinit var binding: FragmentIdentityBinding

    private var currentPhotoPath: String = ""

    private val paymentDataObserver = requestObserver<OrderInfoData>(
        onOk = { /* Order status will be updated via WS connection */ },
        onFail = { purchaseFailed(it.message, model.extractAmounts()) },
        final = { /* do not hide loader here */ }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentIdentityBinding.inflate(inflater, container, false).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        setupInputs()

        event.observe(viewLifecycleOwner, Observer {
            when (it!!) {
                SourceClickType.PHOTO -> choosePhotoFromGalleryWithPermissionCheck()
                SourceClickType.CAMERA -> takePhotoWithPermissionCheck()
                SourceClickType.CANCEL -> {
                    // ignore. event is handled within dialog
                }
            }
        })

        model.apply {
            chooseCountryEvent.observe(viewLifecycleOwner, Observer {
                CountryPickerDialog().show(requireFragmentManager(), "country")
            })
            chooseStateEvent.observe(viewLifecycleOwner, Observer {
                StatePickerDialog().show(requireFragmentManager(), "state")
            })
            uploadPhoto.observe(viewLifecycleOwner, Observer {
                PhotoSourceDialog().show(requireFragmentManager(), "choose")
            })
            cvvInfoEvent.observe(viewLifecycleOwner, Observer {
                CvvInfoDialog().show(requireFragmentManager(), "cvv")
            })
            scanQrClick.observe(viewLifecycleOwner, Observer {
                scanQrCodeWithPermissionCheck()
            })
            nextClick.observe(viewLifecycleOwner, Observer { handleNextClick() })
            sendBasePaymentDataRequest.observe(viewLifecycleOwner, paymentDataObserver)
            sendExtraPaymentDataRequest.observe(viewLifecycleOwner, paymentDataObserver)
            sendToProcessingRequest.observe(viewLifecycleOwner, requestObserver(onOk = {}, final = {}))
            newOrderInfoRequest.observe(viewLifecycleOwner, requestObserver(
                onOk = {
                    setRequiredImages(it!!.basic.images)
                    setPaymentBase()
                    subscribeToOrderInfoUpdates()
                },
                onFail = {
                    if (it.code == COUNTRY_NOT_SUPPORTED) {
                        locationNotSupported(
                            extractAmounts(),
                            userCountry.selectedCountry.code,
                            userEmail.email
                        )
                    } else {
                        purchaseFailed(it.message, extractAmounts())
                    }
                }
            ))
            uploadPhotoRequest.observe(viewLifecycleOwner, requestObserver(
                onOk = { setDocumentStatusToValid() },
                onFail = { purchaseFailed(it.message, extractAmounts()) }
            ))
            sendToVerificationRequest.observe(viewLifecycleOwner, requestObserver(
                onLoading = {
                    hideKeyboard()
                    hideLoader()
                    verificationInProgressEvent.value = true
                },
                onOk = {},
                onFail = {
                    verificationInProgressEvent.value = false
                    if (it.message == "Error while executing 'Validate wallet address for crypto currency'") {
                        userWallet.walletStatus = FieldStatus.INVALID
                    } else {
                        purchaseFailed(it.message, model.extractAmounts())
                    }
                },
                final = {}
            ))
        }.let { binding.model = it }
    }

    private fun setupInputs() {
        binding.apply {
            fiCard.fiInputCardNumber.apply {
                addTextChangedListener(CreditCardFormatTextWatcher(this))
                filters = arrayOf(InputFilter.LengthFilter(MAX_CARD_LENGTH))
            }
            fiCard.fiInputCvv.filters = arrayOf(InputFilter.LengthFilter(MAX_CVV_LENGTH))
            fiCard.fiInputDate.apply {
                filters = arrayOf(InputFilter.LengthFilter(MAX_EXP_DATE_LENGTH))
                addTextChangedListener(DateWatcher(this))
            }
            fiContent.layoutTransition = LayoutTransition().apply {
                disableTransitionType(LayoutTransition.DISAPPEARING)
                enableTransitionType(LayoutTransition.CHANGING)
            }
            fiSsn.psSsn.apply {
                filters = arrayOf(InputFilter.LengthFilter(MAX_SSN_LENGTH))
                addTextChangedListener(SsnWatcher(this))
            }
        }
    }

    private fun subscribeToOrderInfoUpdates() {
        model.subscribeToOrderInfo().observe(viewLifecycleOwner, socketObserver(
            onOk = {
                model.updateOrderStatus(
                    it,
                    {
                        requireContext().paymentRejected(it, model.extractAmounts())
                        finish()
                    },
                    { /*hideLoader()*/  model.verificationInProgressEvent.value = false  },
                    {
                        // FIXME: A workaround (dirty hack) to request scroll after view is laid out
                        binding.fiExtras.peExtrasTitle.postDelayed(
                            { model.requestScrollTo(binding.fiExtras.peExtrasTitle.id) },
                            SCROLL_DELAY
                        )
                    }
                )
            },
            onFail = { purchaseFailed(it.message, model.extractAmounts()) }
        ))
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
                try {
                    createImageFile()
                } catch (ex: IOException) {
                    toast("Cannot take photo. ${ex.message ?: ""}")
                    null
                }?.let {
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

    @NeedsPermission(Manifest.permission.CAMERA)
    fun scanQrCode() {
        startActivityForResult(
            Intent(context, QrScannerActivity::class.java),
            RQ_SCAN_QR
        )
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
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            RQ_CHOOSE_PIC -> {
                try {
                    data?.data?.let { uri ->
                        checkAndSet(
                            context!!,
                            uri,
                            {
                                model.setImage(
                                    GalleryImageReference(
                                        it,
                                        requireContext().contentResolver
                                    )
                                )
                                Glide.with(this)
                                    .load(uri)
                                    .thumbnail(THUMBNAIL_SCALE_FACTOR)
                                    .into(getTargetView())
                            },
                            {
                                when (it) {
                                    FailType.SIZE_INVALID -> model.setImageSizeInvalid()
                                    FailType.UNSUPPORTED_FORMAT -> model.setUnsupportedFormat()
                                }
                            }
                        )
                    }
                } catch (e: FileNotFoundException) {
                    toast(R.string.cexd_file_not_loaded)
                }
            }
            RQ_TAKE_PHOTO -> {
                try {
                    checkAndSet(
                        currentPhotoPath,
                        {
                            model.setImage(CameraImageReference(it))
                            Glide.with(this)
                                .load(File(currentPhotoPath))
                                .thumbnail(THUMBNAIL_SCALE_FACTOR)
                                .into(getTargetView())
                        },
                        { model.setImageSizeInvalid() }
                    )
                } catch (e: FileNotFoundException) {
                    toast(R.string.cexd_file_not_found)
                }
            }
            RQ_SCAN_QR -> model.userWallet.address = data!!.getStringExtra("data")
        }
    }

    private fun getTargetView() =
        when (model.userDocs.currentPhotoType) {
            PhotoType.ID -> binding.fiDocs.fiDocument
            PhotoType.ID_BACK -> binding.fiDocs.fiDocumentBack
            PhotoType.SELFIE -> binding.fiDocs.fiSelfie
        }

    companion object {
        const val MAX_CARD_LENGTH = 16
        const val MAX_CVV_LENGTH = 4
        const val MAX_EXP_DATE_LENGTH = 5
        const val MAX_SSN_LENGTH = 11
        const val THUMBNAIL_SCALE_FACTOR = 0.25f
        const val RQ_TAKE_PHOTO = 1000
        const val RQ_CHOOSE_PIC = 1001
        const val RQ_SCAN_QR = 1002
        const val COUNTRY_NOT_SUPPORTED = 475
        const val SCROLL_DELAY = 1000L
    }
}
