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

package com.cexdirect.lib

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


@Suppress("unused")
abstract class BaseObservableViewModel(private val dispatcherProvider: CoroutineDispatcherProvider) : ViewModel(),
    Observable, CoroutineScope {

    private val job by lazy { Job() }

    @Transient
    private var mCallbacks: PropertyChangeRegistry? = null

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.main + job

    @Synchronized
    override fun addOnPropertyChangedCallback(callback: OnPropertyChangedCallback) {
        if (mCallbacks == null) {
            mCallbacks = PropertyChangeRegistry()
        }
        mCallbacks!!.add(callback)
    }

    @Synchronized
    override fun removeOnPropertyChangedCallback(callback: OnPropertyChangedCallback) {
        mCallbacks?.remove(callback)
    }

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    @Synchronized
    fun notifyChange() {
        mCallbacks?.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies listeners that a specific property has changed. The getter for the property
     * that changes should be marked with [Bindable] to generate a field in
     * `BR` to be used as `fieldId`.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        mCallbacks?.notifyCallbacks(this, fieldId, null)
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}
