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

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.*
import com.cexdirect.lib._network.AnalyticsApi
import com.cexdirect.lib._network.MerchantApi
import com.cexdirect.lib._network.PaymentApi
import com.cexdirect.lib._network.enqueueWith
import com.cexdirect.lib._network.models.EventData
import com.cexdirect.lib._network.models.ExchangeRate
import com.cexdirect.lib._network.models.MonetaryData
import com.cexdirect.lib._network.models.Precision
import com.cexdirect.lib._network.ws.Messenger
import com.cexdirect.lib._util.RateConverter
import com.cexdirect.lib._util.formatAmount
import kotlin.math.max
import kotlin.math.min

@OpenForTesting
class BuyActivityViewModel(
    merchantApi: MerchantApi,
    paymentApi: PaymentApi,
    private val analyticsApi: AnalyticsApi,
    private val messenger: Messenger,
    dispatcherProvider: CoroutineDispatcherProvider
) : LegalViewModel(dispatcherProvider) {

    val shouldShowCryptoInput = ObservableBoolean(false)
    val buyAmount = ObservableField("")
    val buyAmountFocused = ObservableBoolean(true)
    val buyCryptoAmount = ObservableField("")
    val buyCryptoAmountFocused = ObservableBoolean(false)
    val selectedFiatCurrency = ObservableField("USD")
    val selectedCryptoCurrency = ObservableField("BTC")
    val boundaryMessage = ObservableField("")
    val dataLoaded = ObservableBoolean(false)

    val buyCryptoEvent = BuyCryptoEvent()
    val popularClickEvent = ClickEvent()
    val switchBaseCurrencyEvent = SwitchCurrencyEvent()
    val switchQuoteCurrencyEvent = SwitchCurrencyEvent()
    val closeSelectorEvent = CloseSelectorEvent()
    val currencyClickEvent = ClickEvent()

    val cryptoInputFilter = ObservableField<TradeInputFilter>()
    val fiatInputFilter = ObservableField<TradeInputFilter>()

    val popularValues = ObservableField(emptyList<String>())

    val currencyAdapter = CurrencyAdapter(currencyClickEvent)

    private var minBoundary = ""
    private var maxBoundary = ""

    private val precisions = merchantApi.getCurrencyPrecisions(this, Direct.credentials.placementId)
        .apply { execute() }

    @Suppress("NestedLambdaShadowedImplicitParameter")
    val currencies = Transformations.switchMap(precisions) {
        it.enqueueWith({
            it.data.let {
                precisionList = it!!
                precisionList.find { it.currency == selectedFiatCurrency.get() }?.let {
                    fiatInputFilter.set(TradeInputFilter(it.visiblePrecision))
                }
                precisionList.findLast { it.currency == selectedCryptoCurrency.get() }?.let {
                    cryptoInputFilter.set(TradeInputFilter(it.visiblePrecision))
                }

            }
            paymentApi.getExchangeRates(this).apply { execute() }
        })
    }!!

    private var rates: List<ExchangeRate> = emptyList()

    val sendBuyEvent = analyticsApi.sendBuyEvent(this) {
        EventData(
            fiat = MonetaryData(buyAmount.get()!!, selectedFiatCurrency.get()!!),
            crypto = MonetaryData(buyCryptoAmount.get()!!, selectedCryptoCurrency.get()!!)
        )
    }

    private var precisionList = emptyList<Precision>()

    init {
        buyAmount.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (buyAmountFocused.get()) {
                    convertToCrypto(buyAmount.get() ?: "")?.takeIf { it.isNotBlank() }?.let {
                        if (buyCryptoAmount.get() != it) buyCryptoAmount.set(it)
                    }
                }
                boundaryMessage.set(getBoundaryMessage(buyAmount.get()!!))
            }
        })
        buyCryptoAmount.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (buyCryptoAmountFocused.get()) {
                    convertToFiat(buyCryptoAmount.get() ?: "")?.takeIf { it.isNotBlank() }?.let {
                        if (buyAmount.get() != it) buyAmount.set(it)
                        boundaryMessage.set(getBoundaryMessage(it))
                    }
                }
            }
        })
    }

    fun sendOpenEvent() {
        analyticsApi.sendOpenEvent(this).apply { execute() }
    }

    fun buyCrypto() {
        buyCryptoEvent.call()
    }

    fun convertToCrypto(value: String) =
        findCurrentPair()!!.let {
            precisionList.find { precision ->
                precision.currency == selectedCryptoCurrency.get()
            }?.let { precision ->
                val amount = value.takeIf { it.isNotBlank() }?.toDouble()
                    ?: 0.0
                val converted = RateConverter(it.a, it.b, it.c).convert(amount)

                if (converted < 0) {
                    0.0.formatAmount(precision)
                } else {
                    converted.formatAmount(precision)
                }
            }
        }

    fun convertToFiat(value: String) =
        findCurrentPair()!!.let {
            precisionList.find { precision ->
                precision.currency == selectedFiatCurrency.get()
            }?.let { precision ->
                val amount = value.takeIf { it.isNotBlank() }?.toDouble()
                    ?: 0.0
                val converted = RateConverter(it.a, it.b, it.c).convertToFiat(amount)

                if (converted <= it.b / it.a) {
                    0.0.formatAmount(precision)
                } else {
                    converted.formatAmount(precision)
                }
            }
        }

    fun enableCryptoInput() {
        shouldShowCryptoInput.set(true)
    }

    fun changeBaseCurrency() {
        if (hasCurrencies()) switchBaseCurrencyEvent.call()
    }

    fun changeQuoteCurrency() {
        if (hasCurrencies()) switchQuoteCurrencyEvent.call()
    }

    private fun hasCurrencies() = rates.isNotEmpty()

    fun closeDialog() {
        closeSelectorEvent.call()
    }

    private fun updatePopularValues() {
        findCurrentPair()
            ?.let {
                popularValues.set(it.fiatPopularValues)
            } ?: popularValues.set(emptyList())
    }

    private fun findCurrentPair() = rates.find {
        it.fiat == selectedFiatCurrency.get() && it.crypto == selectedCryptoCurrency.get()
    }

    private fun updateAmountBoundaries() {
        findCurrentPair()?.let { rate ->
            val converter = RateConverter(rate.a, rate.b, rate.c)
            val fiatPrecision = precisionList.find { it.currency == selectedFiatCurrency.get() }
            val cryptoPrecision = precisionList.find { it.currency == selectedCryptoCurrency.get() }
            if (fiatPrecision != null && cryptoPrecision != null) {
                val minLimit = converter.convertToFiat(cryptoPrecision.minLimit.toDouble())
                minBoundary =
                    max(minLimit, fiatPrecision.minLimit.toDouble()).formatAmount(fiatPrecision)

                val cryptoMaxLimit = cryptoPrecision.maxLimit.toDouble()
                val maxLimit =
                    if (cryptoMaxLimit > 0) converter.convertToFiat(cryptoMaxLimit) else Double.POSITIVE_INFINITY
                maxBoundary =
                    min(maxLimit, fiatPrecision.maxLimit.toDouble()).formatAmount(fiatPrecision)
            }
        }
    }

    private fun getBoundaryMessage(amount: String) =
        amount.takeIf { it.isNotBlank() }?.toDouble()?.let { value ->
            when {
                value < minBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 -> return@let "Min amount $minBoundary"
                value > maxBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 -> return@let "Max amount $maxBoundary"
                else -> ""
            }
        } ?: ""

    fun updateCurrencySelection(focusField: () -> ObservableBoolean) {
        rates.find { it.crypto == selectedCryptoCurrency.get() && it.fiat == selectedFiatCurrency.get() }
            ?: rates.find { it.fiat == selectedFiatCurrency.get() }?.let {
                selectedCryptoCurrency.set(it.crypto)
            }
        focusField.invoke().set(true)
        val cryptoAmount = convertToCrypto(buyAmount.get() ?: "")
        buyCryptoAmount.set(cryptoAmount)
        updatePopularValues()
        updateAmountBoundaries()
        boundaryMessage.set(getBoundaryMessage(buyAmount.get() ?: ""))
    }

    fun subscribeToExchangeRates() = messenger.subscribeToExchangeRates()

    fun extractMonetaryData(block: (cryptoAmount: String, cryptoCurrency: String, fiatAmount: String, fiatCurrency: String) -> Unit) {
        block.invoke(
            buyCryptoAmount.get()!!,
            selectedCryptoCurrency.get()!!,
            buyAmount.get()!!,
            selectedFiatCurrency.get()!!
        )
    }

    fun filterBaseCurrencies(action: () -> Unit) {
        currencyAdapter.items =
            rates.distinctBy { it.crypto }.filter { it.fiat == selectedFiatCurrency.get() }
                .map { it.crypto }
        action.invoke()
    }

    fun filterQuoteCurrencies(action: () -> Unit) {
        currencyAdapter.items = rates.distinctBy { it.fiat }.map { it.fiat }
        action.invoke()
    }

    fun initRates(data: List<ExchangeRate>, action: () -> Unit) {
        rates = data
        data.find { it.fiat == selectedFiatCurrency.get() }
            ?.let {
                buyAmount.set(it.fiatPopularValues.first())
                buyAmountFocused.set(true)
            }
        updatePopularValues()
        updateAmountBoundaries()
        dataLoaded.set(true)
        action.invoke()
    }

    fun updateRates(data: List<ExchangeRate>) {
        rates = data
        convertToCrypto(buyAmount.get() ?: "")
        updateAmountBoundaries()
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
        private val dispatcherProvider: CoroutineDispatcherProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BuyActivityViewModel(merchantApi, paymentApi, analyticsApi, messenger, dispatcherProvider) as T
    }
}


class BuyCryptoEvent : SingleLiveEvent<Void>()
class SwitchCurrencyEvent : SingleLiveEvent<Void>()
class CloseSelectorEvent : SingleLiveEvent<Void>()
