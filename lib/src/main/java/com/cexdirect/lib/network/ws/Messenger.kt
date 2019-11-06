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

package com.cexdirect.lib.network.ws

import androidx.lifecycle.LiveData
import com.cexdirect.lib.Direct
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.OrderInfoBody
import com.cexdirect.lib.network.models.OrderInfoData
import com.cexdirect.livedatax.distinctUntilChanged
import com.cexdirect.livedatax.filter
import com.cexdirect.livedatax.map
import com.google.gson.Gson

@OpenForTesting
class Messenger(private val cexdSocket: LiveSocket, private val gson: Gson) {

    fun subscribeToOrderInfo(): LiveData<Resource<OrderInfoData>> =
        cexdSocket.run {
            sendMessage { OrderInfoSubscription(OrderInfoBody(), "orderInfo") }
            parsedMessage
                .filter { it.first == "orderInfo" }
                .map { gson.fromJson(it.second, OrderInfoMessage::class.java).data }
                .map { mapResponse(it) }
                .distinctUntilChanged { previous, current ->
                    if (previous is Success && current is Success) {
                        previous.data == current.data
                    } else {
                        false
                    }
                }
        }

    fun removeOrderInfoSubscription() {
        cexdSocket.removeSubscriptionByKey("orderInfo")
        cexdSocket.sendMessage {
            UnsubscribeMessage(data = UnsubscribeData(event = UnsubscribeType.ORDER_INFO.raw))
        }
    }

    fun subscribeToExchangeRates(
        placementId: String = Direct.credentials.placementId
    ): LiveData<Resource<List<ExchangeRate>>> =
        cexdSocket.run {
            sendMessage { ExchangeRatesSubscription(placementId, "currencies") }
            parsedMessage
                .filter { it.first == "currencies" }
                .map { gson.fromJson(it.second, ExchangeRatesMessage::class.java).data }
                .map { mapResponse(it) }
        }

    fun removeExchangesSubscription() {
        cexdSocket.removeSubscriptionByKey("currencies")
        cexdSocket.sendMessage {
            UnsubscribeMessage(data = UnsubscribeData(event = UnsubscribeType.CURRENCIES.raw))
        }
    }

    fun clear() {
        cexdSocket.stop()
        cexdSocket.removeAllSubscriptions()
    }
}
