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

package com.cexdirect.lib.verification.identity

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.cexdirect.lib.BR
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.checkSsnStatus

class UserSsn : BaseObservable() {

    @get:Bindable
    var ssn = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.ssn)
        }

    @get:Bindable
    var ssnStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.ssnStatus)
        }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.ssn) {
                    ssnStatus = checkSsnStatus(ssn)
                }
            }
        })
    }

    fun forceValidate() {
        if (ssnStatus == FieldStatus.EMPTY) {
            ssnStatus = FieldStatus.INVALID
        }
    }

    fun isSsnValid() = ssnStatus == FieldStatus.VALID

    fun getFormattedValue() = ssn.replace("-", "")
}
