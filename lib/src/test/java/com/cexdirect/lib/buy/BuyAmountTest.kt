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

import com.cexdirect.lib.StringProvider
import com.cexdirect.lib._network.models.ExchangeRate
import com.cexdirect.lib._network.models.Precision
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BuyAmountTest {

    @Mock
    lateinit var stringProvider: StringProvider

    lateinit var buyAmount: BuyAmount

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(stringProvider.provideString(anyInt())).thenReturn("")

        buyAmount = BuyAmount(stringProvider)
    }

    @Test
    fun initData() {
        val spy = spy(buyAmount)

        spy.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
        }

        verify(spy).updateConverter()
        verify(spy).updateAmountBoundaries()
        verify(spy).convertToCrypto()

        assertThat(spy.converter).isNotNull()
        assertThat(spy.popularValues).containsOnlyElementsOf(listOf("100", "200", "500"))
        assertThat(spy)
                .hasFieldOrPropertyWithValue("minBoundary", "202.86")
                .hasFieldOrPropertyWithValue("maxBoundary", "1000.00")
                .hasFieldOrPropertyWithValue("fiatAmount", "")
                .hasFieldOrPropertyWithValue("cryptoAmount", "0.0000")
    }

    @Test
    fun convert200UsdToEth() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedCryptoCurrency = "ETH"
        }

        val spy = spy(buyAmount).apply {
            fiatAmount = "200"
        }

        verify(spy).convertToCrypto()
        verify(spy).updateCryptoBoundaryMessage()
        verify(spy, never()).convertToFiat()
        assertThat(spy.cryptoAmount).isEqualTo("0.221019")
    }

    @Test
    fun convert1EthToUsd() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedCryptoCurrency = "ETH"
            selectedFiatCurrency = "USD"
            inputMode = InputMode.CRYPTO
        }

        val spy = spy(buyAmount).apply {
            cryptoAmount = "1.000000"
        }

        verify(spy).convertToFiat()
        verify(spy).updateFiatBoundaryMessage()
        verify(spy, never()).convertToCrypto()
        assertThat(spy.fiatAmount).isEqualTo("874.38")
    }

    @Test
    fun hasCurrencies() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
        }

        val actual = buyAmount.hasCurrencies()

        assertThat(actual).isTrue()
    }

    @Test
    fun hasNoCurrencies() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = emptyList()
        }

        val actual = buyAmount.hasCurrencies()

        assertThat(actual).isFalse()
    }

    @Test
    fun returnPopularValuesForGivenPair() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedCryptoCurrency = "ETH"
        }

        val actual = buyAmount.currentPairPopularValues()

        assertThat(actual).containsAll(listOf("100", "200", "500"))
    }

    @Test
    fun returnEmptyPopularValuesForGivenPair() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedCryptoCurrency = "BCH"
        }

        val actual = buyAmount.currentPairPopularValues()

        assertThat(actual).isEmpty()
    }

    @Test
    fun changeFiatCurrency() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
        }

        val spy = spy(buyAmount)

        spy.apply {
            selectedFiatCurrency = "EUR"
        }

        verify(spy).updateFiatInputFilter()
        verify(spy).updateConverter()
        verify(spy).updateAmountBoundaries()
        verify(spy).convertToCrypto()
    }

    @Test
    fun changeCryptoCurrency() {
        buyAmount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
        }

        val spy = spy(buyAmount)

        spy.apply {
            selectedCryptoCurrency = "ETH"
        }

        verify(spy).updateCryptoInputFilter()
        verify(spy).updateConverter()
        verify(spy).updateAmountBoundaries()
        verify(spy).convertToCrypto()
    }

    private fun givenPrecisions() = listOf(
            Precision(
                    "crypto",
                    "ETH",
                    6,
                    6,
                    "trunk",
                    "0.01",
                    "0"
            ),
            Precision(
                    "fiat",
                    "USD",
                    2,
                    2,
                    "trunk",
                    "50",
                    "1000"
            ),
            Precision(
                    "fiat",
                    "EUR",
                    2,
                    2,
                    "trunk",
                    "50",
                    "1000"
            ),
            Precision(
                    "crypto",
                    "BTC",
                    4,
                    8,
                    "trunk",
                    "0.01",
                    "0"
            ),
            Precision(
                    "crypto",
                    "BCH",
                    4,
                    8,
                    "trunk",
                    "0.01",
                    "0"
            )
    )

    private fun givenRates() = listOf(
            ExchangeRate(
                    "USD",
                    "ETH",
                    0.0011550998408705918,
                    0.01,
                    1.0,
                    listOf("100", "200", "500"),
                    listOf("0.01", "0.05", "0.1")
            ),
            ExchangeRate(
                    "USD",
                    "BCH",
                    0.0008324855256099591,
                    0.001,
                    1.0,
                    emptyList(),
                    listOf("0.01", "0.05", "0.1")
            ),
            ExchangeRate(
                    "USD",
                    "BTC",
                    0.000051758739067181134,
                    0.0005,
                    1.0,
                    listOf("100", "200", "500"),
                    listOf("0.01", "0.05", "0.1")
            )
    )
}
