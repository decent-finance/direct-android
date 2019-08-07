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

package com.cexdirect.lib.buy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.BuyActivityFactory
import com.cexdirect.lib._network.Failure
import com.cexdirect.lib._network.Loading
import com.cexdirect.lib._network.Resource
import com.cexdirect.lib._network.Success
import com.cexdirect.lib._network.models.ExchangeRate
import com.cexdirect.lib._network.models.MonetaryData
import com.cexdirect.lib.databinding.ActivityBuyBinding
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.verification.VerificationActivity
import javax.inject.Inject

class BuyActivity : BaseActivity() {

    @field:[Inject BuyActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: BuyActivityViewModel by viewModelProvider { modelFactory }

    private val ratesObserver = Observer<Resource<List<ExchangeRate>?>> {
        when (it) {
            is Success -> model.updateRates(it.data!!)
            is Failure -> purchaseFailed(it.message)
        }
    }

    private lateinit var binding: ActivityBuyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        Direct.directComponent.socket().start()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_buy)

        model.apply {
            applyLegalObservers()
            buyCryptoEvent.observe(this@BuyActivity, Observer { model.sendBuyEvent.execute() })
            sendBuyEvent.observe(this@BuyActivity, Observer {
                if (it !is Loading) {
                    model.extractMonetaryData { cryptoAmount, cryptoCurrency, fiatAmount, fiatCurrency ->
                        Direct.setPendingAmounts(
                                crypto = MonetaryData(cryptoAmount, cryptoCurrency),
                                fiat = MonetaryData(fiatAmount, fiatCurrency)
                        )
                    }
                    val intent = with(Intent(this@BuyActivity, VerificationActivity::class.java)) {
                        model.extractMonetaryData { cryptoAmount, cryptoCurrency, fiatAmount, fiatCurrency ->
                            putExtra("cryptoAmount", cryptoAmount)
                            putExtra("crypto", cryptoCurrency)
                            putExtra("fiatAmount", fiatAmount)
                            putExtra("fiat", fiatCurrency)
                        }
                        this
                    }
                    startActivity(intent)
                    finish()
                } else {
                    showLoader()
                }
            })
            currencies.observe(this@BuyActivity, Observer {
                when (it) {
                    is Loading -> showLoader()
                    is Success -> model.initRates(it.data!!) {
                        hideLoader()
                        subscribeToExchangeRates().observe(this@BuyActivity, ratesObserver)
                    }
                    is Failure -> purchaseFailed(it.message)
                }
            })
            popularClickEvent.observe(this@BuyActivity, Observer {
                binding.abAmount.requestFocus()
                model.amount.fiatAmount = it
            })
            switchBaseCurrencyEvent.observe(this@BuyActivity, Observer {
                model.filterBaseCurrencies { showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_BASE) }
            })
            switchQuoteCurrencyEvent.observe(this@BuyActivity, Observer {
                model.filterQuoteCurrencies { showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_QUOTE) }
            })
        }.let {
            binding.model = it
            it.sendOpenEvent()
            it.loadData()
        }
    }

    private fun showCurrencySelectionDialog(type: String) {
        PairSelectionBottomSheetDialog.create(type).show(supportFragmentManager, "selector")
    }
}

fun Context.startBuyActivity() {
    startActivity(Intent(this, BuyActivity::class.java))
}
