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

package com.cexdirect.lib.check

import androidx.lifecycle.MutableLiveData
import com.cexdirect.lib.Direct
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.PlacementInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@FlowPreview
@ExperimentalCoroutinesApi
@OpenForTesting
class PlacementApi(private val merchantFlow: MerchantFlow, private val paymentFlow: PaymentFlow) {

    val checkResult = MutableLiveData<Resource<Boolean>>()

    fun loadPlacementData(scope: CoroutineScope, predicate: (info: PlacementInfo) -> Boolean) {
        checkResult.value = Loading()
        merchantFlow.getPlacementInfo(Direct.credentials.placementId)
            .onStart { checkResult.value = Loading() }
            .map {
                if (predicate.invoke(it.data)) {
                    it
                } else {
                    error("Placement inactive")
                }
            }
            .flatMapConcat { merchantFlow.getRules(it.data.rulesIds) }
            .onEach { Direct.updateRules(it) }
            .flatMapConcat { paymentFlow.getCountries() }
            .catch { checkResult.value = it.mapFailure() }
            .onEach {
                Direct.countries = it.data
                checkResult.value = Success(true)
            }
            .launchIn(scope)
    }
}
