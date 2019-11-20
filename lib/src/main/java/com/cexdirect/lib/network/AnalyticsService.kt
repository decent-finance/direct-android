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

import com.cexdirect.lib.network.models.ApiResponse
import com.cexdirect.lib.network.models.DefaultEvent
import retrofit2.http.Body
import retrofit2.http.PUT

interface AnalyticsService {

    @PUT("api/v1/orders/buy")
    suspend fun sendBuyEvent(@Body body: DefaultEvent): ApiResponse<Void>

    @PUT("api/v1/orders/opened")
    suspend fun sendNewOrderEvent(@Body body: DefaultEvent): ApiResponse<Void>
}
