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

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.MonetaryData
import com.cexdirect.lib.network.models.Precision
import com.cexdirect.livedatax.switchMap
import com.cexdirect.livedatax.throttleFirst
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.concurrent.TimeUnit

@FlowPreview
@ExperimentalCoroutinesApi
@OpenForTesting
class CalcActivityViewModel(
    private val api: CalcApi,
    stringProvider: StringProvider
) : LegalViewModel() {

    val amount = BuyAmount(stringProvider)

    val shouldShowCryptoInput = ObservableBoolean(false)
    val dataLoaded = ObservableBoolean(false)

    val popularClickEvent = StringLiveEvent()
    val switchBaseCurrencyEvent = VoidLiveEvent()
    val switchQuoteCurrencyEvent = VoidLiveEvent()
    val closeSelectorEvent = VoidLiveEvent()
    private final val buyClickEvent = VoidLiveEvent()

    final val currencyClickEvent = StringLiveEvent()
    val currencyAdapter = CurrencyAdapter(currencyClickEvent)

    val calcData = api.calcData
    val buyEvent = buyClickEvent
        .throttleFirst(BuildConfig.THROTTLE_DELAY_MILLIS, TimeUnit.MILLISECONDS)
        .switchMap {
            api.sendBuyEvent(
                this,
                EventData(
                    fiat = MonetaryData(amount.fiatAmount, amount.selectedFiatCurrency),
                    crypto = MonetaryData(amount.cryptoAmount, amount.selectedCryptoCurrency)
                )
            )
        }

    fun loadData() {
        api.loadCalcData(this)
    }

    fun buyCrypto() {
        buyClickEvent.call()
    }

    fun enableCryptoInput() {
        shouldShowCryptoInput.set(true)
    }

    fun changeBaseCurrency() {
        if (amount.hasCurrencies()) switchBaseCurrencyEvent.call()
    }

    fun changeQuoteCurrency() {
        if (amount.hasCurrencies()) switchQuoteCurrencyEvent.call()
    }

    fun closeDialog() {
        closeSelectorEvent.call()
    }

    fun setSelectedCryptoCurrency(currency: String) {
        amount.selectedCryptoCurrency = currency
    }

    fun setSelectedFiatCurrency(currency: String) {
        amount.selectedFiatCurrency = currency
    }

    fun subscribeToExchangeRates() = api.subscribeToExchangeRates()

    fun unsubscribeFromExchangeRates() {
        api.unsubscribeFromExchangeRates()
    }

    fun extractMonetaryData(block: (cryptoAmount: String, cryptoCurrency: String, fiatAmount: String, fiatCurrency: String) -> Unit) {
        block.invoke(
            amount.cryptoAmount,
            amount.selectedCryptoCurrency,
            amount.fiatAmount,
            amount.selectedFiatCurrency
        )
    }

    fun filterBaseCurrencies(action: () -> Unit) {
        currencyAdapter.items = amount.rates
            .filter { it.fiat == amount.selectedFiatCurrency }
            .distinctBy { it.crypto }
            .map { it.crypto }
        currencyAdapter.selectedCurrency = amount.selectedCryptoCurrency
        action.invoke()
    }

    fun filterQuoteCurrencies(action: () -> Unit) {
        currencyAdapter.items = amount.rates
            .distinctBy { it.fiat }
            .map { it.fiat }
        currencyAdapter.selectedCurrency = amount.selectedFiatCurrency
        action.invoke()
    }

    fun initPrecisions(precisions: List<Precision>) {
        amount.precisionList = precisions
        amount.precisionList.find { it.currency == amount.selectedFiatCurrency }
            ?.let { amount.fiatInputFilter = TradeInputFilter(it.visiblePrecision) }
        amount.precisionList.findLast { it.currency == amount.selectedCryptoCurrency }
            ?.let { amount.cryptoInputFilter = TradeInputFilter(it.visiblePrecision) }
    }

    fun initRates(
        data: List<ExchangeRate>,
        lastFiatAmount: String?,
        lastFiatCurrency: String?,
        lastCryptoCurrency: String?,
        action: () -> Unit
    ) {
        amount.rates = data
        amount.fiatAmount = lastFiatAmount ?: "250" // for now, this value is hardcoded
        lastFiatCurrency?.let { amount.selectedFiatCurrency = it }
        lastCryptoCurrency?.let { amount.selectedCryptoCurrency = it }
        amount.inputMode = InputMode.FIAT
        dataLoaded.set(true)
        action.invoke()
    }

    fun updateRates(data: List<ExchangeRate>) {
        amount.rates = data
    }

    class Factory(private val api: CalcApi, private val stringProvider: StringProvider) :
        ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalcActivityViewModel(api, stringProvider) as T
    }
}
