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

package com.cexdirect.lib._util

import com.cexdirect.lib._network.models.Precision
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FormatAmountTest(
    private val given: Double,
    private val expected: String,
    private val precision: Precision
) {

    @Test
    fun formatAmount() {
        val actual = given.formatAmount(precision)

        assertThat(actual).isNotBlank().isEqualTo(expected)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: Format {0} as {1}")
        fun getData() = arrayListOf(
            arrayOf(1.000001, "1.00", Precision("", "FOO", 2, 2, "smaller", "", "")),
            arrayOf(1.000009, "1.00", Precision("", "FOO", 2, 2, "smaller", "", "")),
            arrayOf(1.099999, "1.09", Precision("", "FOO", 2, 2, "smaller", "", "")),
            arrayOf(1.099999, "1.10", Precision("", "BAR", 2, 2, "bigger", "", "")),
            arrayOf(1.000009, "1.00", Precision("", "BAR", 2, 2, "bigger", "", "")),
            arrayOf(1.000003, "1.00", Precision("", "BAR", 2, 2, "bigger", "", "")),
            arrayOf(1.099999, "1.09", Precision("", "BUZZ", 2, 2, "trunk", "", "")),
            arrayOf(1.134576, "1.13", Precision("", "BUZZ", 2, 2, "trunk", "", ""))
        )
    }
}
