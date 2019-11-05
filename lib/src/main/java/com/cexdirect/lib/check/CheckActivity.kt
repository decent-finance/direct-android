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

package com.cexdirect.lib.check

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.BuyActivity
import com.cexdirect.lib.databinding.ActivityCheckBinding
import com.cexdirect.lib.di.annotation.CheckActivityFactory
import javax.inject.Inject

class CheckActivity : BaseActivity() {

    @field:[Inject CheckActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: CheckActivityViewModel by viewModelProvider { modelFactory }

    private lateinit var binding: ActivityCheckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_check)

        model.apply {
            result.observe(this@CheckActivity, requestObserver(
                onOk = { launchDirect() },
                onFail = { showStubScreen() }
            ))
            binding.model = this
        }.loadPlacementData()
    }

    private fun launchDirect() {
        startActivity(Intent(this, BuyActivity::class.java))
        finish()
    }
}
