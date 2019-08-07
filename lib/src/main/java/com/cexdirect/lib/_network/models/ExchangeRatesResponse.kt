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

package com.cexdirect.lib._network.models

class ExchangeRatesResponse(val data: ExchangeData) : ApiResponse<List<ExchangeRate>>() {

    override fun extract(): List<ExchangeRate> = data.currencies
}

class ExchangeData(val currencies: List<ExchangeRate>)

data class ExchangeRate(
    val fiat: String,
    val crypto: String,
    val a: Double,
    val b: Double,
    val c: Double,
    val fiatPopularValues: List<String>,
    val cryptoPopularValue: List<String>
)
