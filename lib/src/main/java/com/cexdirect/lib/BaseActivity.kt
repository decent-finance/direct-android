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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.cexdirect.lib.error.LastKnownOrderInfo
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.stub.StubActivity
import com.cexdirect.lib.terms.showTerms
import com.cexdirect.lib.views.LoaderView
import com.mcxiaoke.koi.ext.toast

abstract class BaseActivity : AppCompatActivity() {

    private var loader: LoaderView? = null

    protected inline fun <reified VM : BaseObservableViewModel> viewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) = lazy { ViewModelProviders.of(this, factory()).get(VM::class.java) }

    protected fun LegalViewModel.applyLegalObservers() {
        supportClickEvent.observe(this@BaseActivity, Observer {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(SUPPORT_EMAIL)
            }.let {
                it.resolveActivity(packageManager)?.run { startActivity(it) }
                    ?: toast(R.string.cexd_no_email_apps)
            }
        })
        legalClickEvent.observe(this@BaseActivity, Observer {
            showTerms(it.formattedName(), it.value)
        })
        exitClickEvent.observe(this@BaseActivity, Observer {
            ExitDialog().show(supportFragmentManager, "exit")
        })
    }

    fun showLoader() {
        if (loader == null) {
            loader = LoaderView(this)
        }
        loader!!.show()
    }

    fun hideLoader() {
        loader?.hide()
        loader = null
    }

    protected fun showStubScreen() {
        startActivity(Intent(this, StubActivity::class.java))
        finish()
    }

    protected fun <T> messageObserver(onOk: (res: T) -> Unit, onFail: (res: Failure<T>) -> Unit) =
        Observer<Resource<T>> {
            when (it) {
                is Success -> onOk.invoke(it.data!!)
                is Failure -> onFail.invoke(it)
            }
        }

    protected fun <T> requestObserver(
        onLoading: () -> Unit = { showLoader() },
        onOk: (res: T?) -> Unit,
        onFail: (res: Failure<T>) -> Unit = {
            purchaseFailed(
                it.message,
                LastKnownOrderInfo("", "", "", "", "")
            )
        },
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

    companion object {
        const val SUPPORT_EMAIL = "mailto:support@cexdirect.com"
    }
}
