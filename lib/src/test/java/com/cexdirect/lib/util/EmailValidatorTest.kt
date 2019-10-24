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
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Parameterized::class)
class EmailValidatorTest(private val givenEmail: String, private val expectedStatus: FieldStatus) {

    @Test
    fun checkStatus() {
        val actual = checkEmailStatus(givenEmail)
        assertThat(actual).`as`("Email status").isEqualTo(expectedStatus)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0} is {1}")
        fun getData() = arrayListOf(
            arrayOf("", FieldStatus.EMPTY),
            arrayOf("dd@dd.de", FieldStatus.VALID),
            arrayOf("john.smith@example.com", FieldStatus.VALID),
            arrayOf("xyz", FieldStatus.INVALID),
            arrayOf("abc@qwe.......com", FieldStatus.INVALID)
        )
    }
}
