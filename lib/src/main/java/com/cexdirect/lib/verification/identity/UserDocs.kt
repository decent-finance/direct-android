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
    var documentImage = R.drawable.ic_pic_id_card
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentImage)
        }

    @get:Bindable
    var documentImageBack = R.drawable.ic_pic_id_card
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
    var requiredPhotos = 1
        set(value) {
            field = value
            notifyPropertyChanged(BR.requiredPhotos)
        }

    @get:Bindable
    var selectedDocType = View.NO_ID
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedDocType)
        }

    var imagesBase64 = ObservableArrayMap<String, String>()
    var selfieBase64 = ""

    lateinit var currentPhotoType: PhotoType
    lateinit var uploadAction: () -> Unit

    fun setFrontPhoto(img: String) {
        imagesBase64["front"] = img
    }

    fun setBackPhoto(img: String) {
        imagesBase64["back"] = img
    }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.documentType -> {
                        imagesBase64.clear()
                        requiredPhotos = when (documentType) {
                            DocumentType.PASSPORT -> 1
                            DocumentType.DRIVER_LICENCE, DocumentType.ID_CARD -> 2
                            else -> error("Illegal document type $documentType")
                        }
                    }
                    BR.shouldSendPhoto -> {
                        if (shouldSendPhoto) {
                            uploadAction.invoke()
                        }
                    }
                    BR.selectedDocType -> {
                        when (selectedDocType) {
                            R.id.fiIdCard -> {
                                documentTypeSelected = true
                                documentType = DocumentType.ID_CARD
                                documentImage = R.drawable.ic_pic_id_card
                                documentImageBack = R.drawable.ic_pic_id_card
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
                                documentImage = R.drawable.ic_pic_driver_license
                                documentImageBack = R.drawable.ic_pic_driver_license
                                documentTypeText =
                                    stringProvider.provideString(R.string.cexd_take_pic_licence)
                            }
                            else -> {
                                error("Illegal doc type")
                            }
                        }
                    }
                }
            }
        })
        imagesBase64.addOnMapChangedCallback(object :
            ObservableMap.OnMapChangedCallback<ObservableMap<String, String>, String, String>() {
            override fun onMapChanged(sender: ObservableMap<String, String>, key: String?) {
                if (sender.keys.size == requiredPhotos) {
                    shouldSendPhoto = true
                } else if (sender.isEmpty() || sender.keys.size < requiredPhotos) {
                    shouldSendPhoto = false
                }
            }
        })
    }
}
