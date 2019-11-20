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

package com.cexdirect.lib.order.confirmation

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CheckCodeTimeTest(private val given: Long, private val expected: String) {

    @Test
    fun formatTime() {
        val checkCode = CheckCode().apply {
            timer = createTimer()
        }
        checkCode.timer.onTick(given)

        assertThat(checkCode.remaining).isEqualTo(expected)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: Format {0} as {1}")
        fun getData() = arrayListOf(
            arrayOf(2 * 60 * 1000L, "2:00"),
            arrayOf(60 * 1000L, "1:00"),
            arrayOf(30 * 1000L, "0:30"),
            arrayOf(0L, "0:00")
        )
    }
}
