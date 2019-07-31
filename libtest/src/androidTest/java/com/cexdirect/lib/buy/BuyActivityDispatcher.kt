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

package com.cexdirect.lib.buy

import com.cexdirect.lib.BaseDispatcher
import com.cexdirect.lib.util.TEST_PLACEMENT
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class BuyActivityDispatcher : BaseDispatcher() {

    override fun dispatch(request: RecordedRequest) = when (request.path) {
        "/api/v1/merchant/precisions/$TEST_PLACEMENT" -> {
            val data = PrecisionData(
                listOf(
                    Precision(
                        "fiat",
                        "USD",
                        2,
                        2,
                        "trunk",
                        "100",
                        "1000"
                    ),
                    Precision(
                        "crypto",
                        "BTC",
                        8,
                        8,
                        "trunk",
                        "0.01",
                        "0.5"
                    )
                )
            )
            val response = PrecisionsResponse(data).applyOkFields()
            MockResponse().setResponseCode(200).setBody(gson.toJson(response))
        }
        "/api/v1/payments/currencies/$TEST_PLACEMENT" -> {
            val data = ExchangeData(
                listOf(
                    ExchangeRate(
                        "USD",
                        "BTC",
                        1.0,
                        0.0,
                        1.0,
                        listOf("100", "150", "200"),
                        emptyList()
                    )
                )
            )
            val response = ExchangeRatesResponse(data).applyOkFields()
            MockResponse().setResponseCode(200).setBody(gson.toJson(response))
        }
        else -> makeErrorResponse()
    }
}
