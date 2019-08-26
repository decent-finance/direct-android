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

package com.cexdirect.lib.verification.receipt

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.SingleLiveEvent
import com.cexdirect.lib.network.models.PaymentInfo
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.verification.events.CopyEvent

class ReceiptFragmentViewModel(private val messenger: Messenger, dispatcherProvider: CoroutineDispatcherProvider) :
    BaseObservableViewModel(dispatcherProvider) {

    val paymentInfo = ObservableField<PaymentInfo>()
    val txId = ObservableField("")

    val buyMoreEvent = BuyMoreEvent()
    val txIdCopyEvent = CopyEvent()

    fun buyMore() {
        buyMoreEvent.call()
    }

    fun updatePaymentInfo(info: PaymentInfo) {
        paymentInfo.set(info)
        info.txId?.let { txId.set(it) }
    }

    fun copyTxId(txId: String) {
        txIdCopyEvent.postValue(txId)
    }

    fun subscribeToOrderInfo() = messenger.subscribeToOrderInfo()

    class Factory(private val messenger: Messenger, private val dispatcherProvider: CoroutineDispatcherProvider) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ReceiptFragmentViewModel(messenger, dispatcherProvider) as T
    }
}

class BuyMoreEvent : SingleLiveEvent<Void>()
