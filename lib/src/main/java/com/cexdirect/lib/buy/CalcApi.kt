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

package com.cexdirect.lib.buy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cexdirect.lib.Direct
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.Precision
import com.cexdirect.lib.network.ws.Messenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@OpenForTesting
class CalcApi(
    private val analyticsApi: AnalyticsFlow,
    private val merchantApi: MerchantFlow,
    private val paymentApi: PaymentFlow,
    private val messenger: Messenger
) {

    val calcData = MutableLiveData<Resource<Pair<List<Precision>, List<ExchangeRate>>>>()
    private val buyEvent = MutableLiveData<Resource<Void>>()

    fun loadCalcData(scope: CoroutineScope) {
        analyticsApi.sendOpenEvent()
            .onStart { calcData.value = Loading() }
            .flatMapConcat {
                merchantApi.getCurrencyPrecisions(Direct.credentials.placementId).combine(
                    paymentApi.getExchangeRates(Direct.credentials.placementId)
                ) { precisions, rates ->
                    precisions.data.precisions to rates.data.currencies
                }.take(1)
            }
            .catch { calcData.value = it.mapFailure() }
            .onEach { calcData.value = Success(it) }
            .launchIn(scope)
    }

    fun sendBuyEvent(scope: CoroutineScope, eventData: EventData): LiveData<Resource<Void>> {
        analyticsApi.sendBuyEvent(eventData)
            .onStart { buyEvent.value = Loading() }
            .catch { buyEvent.value = it.mapFailure() }
            .onEach { buyEvent.value = Success(null) }
            .launchIn(scope)
        return buyEvent
    }

    fun subscribeToExchangeRates() = messenger.subscribeToExchangeRates()

    fun unsubscribeFromExchangeRates() {
        messenger.removeExchangesSubscription()
    }
}
