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

import com.cexdirect.lib.Direct
import com.cexdirect.lib._network.models.ExchangeRatesResponse
import com.cexdirect.lib._network.models.OrderInfoBody
import com.cexdirect.lib._network.models.OrderInfoResponse

open class BaseSocketMessage(val event: String)

class ExchangeRatesSubscription(val data: String, event: String) : BaseSocketMessage(event)

class ExchangeRatesMessage(val data: ExchangeRatesResponse, event: String) : BaseSocketMessage(event)

class OrderInfoSubscription(val data: OrderInfoBody, event: String) : BaseSocketMessage(event)

class OrderInfoMessage(val data: OrderInfoResponse, event: String) : BaseSocketMessage(event)

class UnsubscribeMessage(val data: UnsubscribeData, event: String = "unsubscribe") : BaseSocketMessage(event)

class UnsubscribeData(val event: String, val room: String = Direct.pendingOrderId)

enum class UnsubscribeType(val raw: String) {
    CURRENCIES("currencies"),
    ORDER_INFO("orderinfo")
}
