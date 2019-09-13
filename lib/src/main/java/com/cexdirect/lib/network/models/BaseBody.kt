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
import com.cexdirect.lib.util.sha512

abstract class AuthBody<T : BaseData>(
    serviceData: ServiceData = ServiceData(),
    data: T
) : BaseBody<T>(serviceData, data) {
    init {
        data.orderSecret = "${Direct.userEmail}${Direct.pendingOrderId}${serviceData.nonce}".sha512()
    }
}

abstract class BaseBody<T>(
    val serviceData: ServiceData = ServiceData(),
    val data: T
)

data class ServiceData(
    val nonce: Long = System.currentTimeMillis(),
    val deviceFingerprint: String = Direct.fingerprint,
    val signatureType: String = "msignature512",
    val signature: String = Direct.credentials.getSignature(nonce),
    val placementId: String = Direct.credentials.placementId
)
