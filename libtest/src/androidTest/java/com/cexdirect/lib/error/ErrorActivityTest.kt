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
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib.*
import com.cexdirect.lib.buy.BuyActivity
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.terms.TermsActivity
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.google.android.material.textfield.TextInputEditText
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assume.assumeThat
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.util.*
import org.assertj.core.api.Java6Assertions.assertThat as assertjThat

class ErrorActivityTest {

    @get:Rule
    val activityRule = IntentsTestRule(ErrorActivity::class.java, true, false)

    @get:Rule
    val mockRule = DirectMockRule()

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

    @Test
    fun showTerms() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)

        onView(
            allOf(
                withText("Terms"),
                isAssignableFrom(TextView::class.java),
                hasSibling(withText("Refund"))
            )
        ).perform(scrollTo(), click())

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
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_exit)).perform(scrollTo(), click())

        onView(withText(R.string.cexd_do_you_want_to_exit)).check(matches(isDisplayed()))
    }

    @Test
    fun dismissExitDialog() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_exit)).perform(scrollTo(), click())
        onView(withText(R.string.cexd_cancel)).perform(click())

        onView(withText(R.string.cexd_do_you_want_to_exit)).check(doesNotExist())
    }

    @Test
    fun exit() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_exit)).perform(scrollTo(), click())
        onView(withText(R.string.cexd_exit)).perform(click())

        @Suppress("UsePropertyAccessSyntax")
        assertjThat(activityRule.activity.isFinishing).isTrue()
    }

    @Test
    fun displayLocationNotSupportedError() {
        val intent = givenLocationNotSupportedIntent()

        activityRule.launchActivity(intent)

        onView(withText(R.string.cexd_location_not_supported)).check(matches(isDisplayed()))
    }

    @Test
    fun displayGivenUserEmail() {
        Direct.userEmail = "fast@usps.gov"

        val intent = givenLocationNotSupportedIntent()

        activityRule.launchActivity(intent)

        onView(
            allOf(
                withText("fast@usps.gov"),
                isAssignableFrom(TextInputEditText::class.java)
            )
        ).check(
            matches(isDisplayed())
        )
    }

    @Test
    fun displayVerificationError() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)

        onView(withText("Test reason")).check(matches(isDisplayed()))
    }

    @Test
    fun relaunchDirectFromVerificationErrorScreen() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_try_again)).perform(click())

        intended(hasComponent(BuyActivity::class.java.name))
    }

    @Test
    fun displayPurchaseError() {
        val intent = givenPurchaseFailedIntent()

        activityRule.launchActivity(intent)

        onView(withText("Test reason")).check(matches(isDisplayed()))
    }

    @Test
    fun relaunchDirectFromPurchaseErrorScreen() {
        val intent = givenPurchaseFailedIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_try_again)).perform(click())

        intended(hasComponent(BuyActivity::class.java.name))
    }

    @Test
    fun contactSupport() {
        val intent = givenVerificationErrorIntent()

        activityRule.launchActivity(intent)
        onView(withText(R.string.cexd_support)).perform(scrollTo(), click())

        val resolved = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(BaseActivity.SUPPORT_EMAIL)
        }.resolveActivity(InstrumentationRegistry.getInstrumentation().targetContext.packageManager)
        assumeThat(resolved, notNullValue())

        intended(allOf(hasData(BaseActivity.SUPPORT_EMAIL), hasAction(Intent.ACTION_SENDTO)))
    }

    private fun givenLocationNotSupportedIntent() =
        Intent().apply {
            putExtra("type", ErrorType.LOCATION_NOT_SUPPORTED.name)
            putExtra("reason", "Test reason")
        }

    private fun givenPurchaseFailedIntent() =
        Intent().apply {
            putExtra("type", ErrorType.PURCHASE_FAILED.name)
            putExtra("reason", "Test reason")
        }

    private fun givenVerificationErrorIntent() =
        Intent().apply {
            putExtra("type", ErrorType.VERIFICATION_ERROR.name)
            putExtra("reason", "Test reason")
        }
}
