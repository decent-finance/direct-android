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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.CalcActivity.Companion.startBuyActivity
import com.cexdirect.lib.databinding.ActivityErrorBinding
import com.cexdirect.lib.di.annotation.ErrorActivityFactory
import com.cexdirect.lib.network.models.OrderStatus
import com.mcxiaoke.koi.ext.finish
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
        intent.getParcelableExtra<LastKnownOrderInfo>("info")
            ?.let {
                model.orderId.set(it.orderId)
                model.orderAmounts.selectedCryptoAmount = it.cryptoAmount
                model.orderAmounts.selectedCryptoCurrency = it.cryptoCurrency
                model.orderAmounts.selectedFiatAmount = it.fiatAmount
                model.orderAmounts.selectedFiatCurrency = it.fiatCurrency
            }

        model.applyLegalObservers()

        model.tryAgainEvent.observe(this, Observer {
            finish()
            startBuyActivity(data = null)
        })

        binding.model = model

        when (ErrorType.valueOf(intent.getStringExtra("type")!!)) {
            ErrorType.PURCHASE_FAILED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, PurchaseFailedFragment())
                .commit()
            ErrorType.VERIFICATION_ERROR -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, GenericErrorFragment())
                .commit()
            ErrorType.NOT_VERIFIED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, VerificationRejectedFragment())
                .commit()
            ErrorType.LOCATION_NOT_SUPPORTED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, LocationNotSupportedFragment())
                .commit()
            ErrorType.PROCESSING_REJECTED -> supportFragmentManager
                .beginTransaction()
                .replace(binding.aeErrorFrame.id, ProcessingRejectedFragment())
                .commit()
        }

        model.clearData()
    }
}

fun Context.paymentRejected(rejectStatus: OrderStatus, orderInfo: LastKnownOrderInfo) {
    when (rejectStatus) {
        OrderStatus.IVS_REJECTED -> {
            Intent(this, ErrorActivity::class.java).apply {
                putExtra("type", ErrorType.NOT_VERIFIED.name)
                putExtra("info", orderInfo)
            }.let { startActivity(it) }
        }
        OrderStatus.IVS_FAILED, OrderStatus.PSS_FAILED -> {
            Intent(this, ErrorActivity::class.java).apply {
                putExtra("type", ErrorType.VERIFICATION_ERROR.name)
                putExtra("info", orderInfo)
            }.let { startActivity(it) }
        }
        OrderStatus.PSS_REJECTED -> {
            Intent(this, ErrorActivity::class.java).apply {
                putExtra("type", ErrorType.PROCESSING_REJECTED.name)
                putExtra("info", orderInfo)
            }.let { startActivity(it) }
        }
        OrderStatus.REJECTED -> {
            Intent(this, ErrorActivity::class.java).apply {
                putExtra("type", ErrorType.PURCHASE_FAILED.name)
                putExtra("reason", "Rejected")
                putExtra("info", orderInfo)
            }.let { startActivity(it) }
        }
        else -> error("Illegal reject status: ${rejectStatus.name}")
    }
}

fun Fragment.purchaseFailed(reason: String?, orderInfo: LastKnownOrderInfo) {
    this.context!!.showPurchaseFailedScreen(reason, orderInfo)
    finish()
}

fun Activity.purchaseFailed(reason: String?, orderInfo: LastKnownOrderInfo) {
    showPurchaseFailedScreen(reason, orderInfo)
    finish()
}

internal fun Context.showPurchaseFailedScreen(reason: String?, orderInfo: LastKnownOrderInfo) {
    Intent(this, ErrorActivity::class.java).apply {
        putExtra("type", ErrorType.PURCHASE_FAILED.name)
        putExtra("reason", reason)
        putExtra("info", orderInfo)
    }.let { startActivity(it) }
}

fun Fragment.locationNotSupported(
    orderInfo: LastKnownOrderInfo,
    countryCode: String,
    stateCode: String,
    email: String
) {
    Intent(requireContext(), ErrorActivity::class.java).apply {
        putExtra("type", ErrorType.LOCATION_NOT_SUPPORTED.name)
        putExtra("info", orderInfo)
        putExtra("code", countryCode)
        putExtra("state", stateCode)
        putExtra("email", email)
    }.let {
        startActivity(it)
        finish()
    }
}

enum class ErrorType {
    VERIFICATION_ERROR,
    NOT_VERIFIED,
    LOCATION_NOT_SUPPORTED,
    PURCHASE_FAILED,
    PROCESSING_REJECTED
}
