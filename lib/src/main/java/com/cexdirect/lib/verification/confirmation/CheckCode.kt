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

package com.cexdirect.lib.verification.confirmation

import android.os.CountDownTimer
import androidx.annotation.VisibleForTesting
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.cexdirect.lib.BR
import com.cexdirect.lib.OpenForTesting

@OpenForTesting
class CheckCode() : BaseObservable() {

    @VisibleForTesting
    var timer: CountDownTimer =
        object : CountDownTimer(RESEND_TIMEOUT_MILLIS, TICK_INTERVAL_MILLIS) {

            override fun onFinish() {
                canResend = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = ((millisUntilFinished / 1000) % 60)
                remaining = "$mins:${secs.toString().padStart(2, '0')}"
            }
        }


    @get:Bindable
    var remaining = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.remaining)
        }

    @get:Bindable
    var code = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.code)
        }

    @get:Bindable
    var canResend = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.canResend)
        }

    constructor(timer: CountDownTimer) : this() {
        this.timer = timer
    }

    fun startTimer() {
        timer.start()
    }

    fun stopTimer() {
        timer.cancel()
    }

    fun restartTimer() {
        stopTimer()
        canResend = false
        startTimer()
    }

    companion object {
        private const val RESEND_TIMEOUT_MILLIS = 2 * 60 * 1000L
        private const val TICK_INTERVAL_MILLIS = 1000L
    }
}
