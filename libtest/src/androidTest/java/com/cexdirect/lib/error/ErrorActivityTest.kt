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

package com.cexdirect.lib.error

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib.*
import com.cexdirect.lib.buy.CalcActivity
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.terms.TermsActivity
import com.cexdirect.lib.util.CustomViewActions.collapseAppBarLayout
import com.cexdirect.lib.util.CustomViewActions.nestedScrollTo
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.google.android.material.appbar.AppBarLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.*
import org.junit.Assume.assumeThat
import java.util.*
import org.assertj.core.api.Java6Assertions.assertThat as assertjThat

@LargeTest
class ErrorActivityTest {

    @get:Rule
    val mockRule = DirectMockRule()

    private lateinit var scenario: ActivityScenario<ErrorActivity>

    companion object {

        @JvmStatic
        @BeforeClass
        fun setUpAll() {
            Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
            Direct.rules.addAll(
                hashSetOf(
                    RuleData("1", "#Terms", "Test terms", Date().toGMTString()),
                    RuleData("2", "Refund", "Test refund policy", Date().toGMTString())
                )
            )
        }
    }

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        scenario.close()
        Intents.release()
    }

    @Test
    fun showTerms() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(isAssignableFrom(AppBarLayout::class.java)).perform(collapseAppBarLayout())
        onView(
            allOf(
                withText("Terms"),
                isAssignableFrom(TextView::class.java),
                hasSibling(withText("Refund"))
            )
        ).apply {
            perform(nestedScrollTo())

            perform(click())
        }



        intended(
            allOf(
                hasComponent(TermsActivity::class.java.name),
                hasExtra("title", "Terms"),
                hasExtra("content", "Test terms")
            )
        )
    }

    @Test
    fun showExitDialog() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(isAssignableFrom(AppBarLayout::class.java)).perform(collapseAppBarLayout())
        onView(withText(R.string.cexd_exit)).apply {
            perform(nestedScrollTo())
            SystemClock.sleep(500)
            perform(click())
        }
        SystemClock.sleep(500)

        onView(withText(R.string.cexd_do_you_want_to_exit)).check(matches(isDisplayed()))
    }

    @Test
    fun dismissExitDialog() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(isAssignableFrom(AppBarLayout::class.java)).perform(collapseAppBarLayout())
        onView(withText(R.string.cexd_exit)).apply {
            perform(nestedScrollTo())
            SystemClock.sleep(500)
            perform(click())
        }
        SystemClock.sleep(500)
        onView(withText(R.string.cexd_cancel)).perform(click())

        onView(withText(R.string.cexd_do_you_want_to_exit)).check(doesNotExist())
    }

    @Ignore("Incompatible with ActivityScenario?")
    @Test
    fun exit() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(isAssignableFrom(AppBarLayout::class.java)).perform(collapseAppBarLayout())
        onView(withText(R.string.cexd_exit)).apply {
            perform(nestedScrollTo())
            perform(click())
        }
        onView(withText(R.string.cexd_exit)).perform(click())

        assertjThat(scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    @Test
    fun displayLocationNotSupportedError() {
        scenario = ActivityScenario.launch(givenLocationNotSupportedIntent())

        onView(withText(R.string.cexd_location_not_supported)).check(matches(isDisplayed()))
    }

    @Test
    fun displayGenericError() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(withText(R.string.cexd_service_offline)).check(matches(isDisplayed()))
    }

    @Test
    fun displayVerificationRejectedError() {
        scenario = ActivityScenario.launch(givenVerificationRejectedIntent())

        onView(withText(R.string.cexd_you_have_not_passed)).check(matches(isDisplayed()))
    }

    @Test
    fun relaunchDirectFromVerificationErrorScreen() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(withText(R.string.cexd_try_again)).apply {
            perform(nestedScrollTo())
            perform(click())
        }

        intended(hasComponent(CalcActivity::class.java.name))
    }

    @Test
    fun displayPurchaseError() {
        scenario = ActivityScenario.launch(givenPurchaseFailedIntent())

        onView(withText("Test reason")).check(matches(isDisplayed()))
    }

    @Test
    fun displayRefundScreen() {
        scenario = ActivityScenario.launch(givenPaymentRefundedIntent())

        onView(withText(R.string.cexd_refund_title)).check(matches(isDisplayed()))
    }

    @Test
    fun relaunchDirectFromPurchaseErrorScreen() {
        scenario = ActivityScenario.launch(givenPurchaseFailedIntent())

        onView(withText(R.string.cexd_try_again)).perform(click())

        intended(hasComponent(CalcActivity::class.java.name))
    }

    @Test
    fun contactSupport() {
        scenario = ActivityScenario.launch(givenGenericErrorIntent())

        onView(isAssignableFrom(AppBarLayout::class.java)).perform(collapseAppBarLayout())
        onView(withText(R.string.cexd_support)).apply {
            perform(nestedScrollTo())
            SystemClock.sleep(500)
            perform(click())
        }

        val resolved = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(BaseActivity.SUPPORT_EMAIL)
        }.resolveActivity(InstrumentationRegistry.getInstrumentation().targetContext.packageManager)
        assumeThat(resolved, notNullValue())

        intended(allOf(hasData(BaseActivity.SUPPORT_EMAIL), hasAction(Intent.ACTION_SENDTO)))
    }

    private fun givenLocationNotSupportedIntent() =
        Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ErrorActivity::class.java
        ).apply {
            putExtra("type", ErrorType.LOCATION_NOT_SUPPORTED.name)
            putExtra("reason", "Test reason")
            putExtra("info", LastKnownOrderInfo("", "", "", "", ""))
        }

    private fun givenPurchaseFailedIntent() =
        Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ErrorActivity::class.java
        ).apply {
            putExtra("type", ErrorType.PURCHASE_FAILED.name)
            putExtra("reason", "Test reason")
            putExtra("info", LastKnownOrderInfo("abc123", "10", "EUR", "0.0001", "BTC"))
        }

    private fun givenGenericErrorIntent() =
        Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ErrorActivity::class.java
        ).apply {
            putExtra("type", ErrorType.FAIL.name)
        }

    private fun givenVerificationRejectedIntent() =
        Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ErrorActivity::class.java
        ).apply {
            putExtra("type", ErrorType.NOT_VERIFIED.name)
            putExtra("info", LastKnownOrderInfo("abc123", "10", "EUR", "0.0001", "BTC"))
        }

    private fun givenPaymentRefundedIntent() =
        Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            ErrorActivity::class.java
        ).apply {
            putExtra("type", ErrorType.REFUND.name)
            putExtra("info", LastKnownOrderInfo("abc123", "10", "EUR", "0.0001", "BTC"))
            putExtra("refund_extras", RefundExtras("0001"))
        }
}
