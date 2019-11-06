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

import android.os.CountDownTimer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class CheckCodeTest {

    @Test
    fun setCanResendOnFinish() {
        val checkCode = CheckCode().apply {
            timer = createTimer()
        }

        checkCode.timer.onFinish()

        assertThat(checkCode.canResend).isTrue()
    }

    @Test
    fun start() {
        val timer = mock<CountDownTimer>()

        val checkCode = spy(CheckCode(timer))
        whenever(checkCode.createTimer()).thenReturn(timer)

        checkCode.startTimer()

        verify(checkCode).createTimer()
    }

    @Test
    fun stop() {
        val timer = mock<CountDownTimer>()

        val checkCode = CheckCode(timer)
        checkCode.stopTimer()

        verify(timer).cancel()
    }

    @Test
    fun restart() {
        val timer = mock<CountDownTimer>()
        val spy = spy(CheckCode(timer))

        spy.restartTimer()

        verify(spy).stopTimer()
        verify(spy).startTimer()
        assertThat(spy.canResend).isFalse()
    }
}
