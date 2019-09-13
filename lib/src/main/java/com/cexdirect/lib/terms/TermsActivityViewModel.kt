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

package com.cexdirect.lib.terms

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseObservableViewModel
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.VoidLiveEvent

class TermsActivityViewModel(dispatcherProvider: CoroutineDispatcherProvider) :
    BaseObservableViewModel(dispatcherProvider) {

    val title = ObservableField("")
    val content = ObservableField("")

    val okEvent = VoidLiveEvent()

    fun callOk() {
        okEvent.call()
    }

    class Factory(private val dispatcherProvider: CoroutineDispatcherProvider) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = TermsActivityViewModel(dispatcherProvider) as T
    }
}
