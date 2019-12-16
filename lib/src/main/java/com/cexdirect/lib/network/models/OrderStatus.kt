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

enum class OrderStatus(val raws: List<String>) {
    INCOMPLETE(listOf("uncomplited", "new")),
    IVS_READY(listOf("ivs-ready", "verification-ready")),
    IVS_PENDING(listOf("ivs-pending", "verification-in-progress")),
    IVS_SUCCESS(listOf("ivs-success", "verification-success")),
    IVS_FAILED(listOf("ivs-failed", "verification-failed")),
    IVS_REJECTED(listOf("ivs-rejected", "verification-rejected")),
    PSS_WAITDATA(listOf("pss-waitdata", "processing-acknowledge")),
    PSS_READY(listOf("pss-ready", "processing-ready")),
    PSS_PENDING(listOf("pss-pending", "processing-in-progress")),
    PSS_3DS_REQUIRED(listOf("pss-3ds-required", "processing-3ds")),
    PSS_3DS_PENDING(listOf("pss-3ds-pending", "processing-3ds-pending")),
    PSS_FAILED(listOf("processing-failed")),
    PSS_REJECTED(listOf("processing-rejected")),
    PSS_SUCCESS(listOf("pss-success", "processing-success")),
    REFUND_PENDING(listOf("refund-in-progress")),
    REFUNDED(listOf("refunded")),
    WAITING_FOR_CONFIRMATION(listOf("waiting-for-confirmation", "email-confirmation")),
    COMPLETE(listOf("completed", "crypto-sending")),
    FINISHED(listOf("finished", "crypto-sent")),
    REJECTED(listOf("rejected")),
    CRASHED(listOf("crashed"))
}
