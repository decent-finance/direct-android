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

package com.cexdirect.lib.check

import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib.BaseDispatcher
import com.cexdirect.lib.network.models.PlacementInfo
import com.cexdirect.lib.network.models.PlacementInfoResponse
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.network.models.RuleResponse
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.cexdirect.lib.util.applyOkFields
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.util.*

class CheckActivityDispatcher : BaseDispatcher() {

    override fun dispatch(request: RecordedRequest) =
        when (request.path) {
//            "/" -> {
//                MockResponse().setResponseCode(200).
//            }
            "/api/v1/merchant/rules/1" -> {
                val data = RuleData("1", "Foo", "Bar", Date().toGMTString())
                val response = RuleResponse(data).applyOkFields()
                MockResponse().setResponseCode(200).setBody(gson.toJson(response))
            }
            "/api/v1/merchant/placement/check/$TEST_PLACEMENT" -> {
                val data = PlacementInfo(
                    "none",
                    TEST_PLACEMENT,
                    true,
                    listOf(InstrumentationRegistry.getInstrumentation().context.packageName),
                    listOf("1")
                )
                val response = PlacementInfoResponse(data).applyOkFields()
                MockResponse().setResponseCode(200).setBody(gson.toJson(response))
            }
            else -> makeErrorResponse()
        }
}
