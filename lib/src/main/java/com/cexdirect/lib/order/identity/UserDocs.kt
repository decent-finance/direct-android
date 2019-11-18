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

import android.view.View
import androidx.databinding.*
import com.cexdirect.lib.BR
import com.cexdirect.lib.R
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.order.identity.img.ImageReference
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.encodeToString
import org.bouncycastle.util.encoders.Base64

class UserDocs(
    private val stringProvider: StringProvider,
    private val dh: DH
) : BaseObservable() {

    @get:Bindable
    var documentType: DocumentType = DocumentType.PASSPORT
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentType)
        }

    @get:Bindable
    var documentTypeSelected = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentTypeSelected)
        }

    @get:Bindable
    var documentSelectionStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentSelectionStatus)
        }

    @get:Bindable
    var documentImage = R.drawable.ic_pic_id_front
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentImage)
        }

    @get:Bindable
    var documentImageBack = R.drawable.ic_pic_id_back
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentImageBack)
        }

    @get:Bindable
    var documentTypeText = stringProvider.provideString(R.string.cexd_take_pic_id)
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentTypeText)
        }

    @get:Bindable
    var selfieLabel = stringProvider.provideString(R.string.cexd_selfie_id)
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieLabel)
        }

    @get:Bindable
    var shouldSendPhoto = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.shouldSendPhoto)
        }

    @get:Bindable
    var requiredImagesAmount = 1
        set(value) {
            field = value
            notifyPropertyChanged(BR.requiredImagesAmount)
        }

    @get:Bindable
    var selectedDocType = View.NO_ID
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedDocType)
        }

    @get:Bindable
    var requiredImages = Images(false, false)
        set(value) {
            field = value
            notifyPropertyChanged(BR.requiredImages)
        }

    @get:Bindable
    var selfieStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieStatus)
        }

    @get:Bindable
    var documentFrontStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentFrontStatus)
        }

    @get:Bindable
    var documentBackStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentBackStatus)
        }

    @get:Bindable
    var documentFrontErrorText = stringProvider.provideString(R.string.cexd_no_front)
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentFrontErrorText)
        }

    @get:Bindable
    var documentBackErrorText = stringProvider.provideString(R.string.cexd_no_front)
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentBackErrorText)
        }

    @get:Bindable
    var selfieErrorText = stringProvider.provideString(R.string.cexd_no_front)
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieErrorText)
        }

    var imageRefs = ObservableArrayMap<String, ImageReference>()

    @get:Bindable
    var selfieRef: ImageReference? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieRef)
        }

    lateinit var currentPhotoType: PhotoType
    lateinit var uploadAction: (type: PhotoType, amount: Int) -> Unit

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.documentType -> {
                        imageRefs.clear()
                        requiredImagesAmount = when (documentType) {
                            DocumentType.PASSPORT -> 1
                            DocumentType.DRIVER_LICENCE, DocumentType.ID_CARD -> 2
                            else -> error("Illegal document type $documentType")
                        }
                    }
                    BR.shouldSendPhoto -> {
                        if (shouldSendPhoto) {
                            uploadAction.invoke(currentPhotoType, requiredImagesAmount)
                        }
                    }
                    BR.selfieRef -> uploadAction.invoke(currentPhotoType, requiredImagesAmount)
                    BR.selectedDocType -> {
                        when (selectedDocType) {
                            R.id.fiIdCard -> {
                                documentTypeSelected = true
                                documentType = DocumentType.ID_CARD
                                documentImage = R.drawable.ic_pic_id_front
                                documentImageBack = R.drawable.ic_pic_id_back
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_id)
                                selfieLabel = stringProvider.provideString(R.string.cexd_selfie_id)
                            }
                            R.id.fiPassport -> {
                                documentTypeSelected = true
                                documentType = DocumentType.PASSPORT
                                documentImage = R.drawable.ic_pic_passport
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_passport)
                                selfieLabel = stringProvider.provideString(R.string.cexd_selfie_passport)
                            }
                            R.id.fiDriversLicence -> {
                                documentTypeSelected = true
                                documentType = DocumentType.DRIVER_LICENCE
                                documentImage = R.drawable.ic_pic_license_front
                                documentImageBack = R.drawable.ic_pic_license_back
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_licence)
                                selfieLabel = stringProvider.provideString(R.string.cexd_selfie_dl)
                            }
                            else -> error("Illegal doc type")
                        }
                        documentFrontStatus = FieldStatus.EMPTY
                        documentBackStatus = FieldStatus.EMPTY
                    }
                    BR.documentTypeSelected -> documentSelectionStatus = FieldStatus.VALID
                }
            }
        })
        imageRefs.addOnMapChangedCallback(object :
            ObservableMap.OnMapChangedCallback<ObservableMap<String, ImageReference>, String, ImageReference>() {
            override fun onMapChanged(sender: ObservableMap<String, ImageReference>, key: String?) {
                if (sender.keys.size == requiredImagesAmount) {
                    shouldSendPhoto = true
                } else if (sender.isEmpty() || sender.keys.size < requiredImagesAmount) {
                    shouldSendPhoto = false
                }
            }
        })
    }

    fun setImage(imgRef: ImageReference) {
        when (currentPhotoType) {
            PhotoType.SELFIE -> {
                selfieStatus = FieldStatus.VALID
                selfieRef = imgRef
            }
            PhotoType.ID -> {
                documentFrontStatus = FieldStatus.VALID
                imageRefs["front"] = imgRef
            }
            PhotoType.ID_BACK -> {
                documentBackStatus = FieldStatus.VALID
                imageRefs["back"] = imgRef
            }
        }
    }

    fun forceValidate() {
        if (!documentTypeSelected) {
            documentSelectionStatus = FieldStatus.INVALID
            return
        }

        if (requiredImages.isSelfieRequired) {
            if (selfieStatus == FieldStatus.EMPTY) {
                selfieErrorText = stringProvider.provideString(R.string.cexd_no_selfie)
                selfieStatus = FieldStatus.INVALID
            }
        }

        if (requiredImages.isIdentityDocumentsRequired) {
            if (documentFrontStatus == FieldStatus.EMPTY) {
                val text = when (documentType) {
                    DocumentType.PASSPORT ->
                        stringProvider.provideString(R.string.cexd_no_photo)
                    DocumentType.DRIVER_LICENCE, DocumentType.ID_CARD ->
                        stringProvider.provideString(R.string.cexd_no_front)
                    else -> error("Illegal type")
                }
                documentFrontErrorText = text
                documentFrontStatus = FieldStatus.INVALID
            }
            if (requiredImagesAmount == 2 /* id card, licence */ && documentBackStatus == FieldStatus.EMPTY) {
                documentBackErrorText = stringProvider.provideString(R.string.cexd_no_back)
                documentBackStatus = FieldStatus.INVALID
            }
        }
    }

    fun isValid() = documentTypeSelected && docsValid() && selfieValid()

    private fun docsValid() =
        if (requiredImages.isIdentityDocumentsRequired) {
            when (requiredImagesAmount) {
                2 -> documentFrontStatus == FieldStatus.VALID && documentBackStatus == FieldStatus.VALID
                1 -> documentFrontStatus == FieldStatus.VALID
                else -> error("Invalid amount: $requiredImagesAmount")
            }
        } else {
            true
        }

    private fun selfieValid() =
        if (requiredImages.isSelfieRequired) {
            selfieStatus == FieldStatus.VALID
        } else {
            true
        }

    fun setImageSizeInvalid() {
        when (currentPhotoType) {
            PhotoType.ID -> {
                documentFrontErrorText = stringProvider.provideString(R.string.cexd_file_too_big)
                documentFrontStatus = FieldStatus.INVALID
            }
            PhotoType.ID_BACK -> {
                documentBackErrorText = stringProvider.provideString(R.string.cexd_file_too_big)
                documentBackStatus = FieldStatus.INVALID
            }
            PhotoType.SELFIE -> {
                selfieErrorText = stringProvider.provideString(R.string.cexd_file_too_big)
                selfieStatus = FieldStatus.INVALID
            }
        }
    }

    fun setUnsupportedFormat() {
        when (currentPhotoType) {
            PhotoType.ID -> {
                documentFrontErrorText = stringProvider.provideString(R.string.cexd_wrong_format)
                documentFrontStatus = FieldStatus.INVALID
            }
            PhotoType.ID_BACK -> {
                documentBackErrorText = stringProvider.provideString(R.string.cexd_wrong_format)
                documentBackStatus = FieldStatus.INVALID
            }
            PhotoType.SELFIE -> {
                selfieErrorText = stringProvider.provideString(R.string.cexd_wrong_format)
                selfieStatus = FieldStatus.INVALID
            }
        }
    }

    fun getDocumentPhotosArray() =
        Array(requiredImagesAmount) {
            when (it) {
                0 -> Base64Image(0, imageRefs.getValue("front").encodeToBase64())
                1 -> Base64Image(1, imageRefs.getValue("back").encodeToBase64())
                else -> error("Illegal index $it")
            }
        }

    fun getSelfieArray() = arrayOf(
        Base64Image(0, selfieRef?.encodeToBase64() ?: error("Selfie reference not set"))
    )

    fun getPublicKey() = dh.publicKey.y.toByteArray().encodeToString()

    fun generateSelfieData(data: PublicKeyResponseData): EncryptedImageData {
        val vector = dh.byteGenerator(GENERATOR_BYTES)

        val content = EncryptedImage(
            0,
            Base64.toBase64String(vector),
            data.secretId,
            encryptData(
                selfieRef?.getBytes() ?: error("Selfie reference not set"),
                data.publicKey,
                vector
            )
        )

        return EncryptedImageData(
            documentType = DocumentType.SELFIE.value,
            encryptedContent = listOf(content)
        )
    }

    private fun encryptData(data: ByteArray, pubKey: String, vector: ByteArray) =
        dh.encrypt(
            data,
            android.util.Base64.decode(pubKey, android.util.Base64.NO_WRAP),
            vector
        )!!

    fun generateDocumentData(keys: List<PublicKeyResponseData>): EncryptedImageData {
        val vector = dh.byteGenerator(GENERATOR_BYTES)

        val content = Array(requiredImagesAmount) {
            when (it) {
                0 -> EncryptedImage(
                    0,
                    Base64.toBase64String(vector),
                    keys[0].secretId,
                    encryptData(imageRefs.getValue("front").getBytes(), keys[0].publicKey, vector)
                )
                1 -> EncryptedImage(
                    1,
                    Base64.toBase64String(vector),
                    keys[1].secretId,
                    encryptData(imageRefs.getValue("back").getBytes(), keys[1].publicKey, vector)
                )
                else -> error("Illegal index $it")
            }
        }
        return EncryptedImageData(
            documentType = documentType.value,
            encryptedContent = content.toList()
        )
    }

    companion object {
        private const val GENERATOR_BYTES = 16
    }
}
