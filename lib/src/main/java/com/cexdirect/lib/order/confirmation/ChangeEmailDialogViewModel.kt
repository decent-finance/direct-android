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

package com.cexdirect.lib.order.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.VoidLiveEvent
import com.cexdirect.lib.order.identity.UserEmail

class ChangeEmailDialogViewModel :
    BaseObservableViewModel() {

    val userEmail = UserEmail()

    val saveEvent = VoidLiveEvent()
    val cancelEvent = VoidLiveEvent()

    fun saveEmail() {
        saveEvent.call()
    }

    fun cancel() {
        cancelEvent.call()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ChangeEmailDialogViewModel() as T
    }
}
