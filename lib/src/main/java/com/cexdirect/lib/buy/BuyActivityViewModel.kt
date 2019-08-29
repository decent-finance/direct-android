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
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib.network.AnalyticsApi
import com.cexdirect.lib.network.MerchantApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.enqueueWith
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.MonetaryData
import com.cexdirect.lib.network.ws.Messenger

@OpenForTesting
class BuyActivityViewModel(
    merchantApi: MerchantApi,
    paymentApi: PaymentApi,
    private val analyticsApi: AnalyticsApi,
    private val messenger: Messenger,
    dispatcherProvider: CoroutineDispatcherProvider,
    stringProvider: StringProvider
) : LegalViewModel(dispatcherProvider) {

    val amount = BuyAmount(stringProvider)

    val shouldShowCryptoInput = ObservableBoolean(false)
    val dataLoaded = ObservableBoolean(false)

    val buyCryptoEvent = VoidLiveEvent()
    val popularClickEvent = StringLiveEvent()
    val switchBaseCurrencyEvent = VoidLiveEvent()
    val switchQuoteCurrencyEvent = VoidLiveEvent()
    val closeSelectorEvent = VoidLiveEvent()

    final val currencyClickEvent = StringLiveEvent()
    val currencyAdapter = CurrencyAdapter(currencyClickEvent)

    private val precisions =
        merchantApi.getCurrencyPrecisions(this, Direct.credentials.placementId)

    @Suppress("NestedLambdaShadowedImplicitParameter")
    val currencies = Transformations.switchMap(precisions) {
        it.enqueueWith({
            it.data.let {
                amount.precisionList = it!!
                amount.precisionList.find { it.currency == amount.selectedFiatCurrency }?.let {
                    amount.fiatInputFilter = TradeInputFilter(it.visiblePrecision)
                }
                amount.precisionList.findLast { it.currency == amount.selectedCryptoCurrency }?.let {
                    amount.cryptoInputFilter = TradeInputFilter(it.visiblePrecision)
                }

            }
            paymentApi.getExchangeRates(this).apply { execute() }
        })
    }!!

    val sendBuyEvent = analyticsApi.sendBuyEvent(this) {
        EventData(
            fiat = MonetaryData(amount.fiatAmount, amount.selectedFiatCurrency),
            crypto = MonetaryData(amount.cryptoAmount, amount.selectedCryptoCurrency)
        )
    }

    fun loadData() {
        precisions.execute()
    }

    fun sendOpenEvent() {
        analyticsApi.sendOpenEvent(this).apply { execute() }
    }

    fun buyCrypto() {
        buyCryptoEvent.call()
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

    fun subscribeToExchangeRates() = messenger.subscribeToExchangeRates()

    fun extractMonetaryData(
        block: (
            cryptoAmount: String,
            cryptoCurrency: String,
            fiatAmount: String,
            fiatCurrency: String
        ) -> Unit
    ) {
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

    fun initRates(data: List<ExchangeRate>, action: () -> Unit) {
        amount.rates = data
        amount.fiatAmount = "250" // for now, this value is hardcoded
        amount.inputMode = InputMode.FIAT
        dataLoaded.set(true)
        action.invoke()
    }

    fun updateRates(data: List<ExchangeRate>) {
        amount.rates = data
    }

    override fun onCleared() {
        super.onCleared()
        messenger.removeExchangesSubscription()
    }

    class Factory(
        private val merchantApi: MerchantApi,
        private val paymentApi: PaymentApi,
        private val analyticsApi: AnalyticsApi,
        private val messenger: Messenger,
        private val dispatcherProvider: CoroutineDispatcherProvider,
        private val stringProvider: StringProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BuyActivityViewModel(
                merchantApi,
                paymentApi,
                analyticsApi,
                messenger,
                dispatcherProvider,
                stringProvider
            ) as T
    }
}
