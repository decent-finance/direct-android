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

import com.cexdirect.lib.CollectibleLiveData
import com.cexdirect.lib.ExecutableLiveData
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.models.RuleData
import kotlinx.coroutines.CoroutineScope

@OpenForTesting
class MerchantApi(private val service: MerchantService) {

    fun getCurrencyPrecisions(scope: CoroutineScope, placementId: String) =
        ExecutableLiveData(scope) { service.getCurrencyPrecisionsAsync(placementId) }

    fun getPlacementInfo(scope: CoroutineScope, placementId: String) =
        ExecutableLiveData(scope) { service.getPlacementInfoAsync(placementId) }

    fun getRules(scope: CoroutineScope, ids: List<String>) =
        CollectibleLiveData(
            scope,
            HashSet<RuleData>(),
            ids,
            { acc, value -> acc.apply { add(value) } },
            { item -> service.getRuleAsync(item) }
        )
}
