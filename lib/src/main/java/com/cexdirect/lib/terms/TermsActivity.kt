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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.TermsActivityFactory
import com.cexdirect.lib.databinding.ActivityTermsBinding
import javax.inject.Inject

class TermsActivity : BaseActivity() {

    @field:[Inject TermsActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val viewModel: TermsActivityViewModel by viewModelProvider { modelFactory }

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Direct.directComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms)

        viewModel.apply {
            title.set(intent.getStringExtra("title"))
            content.set(intent.getStringExtra("content"))
            okEvent.observe(this@TermsActivity, Observer { finish() })
        }.let { binding.model = it }
    }
}

fun Context.showTerms(title: String, content: String) {
    Intent(this, TermsActivity::class.java)
        .apply {
            putExtra("title", title.replace("#", ""))
            putExtra("content", content)
        }
        .let { startActivity(it) }
}
