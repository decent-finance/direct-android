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

import com.cexdirect.lib.network.models.PlacementInfoResponse
import com.cexdirect.lib.network.models.PrecisionsResponse
import com.cexdirect.lib.network.models.RuleResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface MerchantService {

    @GET("api/v1/merchant/precisions/{placementId}")
    suspend fun getCurrencyPrecisions(@Path("placementId") placementId: String): PrecisionsResponse

    @GET("api/v1/merchant/placement/check/{placementId}")
    suspend fun getPlacementInfo(@Path("placementId") placementId: String): PlacementInfoResponse

    @GET("api/v1/merchant/rules/{ruleId}")
    suspend fun getRule(@Path("ruleId") ruleId: String): RuleResponse
}
