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

enum class OrderStatus(val raw: String) {
    INCOMPLETE("uncomplited"),
    IVS_READY("ivs-ready"),
    IVS_PENDING("ivs-pending"),
    IVS_SUCCESS("ivs-success"),
    IVS_FAILED("ivs-failed"),
    IVS_REJECTED("ivs-rejected"),
    PSS_WAITDATA("pss-waitdata"),
    PSS_READY("pss-ready"),
    PSS_PENDING("pss-pending"),
    PSS_3DS_REQUIRED("pss-3ds-required"),
    PSS_3DS_PENDING("pss-3ds-pending"),
    PSS_SUCCESS("pss-success"),
    WAITING_FOR_CONFIRMATION("waiting-for-confirmation"),
    COMPLETE("completed"),
    FINISHED("finished"),
    REJECTED("rejected")
}
