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
import com.cexdirect.lib.ConfirmExitActivity
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib.databinding.ActivityCalcBinding
import com.cexdirect.lib.di.annotation.BuyActivityFactory
import com.cexdirect.lib.error.LastKnownOrderInfo
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.order.OrderActivity
import javax.inject.Inject

class CalcActivity : ConfirmExitActivity() {

    @field:[Inject BuyActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val model: CalcActivityViewModel by viewModelProvider { modelFactory }

    private val ratesObserver = messageObserver<List<ExchangeRate>>(
        onOk = { model.updateRates(it) },
        onFail = { purchaseFailed(it.message, LastKnownOrderInfo("", "", "", "", "")) }
    )

    private lateinit var binding: ActivityCalcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Direct.directComponent.inject(this)
        Direct.directComponent.socket().start()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calc)

        model.apply {
            applyLegalObservers()
            buyEvent.observe(this@CalcActivity, requestObserver(
                onOk = {
                    val intent = with(Intent(this@CalcActivity, OrderActivity::class.java)) {
                        model.extractMonetaryData { cryptoAmount, cryptoCurrency, fiatAmount, fiatCurrency ->
                            putExtra("cryptoAmount", cryptoAmount)
                            putExtra("crypto", cryptoCurrency)
                            putExtra("fiatAmount", fiatAmount)
                            putExtra("fiat", fiatCurrency)
                        }
                        this
                    }
                    unsubscribeFromExchangeRates()
                    startActivity(intent)
                    finish()
                },
                onFail = {
                    showStubScreen()
                }
            ))
            calcData.observe(this@CalcActivity, requestObserver(
                onOk = {
                    model.initPrecisions(it!!.first)
                    model.initRates(
                        it.second,
                        intent.getStringExtra("lastFiatAmount"),
                        intent.getStringExtra("lastFiatCurrency"),
                        intent.getStringExtra("lastCryptoCurrency")
                    ) { subscribeToExchangeRates().observe(this@CalcActivity, ratesObserver) }
                },
                onFail = { showStubScreen() }
            ))
            popularClickEvent.observe(this@CalcActivity, Observer {
                binding.acAmount.requestFocus()
                model.amount.fiatAmount = it
            })
            switchBaseCurrencyEvent.observe(this@CalcActivity, Observer {
                model.filterBaseCurrencies {
                    showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_BASE)
                }
            })
            switchQuoteCurrencyEvent.observe(this@CalcActivity, Observer {
                model.filterQuoteCurrencies {
                    showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_QUOTE)
                }
            })
        }.let {
            binding.model = it
            it.loadData()
        }
    }

    private fun showCurrencySelectionDialog(type: String) {
        PairSelectionBottomSheetDialog.create(type).show(supportFragmentManager, "selector")
    }

    companion object {

        @JvmStatic
        fun Context.startBuyActivity(data: AmountData? = null) {
            val intent = Intent(this, CalcActivity::class.java)
            // TODO: use @Parcelize here
            data?.let { (fiatAmount, fiatCurrency, cryptoCurrency) ->
                intent.apply {
                    putExtra("lastFiatAmount", fiatAmount)
                    putExtra("lastFiatCurrency", fiatCurrency)
                    putExtra("lastCryptoCurrency", cryptoCurrency)
                }
            }
            startActivity(intent)
        }
    }
}
