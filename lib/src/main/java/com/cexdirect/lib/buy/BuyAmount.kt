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
import com.cexdirect.lib._network.models.ExchangeRate
import com.cexdirect.lib._network.models.Precision
import com.cexdirect.lib._util.RateConverter
import com.cexdirect.lib._util.formatAmount
import kotlin.math.max
import kotlin.math.min

@OpenForTesting
class BuyAmount : BaseObservable() {

    var inputMode: InputMode = InputMode.FIAT

    @get:Bindable
    var selectedFiatCurrency: String = "USD"
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.selectedFiatCurrency)
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
            updateBoundaryMessage()
            if (inputMode == InputMode.FIAT) convertToCrypto()
        }

    @get:Bindable
    var cryptoAmount: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.cryptoAmount)
            if (inputMode == InputMode.CRYPTO) convertToFiat()
        }

    @get:Bindable
    var boundaryMessage: String = ""
        set(value) {
            if (value != field) {
                field = value
                notifyPropertyChanged(BR.boundaryMessage)
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

    @VisibleForTesting
    internal var minBoundary = ""
    @VisibleForTesting
    internal var maxBoundary = ""

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

    private fun findPrecision(givenCurrency: String) = precisionList.find { it.currency == givenCurrency }

    private fun String?.amountToDouble() = this?.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0

    @VisibleForTesting
    internal fun updateAmountBoundaries() {
        findCurrentPair()?.let { rate ->
            val converter = RateConverter(rate.a, rate.b, rate.c)

            val fiatPrecision = precisionList.find { it.currency == selectedFiatCurrency }!!
            val cryptoPrecision = precisionList.find { it.currency == selectedCryptoCurrency }!!

            val minLimit = converter.convertToFiat(cryptoPrecision.minLimit.toDouble())
            minBoundary = max(minLimit, fiatPrecision.minLimit.toDouble()).formatAmount(fiatPrecision)

            val cryptoMaxLimit = cryptoPrecision.maxLimit.toDouble()
            val maxLimit = if (cryptoMaxLimit > 0) converter.convertToFiat(cryptoMaxLimit) else Double.POSITIVE_INFINITY
            maxBoundary = min(maxLimit, fiatPrecision.maxLimit.toDouble()).formatAmount(fiatPrecision)
        }
    }

    private fun updateBoundaryMessage() {
        boundaryMessage = fiatAmount.takeIf { it.isNotBlank() }?.toDouble()?.let { value ->
            when {
                value < minBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 -> return@let "Min amount $minBoundary"
                value > maxBoundary.takeIf { it.isNotBlank() }?.toDouble() ?: 0.0 -> return@let "Max amount $maxBoundary"
                else -> ""
            }
        } ?: ""
    }

    fun currentPairPopularValues() = findCurrentPair()?.fiatPopularValues ?: emptyList()
    fun hasCurrencies() = rates.isNotEmpty()
}
