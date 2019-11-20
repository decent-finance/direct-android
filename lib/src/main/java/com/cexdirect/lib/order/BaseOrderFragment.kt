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

package com.cexdirect.lib.order

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.di.annotation.VerificationActivityFactory
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import javax.inject.Inject

abstract class BaseOrderFragment : BaseFragment() {

    @field:[Inject VerificationActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    @VisibleForTesting
    val model: OrderActivityViewModel by activityViewModelProvider { modelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.identitySubcomponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
    }

    protected fun <T> requestObserver(
        onLoading: () -> Unit = { showLoader() },
        onOk: (res: T?) -> Unit,
        onFail: (res: Failure<T>) -> Unit = { purchaseFailed(it.message, model.extractAmounts()) },
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
