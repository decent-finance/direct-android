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

package com.cexdirect.lib.verification

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.VerificationActivityFactory
import com.cexdirect.lib.buy.startBuyActivity
import com.cexdirect.lib.databinding.ActivityVerificationBinding
import com.cexdirect.lib.verification.confirmation.PaymentConfirmationFragment
import com.cexdirect.lib.verification.events.StickyViewEvent
import com.cexdirect.lib.verification.identity.IdentityFragment
import com.cexdirect.lib.verification.receipt.ReceiptFragment
import com.mcxiaoke.koi.ext.toast
import javax.inject.Inject

class VerificationActivity : BaseActivity() {

    @field:[Inject VerificationActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var stickyViewEvent: StickyViewEvent

    private val model: VerificationActivityViewModel by viewModelProvider { modelFactory }
    private val fragments =
        listOf(IdentityFragment(), PaymentConfirmationFragment(), ReceiptFragment())

    private lateinit var binding: ActivityVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.identitySubcomponent?.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification)
        binding.avStepsViewPager.pageMargin = VIEWPAGER_ELEMENT_MARGIN

        model.apply {
            intent.let {
                orderAmounts.selectedCryptoCurrency = it.getStringExtra("crypto")
                orderAmounts.selectedCryptoAmount = it.getStringExtra("cryptoAmount")
                orderAmounts.selectedFiatCurrency = it.getStringExtra("fiat")
                orderAmounts.selectedFiatAmount = it.getStringExtra("fiatAmount")
            }
            applyLegalObservers()
            nextClickEvent.observe(this@VerificationActivity, Observer {
                model.proceed()
                replaceFragment(model.currentStep.get() - 1)
            })
            returnEvent.observe(this@VerificationActivity, Observer {
                startBuyActivity()
                finish()
            })
            copyEvent.observe(this@VerificationActivity, Observer { orderId ->
                if (!orderId.isNullOrBlank()) {
                    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip =
                        ClipData.newPlainText(getString(R.string.cexd_order_id_label), orderId)
                    toast(getString(R.string.cexd_order_id_copied))
                }
            })
        }.let {
            binding.model = it
        }

        stickyViewEvent.observe(this, Observer { binding.avScroll.initFooterView(it) })
        replaceFragment(0)
    }

    private fun replaceFragment(position: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.avFragmentFrame, fragments[position])
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
