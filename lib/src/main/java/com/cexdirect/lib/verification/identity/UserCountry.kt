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
import com.cexdirect.lib.network.models.emptyCountry

class UserCountry : BaseObservable() {

    @get:Bindable
    var selectedCountry = emptyCountry()
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedCountry)
        }

    @get:Bindable
    var selectedState = emptyCountry()
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedState)
        }

    @get:Bindable
    var shouldShowState = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.shouldShowState)
        }

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                when (propertyId) {
                    BR.selectedCountry -> {
                        shouldShowState =
                            selectedCountry.states != null && selectedCountry.states!!.isNotEmpty()
                    }
                }
            }
        })
    }

    fun isCountrySelected() =
        if (shouldShowState) {
            selectedCountry.name.isNotBlank() && selectedState.name.isNotBlank()
        } else {
            selectedCountry.name.isNotBlank()
        }

}
