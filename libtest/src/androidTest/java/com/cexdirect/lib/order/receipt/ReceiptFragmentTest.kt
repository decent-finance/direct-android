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

package com.cexdirect.lib.order.receipt

import android.content.ClipboardManager
import android.widget.ProgressBar
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.CalcActivity
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.order.OrderActivityViewModel
import com.cexdirect.lib.order.OrderProcessingApi
import com.cexdirect.lib.util.isToast
import com.cexdirect.lib.util.symbolMap
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Java6Assertions.assertThat
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import java.util.*

class ReceiptFragmentTest {

    @get:Rule
    val networkMockRule = DirectNetworkMockRule()

    @Mock
    lateinit var api: OrderProcessingApi

    private val orderInfo = MutableLiveData<Resource<OrderInfoData>>()

    private lateinit var scenario: FragmentScenario<ReceiptFragment>

    @Before
    fun setUp() {
        Intents.init()
        whenever(api.subscribeToOrderInfo()).thenReturn(orderInfo)
        whenever(api.newOrderResult).thenReturn(mock())
        whenever(api.verificationResult).thenReturn(mock())
        whenever(api.processingResult).thenReturn(mock())
        whenever(api.uploadResult).thenReturn(mock())
        whenever(api.basePaymentDataResult).thenReturn(mock())
        whenever(api.extraPaymentDataResult).thenReturn(mock())
        whenever(api.checkCode).thenReturn(mock())
        scenario = launchFragmentInContainer(
            themeResId = R.style.Direct, instantiate = { ReceiptFragment() }
        )
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun displayPaymentInfo() {
        scenario.onFragment {
            it.model.paymentInfo.set(givenPaymentInfo())
        }

        onView(
            allOf(
                withText("0.123 BTC"),
                hasSibling(withText(R.string.cexd_bought))
            )
        ).check(matches(isDisplayed()))

        onView(
            allOf(
                withText("100 USD"),
                hasSibling(withText(R.string.cexd_for))
            )
        ).check(matches(isDisplayed()))
    }

    @Test
    fun showProgressBar() {
        scenario.onFragment {
            it.model.paymentInfo.set(givenPaymentInfo())
        }

        onView(isAssignableFrom(ProgressBar::class.java)).check(matches(isDisplayed()))
    }

    @Test
    fun showTxId() {
        scenario.onFragment {
            it.model.paymentInfo.set(givenPaymentInfo())
            it.model.txId.set(TX_ID)
        }

        onView(withText(TX_ID)).check(matches(isDisplayed()))
        onView(isAssignableFrom(ProgressBar::class.java)).check(matches(not(isDisplayed())))
    }

    @Test
    fun goToBuyScreen() {
        scenario.onFragment {
            it.model.orderAmounts.apply {
                selectedFiatAmount = "100"
                selectedFiatCurrency = "EUR"
                selectedCryptoAmount = "1.05"
                selectedCryptoCurrency = "BCH"
            }
        }

        onView(withText(R.string.cexd_buy_more_crypto)).perform(click())

        intended(
            allOf(
                hasComponent(CalcActivity::class.java.name),
                hasExtra("lastFiatAmount", "100"),
                hasExtra("lastFiatCurrency", "EUR"),
                hasExtra("lastCryptoCurrency", "BCH")
            )
        )
    }

    @Test
    fun browseTxDetails() {
        scenario.onFragment {
            givenAmounts(it.model)
            it.model.paymentInfo.set(givenPaymentInfo())
            it.model.txId.set(TX_ID)
        }

        onView(withText(TX_ID)).perform(click())

        intended(hasData("${symbolMap.getValue("BTC").transactionBrowserAddress}$TX_ID"))
    }

    @Test
    fun copyTxId() {
        scenario.onFragment {
            givenAmounts(it.model)
            it.model.paymentInfo.set(givenPaymentInfo())
            it.model.txId.set(TX_ID)
        }

        onView(withText(TX_ID)).perform(longClick())

        onView(withText(R.string.cexd_tx_id_copied)).inRoot(isToast()).check(matches(isDisplayed()))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboardManager =
                InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
                    ClipboardManager::class.java
                )!!
            assertThat(clipboardManager.hasPrimaryClip()).isTrue()
            assertThat(clipboardManager.primaryClip!!.getItemAt(0).text).isEqualTo(TX_ID)
        }
    }

    @Test
    fun unsubscribeWhenOrderFinished() {
        scenario.onFragment {
            givenAmounts(it.model)
        }

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            orderInfo.value = givenFinishedValue()
        }

        verify(api).removeOrderInfoSubscription()
    }

    private fun givenFinishedValue(): Success<OrderInfoData> =
        Success(
            OrderInfoData(
                "abc123",
                "abc123",
                OrderStatus.FINISHED,
                Date(),
                "somebody@somewhere.net",
                "DE",
                Basic(
                    Images(true, true),
                    "0000",
                    MonetaryData("10", "USD"),
                    MonetaryData("1", "BTC"),
                    Wallet(".", null),
                    true,
                    false,
                    "https://example.com"
                ),
                null,
                PaymentInfo(
                    MonetaryData("10", "USD"),
                    MonetaryData("1", "BTC"),
                    Wallet(".", null),
                    "test"
                ),
                emptyMap()
            )
        )

    private fun givenPaymentInfo() = PaymentInfo(
        fiat = MonetaryData("100", "USD"),
        crypto = MonetaryData("0.123", "BTC"),
        wallet = Wallet("abc123", null),
        txId = null
    )

    private fun givenAmounts(model: OrderActivityViewModel) {
        model.orderAmounts.apply {
            selectedCryptoCurrency = "BTC"
            selectedCryptoAmount = "0.123"
            selectedFiatCurrency = "USD"
            selectedFiatAmount = "100"
        }
    }

    companion object {
        const val TX_ID = "abcdefghijklmnop1234567890"
    }
}
