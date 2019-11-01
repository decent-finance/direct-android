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

import com.cexdirect.lib.Direct
import com.cexdirect.lib.ExecutableLiveData
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.ApiResponse
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.Precision
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.livedatax.combineLatestWith
import com.cexdirect.livedatax.switchMap
import kotlinx.coroutines.CoroutineScope

class CalculatorApi(
    private val merchantApi: MerchantApi,
    private val paymentApi: PaymentApi,
    private val analyticsApi: AnalyticsApi,
    private val messenger: Messenger,
    private val scope: CoroutineScope
) {

    private val openEvent = analyticsApi.sendOpenEvent(scope)
    private val buyEvent = analyticsApi.sendBuyEvent(scope) { eventData }

    private val precisions =
            merchantApi.getCurrencyPrecisions(scope, Direct.credentials.placementId)
    private val exchangeRates = paymentApi.getExchangeRates(scope)

    val calcData = openEvent
        .switchMap {
            it.enqueueWith({
                precisions.apply { execute() }.combineLatestWith(exchangeRates.apply { execute() })
                { first: Resource<List<Precision>>, second: Resource<List<ExchangeRate>> ->
                    if (first is Failure) {
                        Failure(first.code, first.message)
                    } else if (second is Failure) {
                        Failure(second.code, second.message)
                    } else if (first is Success && second is Success) {
                        Success(first.data!! to second.data!!)
                    } else {
                        Loading<Pair<List<Precision>, List<ExchangeRate>>>()
                    }
                }
            })
        }

    lateinit var eventData: EventData

    fun loadCalcData() {
        openEvent.execute()
    }

    fun sendBuyEvent(eventData: EventData): ExecutableLiveData<ApiResponse<Void>, Void> {
        this.eventData = eventData
        return buyEvent.apply { execute() }
    }

    fun subscribeToExchangeRates() = messenger.subscribeToExchangeRates()

    fun unsubscribeFromExchangeRates() {
        messenger.removeExchangesSubscription()
    }
}
