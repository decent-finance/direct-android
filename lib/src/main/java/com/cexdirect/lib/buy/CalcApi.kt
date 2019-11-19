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
    private val analyticsFlow: AnalyticsFlow,
    private val merchantFlow: MerchantFlow,
    private val paymentFlow: PaymentFlow,
    private val messenger: Messenger
) {

    val calcData = MutableLiveData<Resource<Pair<List<Precision>, List<ExchangeRate>>>>()

    fun loadCalcData(scope: CoroutineScope) {
        analyticsFlow.sendOpenEvent()
            .onStart { calcData.value = Loading() }
            .flatMapConcat {
                merchantFlow.getCurrencyPrecisions(Direct.credentials.placementId).combine(
                    paymentFlow.getExchangeRates(Direct.credentials.placementId)
                ) { precisions, rates ->
                    precisions.data.precisions to rates.data.currencies
                }.take(1)
            }
            .catch { calcData.value = it.mapFailure() }
            .onEach { calcData.value = Success(it) }
            .launchIn(scope)
    }

    fun sendBuyEvent(scope: CoroutineScope, eventData: EventData): LiveData<Resource<Void>> =
        MutableLiveData<Resource<Void>>().apply {
            analyticsFlow.sendBuyEvent(eventData)
                .onStart { this@apply.value = Loading() }
                .catch { this@apply.value = it.mapFailure() }
                .onEach { this@apply.value = Success() }
                .launchIn(scope)
        }

    fun subscribeToExchangeRates() = messenger.subscribeToExchangeRates()

    fun unsubscribeFromExchangeRates() {
        messenger.removeExchangesSubscription()
    }
}
