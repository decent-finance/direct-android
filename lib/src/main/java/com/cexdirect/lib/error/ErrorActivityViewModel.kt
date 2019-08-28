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

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.LegalViewModel
import com.cexdirect.lib.VoidLiveEvent
import com.cexdirect.lib._util.EmailStatus
import com.cexdirect.lib._util.checkEmailStatus

class ErrorActivityViewModel(dispatcherProvider: CoroutineDispatcherProvider) : LegalViewModel(dispatcherProvider) {

    var emailStatus = EmailStatus.EMPTY
        private set

    val reason = ObservableField("")
    val userEmail = ObservableField("").apply {
        addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                emailStatus = checkEmailStatus(get())
            }
        })
    }
    val emailNotificationChecked = ObservableBoolean(false)

    val tryAgainEvent = VoidLiveEvent()
    val informMeEvent = VoidLiveEvent()

    fun tryAgain() {
        tryAgainEvent.call()
    }

    fun informMe() {
        informMeEvent.call()
    }

    class Factory(private val dispatcherProvider: CoroutineDispatcherProvider) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ErrorActivityViewModel(dispatcherProvider) as T
    }
}
