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

package com.cexdirect.lib.verification

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.Direct
import com.cexdirect.lib.LegalViewModel
import com.cexdirect.lib.SingleLiveEvent

@Suppress("MagicNumber")
class VerificationActivityViewModel(dispatcherProvider: CoroutineDispatcherProvider) :
    LegalViewModel(dispatcherProvider) {

    val selectedFiatCurrency = ObservableField("USD")
    val selectedFiatAmount = ObservableField("")
    val selectedCryptoCurrency = ObservableField("BTC")
    val selectedCryptoAmount = ObservableField("")

    val orderId = ObservableField("")

    val currentStep = ObservableInt(1)

    val pagerAdapter = ObservableField<PagerAdapter>(StepsPagerAdapter())

    val nextClickEvent = NextClickEvent()
    val returnEvent = ReturnEvent()
    val copyEvent = CopyEvent()

    fun returnToStart() {
        returnEvent.call()
    }

    fun nextStep() {
        if (currentStep.get() < 3) {
            nextClickEvent.call()
        } else {
            returnEvent.call()
        }
    }

    fun proceed() {
        if (currentStep.get() < 4) {
            currentStep.set(currentStep.get() + 1)
        }
    }

    fun updateOrderId(orderId: String) {
        this.orderId.set(orderId)
        Direct.pendingOrderId = orderId
    }

    fun copyOrderId() {
        copyEvent.postValue(orderId.get())
    }

    class Factory(private val dispatcherProvider: CoroutineDispatcherProvider) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VerificationActivityViewModel(dispatcherProvider) as T
    }
}

class ReturnEvent : SingleLiveEvent<Void>()
class CopyEvent : SingleLiveEvent<String>()
