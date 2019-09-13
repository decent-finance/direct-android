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

package com.cexdirect.lib.verification.confirmation

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.cexdirect.lib.BR
import com.cexdirect.lib.BuildConfig
import com.cexdirect.lib.Direct
import com.cexdirect.lib.network.models._3DsExtras

class _3dsData : BaseObservable() {

    @get:Bindable
    var _3dsUrl: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR._3dsUrl)
        }

    @get:Bindable
    var _3dsExtras: _3DsExtras? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR._3dsExtras)
        }

    @get:Bindable
    var txId: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.txId)
        }

    var termUrl: String = ""

    fun hasData() = !_3dsUrl.isNullOrBlank() && _3dsExtras != null

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.txId) {
                    termUrl =
                        "${BuildConfig.REST_URL}api/v1/orders/3ds-check/${Direct.pendingOrderId}/tx/$txId"
                }
            }
        })
    }
}
