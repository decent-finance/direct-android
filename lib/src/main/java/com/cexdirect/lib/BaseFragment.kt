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

import android.content.Intent
import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.terms.TermsActivity
import com.cexdirect.lib.terms.TermsFragmentArgs
import com.mcxiaoke.koi.ext.finish
import com.mcxiaoke.koi.ext.toast

abstract class BaseFragment : Fragment {

    constructor() : super()
    constructor(@LayoutRes id: Int) : super(id)

    protected val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    protected inline fun <reified VM : BaseObservableViewModel> activityViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(requireActivity(), factory()).get(VM::class.java) }

    protected inline fun <reified VM : BaseObservableViewModel> fragmentViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(this, factory()).get(VM::class.java) }

    protected inline fun <reified VM : BaseObservableViewModel> parentFragmentViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(requireParentFragment(), factory()).get(VM::class.java) }

    protected fun LegalViewModel.applyLegalObservers() {
        supportClickEvent.observe(this@BaseFragment, Observer {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(BaseActivity.SUPPORT_EMAIL)
            }.let {
                it.resolveActivity(requireContext().packageManager)?.run { startActivity(it) }
                    ?: toast(R.string.cexd_no_email_apps)
            }
        })
        legalClickEvent.observe(this@BaseFragment, Observer {
            val intent = Intent(requireContext(), TermsActivity::class.java).apply {
                val args = TermsFragmentArgs(it.formattedName(), it.value).toBundle()
                putExtras(args)
            }.let { startActivity(it) }
        })
        exitClickEvent.observe(this@BaseFragment, Observer {
            ExitDialog().show(childFragmentManager, "exit")
        })
    }

    protected fun showLoader() {
        (requireActivity() as DirectActivity).showLoader()
    }

    protected fun hideLoader() {
        (requireActivity() as DirectActivity).hideLoader()
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
        onFail: (res: Failure<T>) -> Unit = { findNavController().purchaseFailed(it.message) },
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
