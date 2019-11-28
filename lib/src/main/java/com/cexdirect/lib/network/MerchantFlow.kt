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
import com.cexdirect.lib.network.models.RuleData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@ExperimentalCoroutinesApi
@OpenForTesting
class MerchantFlow(private val service: MerchantService) {

    fun getCurrencyPrecisions(placementId: String) =
        flow { emit(service.getCurrencyPrecisions(placementId)) }
            .flowOn(DispatcherRegistry.io)

    fun getPlacementInfo(placementId: String) =
        flow { emit(service.getPlacementInfo(placementId)) }
            .flowOn(DispatcherRegistry.io)

    fun getRules(ids: List<String>) =
        flow {
            ids.fold(HashSet<RuleData>()) { acc, id ->
                acc.apply { add(service.getRule(id).data) }
            }.let { emit(it) }
        }.flowOn(DispatcherRegistry.io)
}
