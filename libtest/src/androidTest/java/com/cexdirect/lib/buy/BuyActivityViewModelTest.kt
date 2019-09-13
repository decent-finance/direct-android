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

import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib.MockCoroutineDispatcherProvider
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.network.AnalyticsApi
import com.cexdirect.lib.network.MerchantApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.Precision
import com.cexdirect.lib.network.ws.Messenger
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BuyActivityViewModelTest {

    @Mock
    lateinit var merchantApi: MerchantApi

    @Mock
    lateinit var paymentApi: PaymentApi

    @Mock
    lateinit var analyticsApi: AnalyticsApi

    @Mock
    lateinit var messenger: Messenger

    private lateinit var model: BuyActivityViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val stringProvider =
            StringProvider(InstrumentationRegistry.getInstrumentation().targetContext)
        model = BuyActivityViewModel(
            mock(),
            mock(),
            mock(),
            mock(),
            MockCoroutineDispatcherProvider(),
            stringProvider
        )
    }

    @After
    fun tearDown() {
        reset(merchantApi, paymentApi, analyticsApi, messenger)
    }

    @Test
    fun filterBaseCurrenciesForUsd() {
        model.amount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedCryptoCurrency = "BTC"
        }

        model.filterBaseCurrencies { }

        assertThat(model.currencyAdapter.selectedCurrency).isEqualTo("BTC")
        assertThat(model.currencyAdapter.items).containsOnly("BTC", "BCH", "ETH")
    }

    @Test
    fun filterBaseCurrenciesForEur() {
        model.amount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedFiatCurrency = "EUR"
        }

        model.filterBaseCurrencies { }

        assertThat(model.currencyAdapter.selectedCurrency).isEqualTo("BTC")
        assertThat(model.currencyAdapter.items).containsOnly("BTC")
    }

    @Test
    fun filterQuoteCurrencies() {
        model.amount.apply {
            precisionList = givenPrecisions()
            rates = givenRates()
            selectedFiatCurrency = "EUR"
        }

        model.filterQuoteCurrencies { }

        assertThat(model.currencyAdapter.selectedCurrency).isEqualTo("EUR")
        assertThat(model.currencyAdapter.items).containsOnly("EUR", "USD")
    }

    @Test
    fun initRatesAndSetFavoriteValue() {
        model.amount.apply {
            precisionList = givenPrecisions()
        }

        model.initRates(givenRates()) { }

        assertThat(model.amount.rates).containsOnlyElementsOf(givenRates())
        assertThat(model.amount.fiatAmount).isEqualTo("250")
        assertThat(model.amount.inputMode).isEqualTo(InputMode.FIAT)
        assertThat(model.dataLoaded.get()).isTrue()
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
        ),
        ExchangeRate(
            "EUR",
            "BTC",
            0.00010585050029429076,
            0.0005,
            1.0,
            listOf("50", "100", "200"),
            listOf("0.01", "0.05", "0.1")
        )
    )
}
