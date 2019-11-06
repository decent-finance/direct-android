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

package com.cexdirect.lib.order.confirmation

import com.cexdirect.lib.BuildConfig
import com.cexdirect.lib.network.models.TdsExtras

data class TdsData(
    val tdsUrl: String,
    val tdsExtras: TdsExtras,
    val txId: String,
    val orderId: String
) {
    fun getTermUrl() = "${BuildConfig.REST_URL}api/v1/orders/3ds-check/$orderId/tx/$txId"
}
