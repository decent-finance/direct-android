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

package com.cexdirect.lib

import androidx.databinding.ObservableField
import com.cexdirect.lib.error.LastKnownOrderInfo
import com.cexdirect.lib.order.OrderAmounts

abstract class AmountViewModel : LegalViewModel() {

    val copyEvent = StringLiveEvent()
    val orderId = ObservableField("")
    val orderAmounts = OrderAmounts()

    fun copyOrderId() {
        copyEvent.postValue(orderId.get())
    }

    fun extractAmounts() = LastKnownOrderInfo(
        orderId.get() ?: "",
        orderAmounts.selectedFiatAmount,
        orderAmounts.selectedFiatCurrency,
        orderAmounts.selectedCryptoAmount,
        orderAmounts.selectedCryptoCurrency
    )
}
