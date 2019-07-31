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

package com.cexdirect.lib._network.ws

import androidx.lifecycle.LiveData
import com.cexdirect.lib.Direct
import com.cexdirect.lib._network.Resource
import com.cexdirect.lib._network.models.ExchangeRate
import com.cexdirect.lib._network.models.OrderInfoBody
import com.cexdirect.lib._network.models.OrderInfoData
import com.google.gson.Gson
import com.shopify.livedataktx.filter
import com.shopify.livedataktx.map

class Messenger(private val cexdSocket: CexdSocket, private val gson: Gson) {

    fun subscribeToOrderInfo(): LiveData<Resource<OrderInfoData?>> =
        cexdSocket.run {
            sendMessage { OrderInfoSubscription(OrderInfoBody(), "orderInfo") }
            parsedMessage
                .filter { it.first == "orderInfo" }
                .map { gson.fromJson(it.second, OrderInfoMessage::class.java).data }
                .map { mapResponse(it) }
        }

    fun removeOrderInfoSubscription() {
        cexdSocket.removeSubscriptionByKey("orderInfo")
        cexdSocket.sendMessage {
            UnsubscribeMessage(data = UnsubscribeData(event = UnsubscribeType.ORDER_INFO.raw))
        }
    }

    fun subscribeToExchangeRates(placementId: String = Direct.credentials.placementId): LiveData<Resource<List<ExchangeRate>?>> =
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
}

