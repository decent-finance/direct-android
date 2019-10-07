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

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success

abstract class BaseFragment : Fragment() {

    protected inline fun <reified VM : BaseObservableViewModel> activityViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(activity!!, factory()).get(VM::class.java) }

    protected inline fun <reified VM : BaseObservableViewModel> fragmentViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(this, factory()).get(VM::class.java) }

    protected fun showLoader() {
        (activity as BaseActivity).showLoader()
    }

    protected fun hideLoader() {
        (activity as BaseActivity).hideLoader()
    }

    protected fun <T> socketObserver(
        onOk: (res: T) -> Unit,
        onFail: (res: Failure<T>) -> Unit
    ) = Observer<Resource<T>> {
        when (it) {
            is Success -> {
                onOk.invoke(it.data!!)
            }
            is Failure -> {
                onFail.invoke(it)
            }
        }
    }

    protected fun <T> restObserver(
        onLoading: () -> Unit = { showLoader() },
        onOk: (res: T?) -> Unit,
        onFail: (res: Failure<T>) -> Unit = { purchaseFailed(it.message) },
        final: () -> Unit = { hideLoader() }
    ) = Observer<Resource<T>> {
        when (it) {
            is Loading -> onLoading.invoke()
            is Success -> {
                final.invoke()
                onOk.invoke(it.data)
            }
            is Failure -> {
                final.invoke()
                onFail.invoke(it)
            }
        }
    }
}
