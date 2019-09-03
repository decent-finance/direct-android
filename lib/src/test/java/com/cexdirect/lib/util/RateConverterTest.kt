/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.util

import org.assertj.core.api.Java6Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class RateConverterTest(
        a: Double,
        b: Double,
        c: Double,
        private val fiat: Double,
        private val crypto: Double,
        private val expectedFiat: Double,
        private val expectedCrypto: Double
) {

    private val converter = RateConverter(a, b, c)

    @Test
    fun convertToFiat() {
        val actual = converter.convertToFiat(crypto)

        assertThat(actual).isCloseTo(expectedFiat, Offset.offset(0.0001))
    }

    @Test
    fun convertToCrypto() {
        val actual = converter.convertToCrypto(fiat)

        assertThat(actual).isCloseTo(expectedCrypto, Offset.offset(0.0000000001))
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: convert {0}, {1}, {2}")
        fun getData() = arrayListOf(
                arrayOf(0.00010585050029429076, 0.0005, 1.0, 100.0, 1.0, 9452.010120106761, 0.010085050029429075),
                arrayOf(0.0009694878706199461, 0.001, 1.0, 100.0, 1.0, 1032.50389235, 0.09594878706199461),
                arrayOf(0.0011550405502515142, 0.01, 1.0, 100.0, 1.0, 874.428174647, 0.10550405502515144),
                arrayOf(0.0015570023548968001, 0.01, 1.0, 100.0, 0.5, 327.55249110320284, 0.14570023548968),
                arrayOf(0.000051757250960277854, 0.0005, 1.0, 100.0, 0.5, 9670.142650816573, 0.004675725096027785)
        )
    }
}
