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

package com.cexdirect.lib._network.ws

import com.cexdirect.lib._network.Failure
import com.cexdirect.lib._network.Resource
import com.cexdirect.lib._network.Success
import com.cexdirect.lib._network.models.ApiResponse

const val CODE_BAD_REQUEST = 400

fun <T : ApiResponse<V>, V> mapResponse(response: T): Resource<V> =
    if (response.code < CODE_BAD_REQUEST) {
        Success<V>(response.extract()!!)
    } else {
        Failure<V>(response.code, response.message ?: "")
    }
