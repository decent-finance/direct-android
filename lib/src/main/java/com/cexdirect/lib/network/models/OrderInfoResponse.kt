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

package com.cexdirect.lib.network.models

import java.util.*

class OrderInfoResponse(val data: OrderInfoData) : ApiResponse<OrderInfoData>() {
    override fun extract(): OrderInfoData = data
}

data class OrderInfoData(
    val orderId: String,
    val orderStatus: OrderStatus,
    val orderExpiredAt: Date,
    val userEmail: String,
    val country: String,
    val basic: Basic,
    val threeDS: _3Ds?,
    val paymentInfo: PaymentInfo?,
    val additional: Map<String, Additional>
)

data class Basic(
    val images: Images,
    val cardBin: String,
    val fiat: MonetaryData,
    val crypto: MonetaryData,
    val wallet: Wallet,
    val ipAddress: Boolean,
    val skipVerify: Boolean,
    val termUrl: String
)

data class Images(
    val isIdentityDocumentsRequired: Boolean,
    val isSelfieRequired: Boolean
)

data class Additional(
    val value: String?,
    val req: Boolean,
    val editable: Boolean
)

data class _3Ds(
    val txId: String,
    val method: String,
    val url: String,
    val type: String,
    val data: _3DsExtras
)

class _3DsExtras : HashMap<String, String>()

data class PaymentInfo(
    val fiat: MonetaryData,
    val crypto: MonetaryData,
    val wallet: Wallet,
    val txId: String?
)
