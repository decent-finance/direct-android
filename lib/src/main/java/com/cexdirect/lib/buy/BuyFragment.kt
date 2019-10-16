/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.R
import com.cexdirect.lib._di.annotation.BuyActivityFactory
import com.cexdirect.lib.databinding.ActivityBuyBinding
import com.cexdirect.lib.error.purchaseFailed
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.verification.VerificationFragmentArgs
import javax.inject.Inject

class BuyFragment : BaseFragment() {

    @field:[Inject BuyActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    private val args by navArgs<BuyFragmentArgs>()

    private val model: BuyActivityViewModel by activityViewModelProvider { modelFactory }

    private val ratesObserver = Observer<Resource<List<ExchangeRate>?>> {
        when (it) {
            is Success -> model.updateRates(it.data!!)
            is Failure -> findNavController().purchaseFailed(it.message)
        }
    }

    private lateinit var binding: ActivityBuyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DataBindingUtil.inflate<ActivityBuyBinding>(
        inflater,
        R.layout.activity_buy,
        container,
        false
    ).apply { binding = this }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Direct.directComponent.inject(this)
        Direct.directComponent.socket().start()
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
        model.apply {
            applyLegalObservers()
            buyCryptoEvent.observe(this@BuyFragment, Observer { model.sendBuyEvent.execute() })
            sendBuyEvent.observe(this@BuyFragment, Observer {
                if (it !is Loading) {
                    hideLoader()
                    model.extractMonetaryData { cryptoAmount, cryptoCurrency, fiatAmount, fiatCurrency ->
                        val args = VerificationFragmentArgs(
                            cryptoCurrency,
                            cryptoAmount,
                            fiatCurrency,
                            fiatAmount
                        ).toBundle()
                        findNavController().navigate(
                            R.id.action_buyFragment_to_verificationFragment,
                            args
                        )
                    }
                } else {
                    showLoader()
                }
            })
            currencies.observe(this@BuyFragment, Observer {
                when (it) {
                    is Loading -> showLoader()
                    is Success -> model.initRates(
                        it.data!!,
                        args.lastFiatAmount,
                        args.lastFiatCurrency,
                        args.lastCryptoCurrency
                    ) {
                        hideLoader()
                        subscribeToExchangeRates().observe(this@BuyFragment, ratesObserver)
                    }
                    is Failure -> showStubScreen()
                }
            })
            popularClickEvent.observe(this@BuyFragment, Observer {
                binding.abAmount.requestFocus()
                model.amount.fiatAmount = it
            })
            switchBaseCurrencyEvent.observe(this@BuyFragment, Observer {
                model.filterBaseCurrencies {
                    showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_BASE)
                }
            })
            switchQuoteCurrencyEvent.observe(this@BuyFragment, Observer {
                model.filterQuoteCurrencies {
                    showCurrencySelectionDialog(PairSelectionBottomSheetDialog.TYPE_QUOTE)
                }
            })
        }.let {
            binding.model = it
            it.sendOpenEvent()
            it.loadData()
        }
    }

    private fun showCurrencySelectionDialog(type: String) {
        PairSelectionBottomSheetDialog.create(type).show(childFragmentManager, "selector")
    }

    private fun showStubScreen() {
        hideLoader()
        findNavController().navigate(R.id.action_buyFragment_to_stubFragment)
    }
}
