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

import com.cexdirect.lib.Direct
import com.cexdirect.lib._util.sha512

class ResendCheckCodeBody(data: ResendCheckCodeData) : BaseBody<ResendCheckCodeData>(data = data) {

    init {
        data.orderSecret = "${Direct.userEmail}${Direct.pendingOrderId}${serviceData.nonce}".sha512()
    }
}

data class ResendCheckCodeData(
    val orderId: String,
    var orderSecret: String = ""
)
