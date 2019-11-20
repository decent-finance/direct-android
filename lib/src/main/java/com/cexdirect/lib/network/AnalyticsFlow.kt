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

package com.cexdirect.lib.network

import com.cexdirect.lib.DispatcherRegistry
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.models.DefaultEvent
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.models.MonetaryData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@ExperimentalCoroutinesApi
@OpenForTesting
class AnalyticsFlow(private val service: AnalyticsService) {

    fun sendOpenEvent() =
        flow {
            val eventData =
                EventData(fiat = MonetaryData("0", "USD"), crypto = MonetaryData("0", "BTC"))
            emit(service.sendNewOrderEvent(DefaultEvent(eventData)))
        }.flowOn(DispatcherRegistry.io)

    fun sendBuyEvent(data: EventData) =
        flow { emit(service.sendBuyEvent(DefaultEvent(data))) }
            .flowOn(DispatcherRegistry.io)
}
