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

package com.cexdirect.lib.verification

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.cexdirect.lib.BR

class OrderAmounts : BaseObservable() {

    @get:Bindable
    var selectedFiatCurrency = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedFiatCurrency)
        }

    @get:Bindable
    var selectedFiatAmount = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedFiatAmount)
        }

    @get:Bindable
    var selectedCryptoCurrency = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedCryptoCurrency)
        }

    @get:Bindable
    var selectedCryptoAmount = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.selectedCryptoAmount)
        }
}
