/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.cexdirect.lib.BR
import com.cexdirect.lib.util.FieldStatus

class UserWallet : BaseObservable() {

    @get:Bindable
    var address = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.address)
        }

    @get:Bindable
    var tag = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.tag)
        }

    @get:Bindable
    var walletStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.walletStatus)
        }

    @get:Bindable
    var needsTag = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.needsTag)
        }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.address) {
                    walletStatus = if (address.isEmpty()) {
                        FieldStatus.EMPTY
                    } else {
                        FieldStatus.VALID
                    }
                }
            }
        })
    }

    fun forceValidate() {
        if (walletStatus == FieldStatus.EMPTY) {
            walletStatus = FieldStatus.INVALID
        }
    }

    fun isValid() =
        if (needsTag) {
            walletStatus == FieldStatus.VALID && tag.isNotBlank()
        } else {
            walletStatus == FieldStatus.VALID
        }
}
