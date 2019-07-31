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

package com.cexdirect.lib.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.Direct
import com.cexdirect.lib._network.MerchantApi
import com.cexdirect.lib._util.PlacementValidator
import com.cexdirect.lib.network.models.RuleData

class CheckActivityViewModel(
    merchantApi: MerchantApi,
    private val placementValidator: PlacementValidator,
    private val ruleIds: RuleIds,
    dispatcherProvider: CoroutineDispatcherProvider
) : BaseObservableViewModel(dispatcherProvider) {

    val checkResult = merchantApi.getPlacementInfo(this, Direct.credentials.placementId)
    val ruleResult = merchantApi.getRule(this) { ruleIds.getCurrentRuleId() }

    fun checkPlacement() {
        checkResult.execute()
    }

    fun updateIds(rulesIds: List<String>) {
        ruleIds.ids = rulesIds
    }

    fun loadRules() {
        ruleResult.execute()
    }

    fun loadNextRule(block: () -> Unit) {
        if (ruleIds.shouldLoadNext()) {
            ruleResult.execute()
        } else {
            block.invoke()
        }
    }

    fun saveRule(ruleData: RuleData) {
        Direct.rules.add(ruleData)
        ruleIds.selectNextId()
    }

    @Suppress("ConstantConditionIf")
    fun canLaunch(activityStatus: Boolean, placementUris: List<String>) = activityStatus
//            if (BuildConfig.FLAVOR == "dev") true else activityStatus && isUriAllowed(placementUris)

    private fun isUriAllowed(placementUris: List<String>) =
        placementUris.find { placementValidator.isPlacementUriAllowed(it) }?.isNotEmpty()
            ?: false

    class Factory(
        private val merchantApi: MerchantApi,
        private val placementValidator: PlacementValidator,
        private val ruleIds: RuleIds,
        private val dispatcherProvider: CoroutineDispatcherProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CheckActivityViewModel(merchantApi, placementValidator, ruleIds, dispatcherProvider) as T
    }
}
