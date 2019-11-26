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

package com.cexdirect.lib.error

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.AmountViewModel
import com.cexdirect.lib.BuildConfig
import com.cexdirect.lib.Direct
import com.cexdirect.lib.VoidLiveEvent
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.livedatax.throttleFirst
import java.util.concurrent.TimeUnit

class ErrorActivityViewModel(private val messenger: Messenger) : AmountViewModel() {

    val reason = ObservableField("")

    val tryAgainEvent = VoidLiveEvent()
    private val goBackEvent = VoidLiveEvent()
    val goBack = goBackEvent
        .throttleFirst(BuildConfig.THROTTLE_DELAY_MILLIS, TimeUnit.MILLISECONDS)

    fun tryAgain() {
        tryAgainEvent.call()
    }

    fun goBack() {
        goBackEvent.call()
    }

    fun clearData() {
        Direct.clear()
    }

    class Factory(private val messenger: Messenger) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ErrorActivityViewModel(messenger) as T
    }
}
