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
import androidx.annotation.VisibleForTesting
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import com.cexdirect.lib.BR
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.util.FieldStatus

@OpenForTesting
class CheckCode() : BaseObservable() {

    @VisibleForTesting
    lateinit var timer: CountDownTimer

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
    var codeStatus = FieldStatus.EMPTY
        set(value) {
            field = value
            notifyPropertyChanged(BR.codeStatus)
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

    init {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (propertyId == BR.code) {
                    codeStatus = validateCheckCode(code)
                }
            }
        })
    }

    @VisibleForTesting
    fun createTimer(): CountDownTimer {
        return object : CountDownTimer(RESEND_TIMEOUT_MILLIS, TICK_INTERVAL_MILLIS) {

            override fun onFinish() {
                canResend = true
            }

            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / MILLIS_IN_SEC) / SECS_IN_MIN
                val secs = ((millisUntilFinished / MILLIS_IN_SEC) % SECS_IN_MIN)
                remaining = "$mins:${secs.toString().padStart(2, '0')}"
            }
        }
    }

    fun startTimer() {
        timer = createTimer()
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

    @VisibleForTesting
    internal fun validateCheckCode(code: String) =
        if (code.isEmpty()) {
            FieldStatus.EMPTY
        } else {
            FieldStatus.VALID
        }

    fun forceValidate() {
        if (codeStatus == FieldStatus.EMPTY) {
            codeStatus = FieldStatus.INVALID
        }
    }

    fun setCodeInvalid() {
        codeStatus = FieldStatus.INVALID
    }

    fun isValid() = codeStatus == FieldStatus.VALID

    companion object {
        private const val RESEND_TIMEOUT_MILLIS = 2 * 60 * 1000L
        private const val TICK_INTERVAL_MILLIS = 1000L

        private const val MILLIS_IN_SEC = 1000
        private const val SECS_IN_MIN = 60
    }
}
