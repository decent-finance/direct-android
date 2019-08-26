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

import androidx.annotation.VisibleForTesting
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.cexdirect.lib.BR
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.R
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib._util.RateConverter
import com.cexdirect.lib._util.formatAmount
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.Precision

@OpenForTesting
class BuyAmount(private val stringProvider: StringProvider) : BaseObservable() {

    var inputMode: InputMode = InputMode.FIAT

    @get:Bindable
    var selectedFiatCurrency: String = "USD"
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.selectedFiatCurrency)
                updateFiatInputFilter()
                updateConverter()
                popularValues = currentPairPopularValues()
                updateAmountBoundaries()
                convertToCrypto()
            }
        }

    @get:Bindable
    var selectedCryptoCurrency: String = "BTC"
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.selectedCryptoCurrency)
                updateCryptoInputFilter()
                updateConverter()
                popularValues = currentPairPopularValues()
                updateAmountBoundaries()
                convertToCrypto()
            }
        }

    @get:Bindable
    var fiatAmount: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.fiatAmount)
            updateFiatBoundaryMessage()
            if (inputMode == InputMode.FIAT) convertToCrypto()
        }

    @get:Bindable
    var cryptoAmount: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.cryptoAmount)
            updateCryptoBoundaryMessage()
            if (inputMode == InputMode.CRYPTO) convertToFiat()
        }

    @get:Bindable
    var fiatBoundaryMessage: String = ""
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.fiatBoundaryMessage)
            }
        }

    @get:Bindable
    var cryptoBoundaryMessage: String = ""
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.cryptoBoundaryMessage)
            }
        }

    var precisionList = emptyList<Precision>()

    var rates: List<ExchangeRate> = emptyList()
        set(value) {
            field = value
            updateConverter()
            popularValues = currentPairPopularValues()
            updateAmountBoundaries()
            convertToCrypto()
        }

    @get:Bindable
    var popularValues = emptyList<String>()
        set(value) {
            field = value
            notifyPropertyChanged(BR.popularValues)
        }

    @get:Bindable
    var cryptoInputFilter: TradeInputFilter? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.cryptoInputFilter)
        }

    @get:Bindable
    var fiatInputFilter: TradeInputFilter? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.fiatInputFilter)
        }

    @VisibleForTesting
    internal var fiatMinBoundary = ""
    @VisibleForTesting
    internal var fiatMaxBoundary = ""
    @VisibleForTesting
    internal var cryptoMinBoundary = ""
    @VisibleForTesting
    internal var cryptoMaxBoundary = ""

    @VisibleForTesting
    internal var converter: RateConverter? = null

    @VisibleForTesting
    internal fun updateConverter() {
        findCurrentPair()?.let {
            converter = RateConverter(it.a, it.b, it.c)
        }
    }

    private fun findCurrentPair() = rates.find {
        it.fiat == selectedFiatCurrency && it.crypto == selectedCryptoCurrency
    }

    @VisibleForTesting
    internal fun convertToCrypto() {
        converter?.let {
            val convertedAmount = it.convertToCrypto(fiatAmount.amountToDouble())
            val precision = findPrecision(selectedCryptoCurrency)!!
            val formattedAmount = if (convertedAmount < 0) {
                0.0.formatAmount(precision)
            } else {
                convertedAmount.formatAmount(precision)
            }
            cryptoAmount = formattedAmount
        }
    }

    @VisibleForTesting
    internal fun convertToFiat() {
        converter?.let {
            val convertedAmount = it.convertToFiat(cryptoAmount.amountToDouble())
            val precision = findPrecision(selectedFiatCurrency)!!
            val formattedAmount = if (convertedAmount <= it.b / it.a) {
                0.0.formatAmount(precision)
            } else {
                convertedAmount.formatAmount(precision)
            }
            fiatAmount = formattedAmount
        }
    }

    @VisibleForTesting
    internal fun updateFiatInputFilter() {
        findPrecision(selectedFiatCurrency)!!.let {
            fiatInputFilter = TradeInputFilter(it.visiblePrecision)
        }
    }

    @VisibleForTesting
    internal fun updateCryptoInputFilter() {
        findPrecision(selectedCryptoCurrency)!!.let {
            cryptoInputFilter = TradeInputFilter(it.visiblePrecision)
        }
    }

    private fun findPrecision(givenCurrency: String) = precisionList.find { it.currency == givenCurrency }

    private fun String?.amountToDouble() = this?.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0

    @VisibleForTesting
    internal fun updateAmountBoundaries() {
        findCurrentPair()?.let { rate ->
            precisionList.find { it.currency == selectedFiatCurrency }!!
                    .let { fiatPrecision ->
                        fiatMinBoundary = fiatPrecision.minLimit
                        fiatMaxBoundary = fiatPrecision.maxLimit
                    }

            precisionList.find { it.currency == selectedCryptoCurrency }!!
                    .let { cryptoPrecision ->
                        cryptoMinBoundary = cryptoPrecision.minLimit
                        cryptoMaxBoundary = if (cryptoPrecision.maxLimit.toDouble() <= 0.0) {
                            Double.MAX_VALUE.toString()
                        } else {
                            cryptoPrecision.maxLimit
                        }
                    }
        }
    }

    @VisibleForTesting
    internal fun updateFiatBoundaryMessage() {
        fiatBoundaryMessage = fiatAmount.takeIf { it.isNotBlank() }?.toDouble()?.let { value ->
            when {
                value < fiatMinBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 ->
                    stringProvider.provideString(R.string.cexd_min_amount, fiatMinBoundary)
                value > fiatMaxBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 ->
                    stringProvider.provideString(R.string.cexd_max_amount, fiatMaxBoundary)
                else -> ""
            }
        } ?: stringProvider.provideString(R.string.cexd_please_enter_amount)
    }

    @VisibleForTesting
    internal fun updateCryptoBoundaryMessage() {
        cryptoBoundaryMessage = cryptoAmount.takeIf { it.isNotBlank() }?.toDouble()?.let { value ->
            when {
                value < cryptoMinBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 ->
                    stringProvider.provideString(R.string.cexd_min_sale, cryptoMinBoundary)
                value > cryptoMaxBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 ->
                    stringProvider.provideString(R.string.cexd_max_sale, cryptoMaxBoundary)
                else -> ""
            }
        } ?: stringProvider.provideString(R.string.cexd_please_enter_amount)
    }

    fun currentPairPopularValues() = findCurrentPair()?.fiatPopularValues ?: emptyList()
    fun hasCurrencies() = rates.isNotEmpty()
}
