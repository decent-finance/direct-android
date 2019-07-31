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

import androidx.databinding.*
import com.cexdirect.lib.BR

class DocumentPhotos : BaseObservable() {

    @get:Bindable
    var documentType: DocumentType = DocumentType.PASSPORT
        set(value) {
            field = value
            notifyPropertyChanged(BR.documentType)
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

    var imagesBase64 = ObservableArrayMap<String, String>()

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
