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

import android.view.View
import androidx.databinding.*
import com.cexdirect.lib.BR
import com.cexdirect.lib.R
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.network.models.Images
import com.cexdirect.lib.util.FieldStatus

class UserDocs(private val stringProvider: StringProvider) : BaseObservable() {

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
    var selfieSizeValid = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieSizeValid)
        }

    @get:Bindable
    var documentFrontStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentFrontStatus)
        }

    @get:Bindable
    var documentFrontSizeValid = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentFrontSizeValid)
        }

    @get:Bindable
    var documentBackStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentBackStatus)
        }

    @get:Bindable
    var documentBackSizeValid = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentBackSizeValid)
        }

    @get:Bindable
    var documentFrontErrorText = stringProvider.provideString(R.string.cexd_no_front)
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentFrontErrorText)
        }

    var imagesBase64 = ObservableArrayMap<String, String>()

    @get:Bindable
    var selfieBase64 = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.selfieBase64)
        }

    lateinit var currentPhotoType: PhotoType
    lateinit var uploadAction: () -> Unit

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.documentType -> {
                        imagesBase64.clear()
                        requiredImagesAmount = when (documentType) {
                            DocumentType.PASSPORT -> 1
                            DocumentType.DRIVER_LICENCE, DocumentType.ID_CARD -> 2
                            else -> error("Illegal document type $documentType")
                        }
                        documentFrontErrorText = when (documentType) {
                            DocumentType.PASSPORT ->
                                stringProvider.provideString(R.string.cexd_no_photo)
                            DocumentType.DRIVER_LICENCE, DocumentType.ID_CARD ->
                                stringProvider.provideString(R.string.cexd_no_front)
                            else -> error("Illegal document type $documentType")
                        }
                    }
                    BR.shouldSendPhoto -> {
                        if (shouldSendPhoto) {
                            uploadAction.invoke()
                        }
                    }
                    BR.selfieBase64 -> uploadAction.invoke()
                    BR.selectedDocType -> {
                        when (selectedDocType) {
                            R.id.fiIdCard -> {
                                documentTypeSelected = true
                                documentType = DocumentType.ID_CARD
                                documentImage = R.drawable.ic_pic_id_front
                                documentImageBack = R.drawable.ic_pic_id_back
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_id)
                            }
                            R.id.fiPassport -> {
                                documentTypeSelected = true
                                documentType = DocumentType.PASSPORT
                                documentImage = R.drawable.ic_pic_passport
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_passport)
                            }
                            R.id.fiDriversLicence -> {
                                documentTypeSelected = true
                                documentType = DocumentType.DRIVER_LICENCE
                                documentImage = R.drawable.ic_pic_license_front
                                documentImageBack = R.drawable.ic_pic_license_back
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_licence)
                            }
                            else -> error("Illegal doc type")
                        }
                        documentFrontStatus = FieldStatus.EMPTY
                        documentFrontSizeValid = true
                        documentBackStatus = FieldStatus.EMPTY
                        documentBackSizeValid = true
                    }
                    BR.documentTypeSelected -> documentSelectionStatus = FieldStatus.VALID
                }
            }
        })
        imagesBase64.addOnMapChangedCallback(object :
            ObservableMap.OnMapChangedCallback<ObservableMap<String, String>, String, String>() {
            override fun onMapChanged(sender: ObservableMap<String, String>, key: String?) {
                if (sender.keys.size == requiredImagesAmount) {
                    shouldSendPhoto = true
                } else if (sender.isEmpty() || sender.keys.size < requiredImagesAmount) {
                    shouldSendPhoto = false
                }
            }
        })
    }

    fun setImage(imageBase64: String) {
        when (currentPhotoType) {
            PhotoType.SELFIE -> {
                selfieSizeValid = true
                selfieStatus = FieldStatus.VALID
                selfieBase64 = imageBase64
            }
            PhotoType.ID -> {
                selfieSizeValid = true
                documentFrontStatus = FieldStatus.VALID
                imagesBase64["front"] = imageBase64
            }
            PhotoType.ID_BACK -> {
                selfieSizeValid = true
                documentBackStatus = FieldStatus.VALID
                imagesBase64["back"] = imageBase64
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
                selfieStatus = FieldStatus.INVALID
            }
        }

        if (requiredImages.isIdentityDocumentsRequired) {
            if (documentFrontStatus == FieldStatus.EMPTY) {
                documentFrontStatus = FieldStatus.INVALID
            }
            if (requiredImagesAmount == 2 /* id card, licence */ && documentBackStatus == FieldStatus.EMPTY) {
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
                documentFrontSizeValid = false
                documentFrontStatus = FieldStatus.INVALID
            }
            PhotoType.ID_BACK -> {
                documentBackSizeValid = false
                documentBackStatus = FieldStatus.INVALID
            }
            PhotoType.SELFIE -> {
                selfieSizeValid = false
                selfieStatus = FieldStatus.INVALID
            }
        }
    }

    fun getDocumentPhotosArray() =
        Array(requiredImagesAmount) {
            when (it) {
                0 -> imagesBase64["front"]!!
                1 -> imagesBase64["back"]!!
                else -> "Illegal index $it"
            }
        }
}
