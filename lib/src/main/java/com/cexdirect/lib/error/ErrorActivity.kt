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

package com.cexdirect.lib.error

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.ErrorActivityFactory
import com.cexdirect.lib.buy.startBuyActivity
import com.cexdirect.lib.databinding.ActivityErrorBinding
import javax.inject.Inject

class ErrorActivity : BaseActivity() {

    @field:[Inject ErrorActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: ErrorActivityViewModel by viewModelProvider { modelFactory }

    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_error)

        intent.getStringExtra("reason")
            ?.takeIf { it.isNotBlank() }
            ?.let { model.reason.set(it) }
            ?: model.reason.set("Unknown Error")

        model.applyLegalObservers()

        model.userEmail.set(Direct.userEmail)
        model.tryAgainEvent.observe(this, Observer {
            finish()
            startBuyActivity()
        })

        binding.model = model

        when (ErrorType.valueOf(intent.getStringExtra("type")!!)) {
            ErrorType.PURCHASE_FAILED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, PurchaseFailedFragment())
                .commit()
            ErrorType.VERIFICATION_ERROR -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, VerificationErrorFragment())
                .commit()
            ErrorType.LOCATION_NOT_SUPPORTED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, LocationNotSupportedFragment())
                .commit()
        }

        Direct.directComponent.socket().stop()
    }
}

fun Context.verificationError(reason: String?) {
    Intent(this, ErrorActivity::class.java).apply {
        putExtra("type", ErrorType.VERIFICATION_ERROR.name)
        putExtra("reason", reason)
    }.let { startActivity(it) }
}

fun Context.purchaseFailed(reason: String?) {
    Intent(this, ErrorActivity::class.java).apply {
        putExtra("type", ErrorType.PURCHASE_FAILED.name)
        putExtra("reason", reason)
    }.let { startActivity(it) }
}

fun Context.locationNotSupported() {
    Intent(this, ErrorActivity::class.java).apply {
        putExtra("type", ErrorType.LOCATION_NOT_SUPPORTED.name)
    }.let { startActivity(it) }
}

enum class ErrorType {
    VERIFICATION_ERROR, LOCATION_NOT_SUPPORTED, PURCHASE_FAILED
}
