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

package com.cexdirect.lib.util

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class CardNumberFormatUtilsTest {

    @Test
    fun extractCardNumberBin() {
        val given = "1234567887654321"

        val actual = given.binifyCardNumber()

        assertThat(actual).isNotBlank().isEqualTo("4321")
    }


}
