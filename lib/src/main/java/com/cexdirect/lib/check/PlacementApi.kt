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
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.PlacementInfo
import com.cexdirect.livedatax.map
import com.cexdirect.livedatax.switchMap
import kotlinx.coroutines.CoroutineScope

class PlacementApi(
    private val merchantApi: MerchantApi,
    private val paymentApi: PaymentApi,
    private val scope: CoroutineScope,
    private val checkPlacementAction: (info: PlacementInfo) -> Boolean
) {

    private val checkResult = merchantApi.getPlacementInfo(scope, Direct.credentials.placementId)

    val placementDataResult = checkResult.map {
        if (it is Success && !checkPlacementAction.invoke(it.data!!)) {
            Failure(-1, "Placement inactive")
        } else {
            it
        }
    }.switchMap {
        it.enqueueWith({ merchantApi.getRules(scope, it.data!!.rulesIds).apply { execute() } })
    }.switchMap {
        it.enqueueWith({
            Direct.rules.apply {
                clear()
                addAll(it.data!!)
            }
            paymentApi.getCountries(scope).apply { execute() }
        })
    }.switchMap {
        it.enqueueWith({
            Direct.countries = it.data!!
            MutableLiveData<Resource<Boolean>>(Success(true))
        })
    }

    fun loadPlacementData() {
        checkResult.execute()
    }
}
