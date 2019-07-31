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

package com.cexdirect.lib._network

import com.cexdirect.lib._network.models.ApiResponse
import com.cexdirect.lib._network.models.ExchangeRatesResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

interface PaymentService {

    @GET("v1/payments/currencies/{placementId}")
    fun getExchangeRatesAsync(@Path("placementId") placementId: String): Deferred<ExchangeRatesResponse>

    @GET("v1/payments/wallet/{wallet}/{currency}/verify")
    fun verifyWalletAddressAsync(@Path("wallet") wallet: String, @Path("currency") currency: String): Deferred<ApiResponse<Void>>
}
