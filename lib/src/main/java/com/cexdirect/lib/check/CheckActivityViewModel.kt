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
import com.cexdirect.lib.Direct
import com.cexdirect.lib.network.MerchantApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.models.CountryData
import com.cexdirect.lib.network.models.PlacementInfo
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.util.PlacementValidator

class CheckActivityViewModel(
    merchantApi: MerchantApi,
    paymentApi: PaymentApi,
    private val placementValidator: PlacementValidator,
    private val ruleIds: RuleIds
) : BaseObservableViewModel() {

    val checkResult = merchantApi.getPlacementInfo(this, Direct.credentials.placementId)
    val ruleResult = merchantApi.getRule(this) { ruleIds.getCurrentRuleId() }
    val countryResult = paymentApi.getCountries(this)

    fun checkPlacement() {
        checkResult.execute()
    }

    private fun updateIds(rulesIds: List<String>) {
        ruleIds.ids = rulesIds
    }

    private fun loadCountries() {
        countryResult.execute()
    }

    fun saveCountriesAndLoadRules(data: List<CountryData>) {
        Direct.countries = data
        loadRules()
    }

    private fun loadRules() {
        ruleResult.execute()
    }

    private fun loadNextRule(block: () -> Unit) {
        if (ruleIds.shouldLoadNext()) {
            ruleResult.execute()
        } else {
            block.invoke()
        }
    }

    fun saveRuleAndLoadNext(ruleData: RuleData, action: () -> Unit) {
        Direct.rules.add(ruleData)
        ruleIds.selectNextId()
        loadNextRule(action)
    }

    fun processPlacementInfo(info: PlacementInfo, failAction: () -> Unit) {
        if (canLaunch(info.activityStatus, info.placementUris)) {
            updateIds(info.rulesIds)
            loadCountries()
        } else {
            failAction.invoke()
        }
    }

    @Suppress("ConstantConditionIf")
    fun canLaunch(activityStatus: Boolean, placementUris: List<String>) = activityStatus
//            if (BuildConfig.FLAVOR == "dev") true else activityStatus && isUriAllowed(placementUris)

    private fun isUriAllowed(placementUris: List<String>) =
        placementUris.find { placementValidator.isPlacementUriAllowed(it) }?.isNotEmpty()
            ?: false

    class Factory(
        private val merchantApi: MerchantApi,
        private val paymentApi: PaymentApi,
        private val placementValidator: PlacementValidator,
        private val ruleIds: RuleIds
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CheckActivityViewModel(
                merchantApi,
                paymentApi,
                placementValidator,
                ruleIds
            ) as T
    }
}
