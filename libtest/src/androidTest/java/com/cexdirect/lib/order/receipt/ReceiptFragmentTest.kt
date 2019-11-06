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

import android.widget.ProgressBar
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.cexdirect.lib.R
import com.cexdirect.lib.buy.CalcActivity
import com.cexdirect.lib.network.models.MonetaryData
import com.cexdirect.lib.network.models.PaymentInfo
import com.cexdirect.lib.network.models.Wallet
import com.cexdirect.lib.order.OrderActivityViewModel
import com.cexdirect.lib.util.symbolMap
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReceiptFragmentTest {

    private lateinit var scenario: FragmentScenario<ReceiptFragment>

    @Before
    fun setUp() {
        Intents.init()
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
