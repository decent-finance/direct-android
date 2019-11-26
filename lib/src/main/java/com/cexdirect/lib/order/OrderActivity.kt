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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.ConfirmExitActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.AmountData
import com.cexdirect.lib.buy.CalcActivity.Companion.startBuyActivity
import com.cexdirect.lib.databinding.ActivityOrderBinding
import com.cexdirect.lib.di.annotation.VerificationActivityFactory
import com.cexdirect.lib.order.confirmation.PaymentConfirmationFragment
import com.cexdirect.lib.order.identity.IdentityFragment
import com.cexdirect.lib.order.identity.VerificationProgressFragment
import com.cexdirect.lib.order.receipt.ReceiptFragment
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class OrderActivity : ConfirmExitActivity() {

    @field:[Inject VerificationActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    @VisibleForTesting
    val model: OrderActivityViewModel by viewModelProvider { modelFactory }

    private val fragments =
        listOf(IdentityFragment(), PaymentConfirmationFragment(), ReceiptFragment())

    private val goToBuyActivityObserver = Observer<Void> {
        startBuyActivity(
            AmountData(
                model.orderAmounts.selectedFiatAmount,
                model.orderAmounts.selectedFiatCurrency,
                model.orderAmounts.selectedCryptoCurrency
            )
        )
        finish()
    }

    private lateinit var binding: ActivityOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order)
        binding.aoStepsViewPager.pageMargin = VIEWPAGER_ELEMENT_MARGIN

        model.apply {
            intent.let {
                setOrderAmounts(
                    it.getStringExtra("crypto"),
                    it.getStringExtra("cryptoAmount"),
                    it.getStringExtra("fiat"),
                    it.getStringExtra("fiatAmount")
                )
                userEmail.email = it.getStringExtra("email") ?: ""
                val countryCode = it.getStringExtra("countryCode")
                Direct.countries.find { it.code == countryCode }
                    ?.let { userCountry.selectedCountry = it }
                val stateCode = it.getStringExtra("stateCode")
                Direct.countries.find { !it.states.isNullOrEmpty() }
                    ?.states
                    ?.find { it.code == stateCode }
                    ?.let { userCountry.selectedState = it }
            }
            applyLegalObservers()
            stepChangeEvent.observe(this@OrderActivity, Observer {
                model.proceed()
                replaceFragment(model.currentStep.get() - 1)
            })
            editClick.observe(this@OrderActivity, goToBuyActivityObserver)
            returnClick.observe(this@OrderActivity, goToBuyActivityObserver)
            copyEvent.observe(this@OrderActivity, Observer { orderId ->
                if (!orderId.isNullOrBlank()) {
                    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                        ClipData.newPlainText(getString(R.string.cexd_order_id_label), orderId)
                    toast(getString(R.string.cexd_order_id_copied))
                }
            })
            scrollRequestEvent.observe(this@OrderActivity, Observer {
                val view = findViewById<View>(it)
                binding.aoScroll.requestChildFocus(view, view)
            })
            verificationInProgressEvent.observe(this@OrderActivity, Observer { inProgress ->
                val orderStep = supportFragmentManager.findFragmentByTag("step")!!
                if (inProgress) {
                    supportFragmentManager
                        .beginTransaction()
                        .hide(orderStep)
                        .add(R.id.aoFragmentFrame, VerificationProgressFragment(), "ivs-loader")
                        .commit()
                } else {
                    supportFragmentManager.findFragmentByTag("ivs-loader")?.let {
                        supportFragmentManager
                            .beginTransaction()
                            .remove(it)
                            .show(orderStep)
                            .commit()
                    }
                }
            })
        }.let { binding.model = it }

        replaceFragment(0)
    }

    @VisibleForTesting
    fun replaceFragment(position: Int) {
        hideLoader()
        supportFragmentManager.beginTransaction()
            .replace(R.id.aoFragmentFrame, fragments[position], "step")
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        Direct.releaseIdentitySubcomponent()
    }

    companion object {
        const val VIEWPAGER_ELEMENT_MARGIN = 10
    }
}
