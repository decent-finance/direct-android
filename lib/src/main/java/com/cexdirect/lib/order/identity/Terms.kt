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

class Terms : BaseObservable() {

    @get:Bindable
    var termsAccepted = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.termsAccepted)
        }

    @get:Bindable
    var termsStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.termsStatus)
        }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.termsAccepted) {
                    termsStatus = if (termsAccepted) {
                        FieldStatus.VALID
                    } else {
                        FieldStatus.EMPTY
                    }
                }
            }
        })
    }

    fun forceValidate() {
        if (termsStatus == FieldStatus.EMPTY) {
            termsStatus = FieldStatus.INVALID
        }
    }

    fun accepted() = termsStatus == FieldStatus.VALID
}
