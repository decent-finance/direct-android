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

import com.cexdirect.lib.BuildConfig
import com.cexdirect.lib.ExecutableLiveData
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.models.ApiResponse
import com.cexdirect.lib.network.models.WalletAddressData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

@OpenForTesting
class PaymentApi(private val service: PaymentService) {

    fun getExchangeRates(scope: CoroutineScope, placementId: String) =
        ExecutableLiveData(scope) { service.getExchangeRatesAsync(placementId) }

    fun verifyWalletAddress(scope: CoroutineScope, block: () -> WalletAddressData) =
        ExecutableLiveData(scope) {
            if (!BuildConfig.DEBUG) {
                val (address, currency) = block.invoke()
                service.verifyWalletAddressAsync(address, currency)
            } else {
                scope.async {
                    ApiResponse<Void>()
                    // TODO: move this part to dev flavor
                }
            }
        }

    fun getCountries(scope: CoroutineScope) =
        ExecutableLiveData(scope) { service.getCountriesAsync() }
}
