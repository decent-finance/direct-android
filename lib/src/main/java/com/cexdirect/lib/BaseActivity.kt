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
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.cexdirect.lib.terms.showTerms
import com.cexdirect.lib.views.LoaderView
import com.mcxiaoke.koi.ext.toast

abstract class BaseActivity : AppCompatActivity() {

    private var loader: LoaderView? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        AppCompatDelegate.setDefaultNightMode(Direct.theme.mode)
    }

    protected inline fun <reified VM : BaseObservableViewModel> viewModelProvider(crossinline factory: () -> ViewModelProvider.Factory) =
        lazy { ViewModelProviders.of(this, factory()).get(VM::class.java) }

    protected fun LegalViewModel.applyLegalObservers() {
        supportClickEvent.observe(this@BaseActivity, Observer {
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@cex.io")
            }.let {
                it.resolveActivity(packageManager)?.run { startActivity(it) }
                    ?: toast("No e-mail apps found")
            }
        })
        legalClickEvent.observe(this@BaseActivity, Observer {
            showTerms(it.formattedName(), it.value)
        })
        exitClickEvent.observe(this@BaseActivity, Observer { ExitDialog().show(supportFragmentManager, "exit") })
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
}
