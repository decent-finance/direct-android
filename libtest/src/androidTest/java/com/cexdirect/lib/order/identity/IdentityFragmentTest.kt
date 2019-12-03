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

package com.cexdirect.lib.order.identity

import android.Manifest
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.R
import com.cexdirect.lib.network.models.*
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.order.OrderActivityViewModel
import com.cexdirect.lib.order.OrderStep
import com.cexdirect.lib.order.scanner.QrScannerActivity
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.entryData
import com.cexdirect.lib.util.hasVisibility
import com.cexdirect.lib.views.CollapsibleLayout
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import java.util.*
import org.assertj.core.api.Java6Assertions.assertThat as asserjThat

@LargeTest
class IdentityFragmentTest {

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Mock
    lateinit var messenger: Messenger

    private val mockServer = MockWebServer()

    private lateinit var scenario: FragmentScenario<IdentityFragment>

    @Before
    fun setUp() {
        Intents.init()

        mockServer.start(8080)

        whenever(messenger.subscribeToOrderInfo()).thenReturn(MutableLiveData())

        scenario = launchFragmentInContainer(
            themeResId = R.style.Direct, instantiate = { ScrollableIdentityFragment() }
        )
    }

    @After
    fun tearDown() {
        mockServer.shutdown()

        Intents.release()
    }

    @Test
    fun validateEmptyUserLocation() {
        onView(withText(R.string.cexd_next)).perform(click())

        onView(withText(R.string.cexd_invalid_email)).check(hasVisibility(View.VISIBLE))
        onView(withText(R.string.cexd_select_country)).check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun displayEmailErrorForIncompleteEmail() {
        onView(withHint(R.string.cexd_email)).perform(typeText("aaaaaaa"))

        onView(withText(R.string.cexd_invalid_email)).check(matches(isDisplayed()))
    }

    @Test
    fun disableCountrySelectionAfterOrderIsCreated() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withText("Email and Country")).perform(scrollTo(), click())
        onView(withHint(R.string.cexd_country)).perform(click())

        onView(withText(R.string.cexd_choose_country)).check(doesNotExist())
    }

    @Test
    fun displayNoPhotoErrorForPassport() {
        scenario.onFragment {
            goToBase(it.model)
            givenAllPhotosRequired(it.model)
        }

        onView(withId(R.id.fiPassport)).perform(click())
        onView(withId(R.id.fiNext)).perform(scrollTo(), click())

        onView(withId(R.id.pdDocFrontError)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun displayFileTooBigError() {
        scenario.onFragment {
            goToBase(it.model)
            givenAllPhotosRequired(it.model)
        }

        onView(withId(R.id.fiPassport)).perform(click())

        scenario.onFragment {
            it.model.apply {
                userDocs.currentPhotoType = PhotoType.ID
                setImageSizeInvalid()
            }
        }

        SystemClock.sleep(500)

        onView(withText(R.string.cexd_file_too_big))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun displayValidationErrorsForCardNumber() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withHint(R.string.cexd_card_number)).perform(scrollTo(), typeText("1234"))

        onView(withText(R.string.cexd_invalid_card_number))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun displaySsnForUs() {
        scenario.onFragment {
            goToBase(it.model)
            it.model.userCountry.selectedCountry = CountryData(
                "USA",
                "US",
                listOf(CountryData("Alabama", "AL", null))
            )
        }

        onView(withHint(R.string.cexd_ssn)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun dontDisplaySsnForNonUs() {
        scenario.onFragment {
            goToBase(it.model)
            it.model.userCountry.selectedCountry = CountryData("Mexico", "MX", null)
        }

        onView(withHint(R.string.cexd_ssn)).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayValidationErrorForSsn() {
        scenario.onFragment {
            goToBase(it.model)
            it.model.apply {
                userCountry.shouldShowState = true
            }
        }

        onView(withHint(R.string.cexd_ssn)).perform(scrollTo(), typeText("911"))

        onView(withText(R.string.cexd_invalid_ssn))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun displayValidationErrorsForCardExpDate() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withHint(R.string.cexd_exp_date)).perform(scrollTo(), typeText("11"))
        Espresso.closeSoftKeyboard()

        onView(withText(R.string.cexd_invalid_exp_date)).check(matches(isDisplayed()))
    }

    @Test
    fun displayValidationErrorsForCardCvv() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withHint(R.string.cexd_cvv)).perform(scrollTo(), typeText("9"))
        Espresso.closeSoftKeyboard()

        onView(withText(R.string.cexd_invalid_cvv))
            .perform(scrollTo())
            .check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun launchQrScanner() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withContentDescription(R.string.cexd_open_qr_scanner)).perform(scrollTo(), click())

        intended(hasComponent(QrScannerActivity::class.java.name))
    }

    @Test
    fun showCvvInfoDialog() {
        scenario.onFragment {
            goToBase(it.model)
        }

        onView(withContentDescription(R.string.cexd_show_cvv_info)).perform(scrollTo(), click())

        onView(withText(R.string.cexd_cvv_desc)).check(matches(isDisplayed()))
    }

    @Test
    fun initValidationMap() {
        scenario.onFragment {
            goToExtras(it.model)
            givenAdditionalFields(it.model)

            asserjThat(it.model.validationMap).hasSize(2)
                .hasEntrySatisfying(entryData("userLastName", FieldStatus.EMPTY))
                .hasEntrySatisfying(entryData("userFirstName", FieldStatus.EMPTY))
        }
    }

    @Test
    fun validateAdditionalFields() {
        scenario.onFragment {
            goToExtras(it.model)
            givenAdditionalFields(it.model)
        }

        SystemClock.sleep(500)

        onView(withHint(R.string.cexd_last_name)).perform(scrollTo(), typeText("Smith"))

        scenario.onFragment {
            it.model.uploadExtraPaymentData()
            asserjThat(it.model.validationMap)
                .hasSize(2)
                .hasEntrySatisfying(entryData("userLastName", FieldStatus.VALID))
                .hasEntrySatisfying(entryData("userFirstName", FieldStatus.INVALID))
        }
    }

    @Test
    fun disableFilledFields() {
        scenario.onFragment {
            goToExtras(it.model)
            givenFilledAdditionalFields(it.model)
        }

        SystemClock.sleep(500)

        onView(withHint(R.string.cexd_last_name))
            .perform(scrollTo())
            .check(matches(not(isEnabled())))
    }

    @Test
    fun displayAptDashByDefault() {
        scenario.onFragment {
            it.model.updateOrderStatus(givenOrderInfo(), { }, {}, {})
        }

        SystemClock.sleep(500)

        onView(allOf(withHint(R.string.cexd_residential_apt), withText("-")))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    private fun givenOrderInfo() =
        OrderInfoData(
            "abc123",
            OrderStatus.PSS_WAITDATA,
            Date(),
            "poststelle@bundeskanzlerin.de-mail.de",
            "DE",
            Basic(
                Images(false, false),
                "0007",
                MonetaryData("10", "EUR"),
                MonetaryData("1", "BTC"),
                Wallet("test"),
                false,
                false,
                "https://example.com"
            ),
            null,
            null,
            hashMapOf(
                "userResidentialAptSuite" to Additional(null, true, true),
                "userResidentialCountry" to Additional(null, false, true),
                "billingCountry" to Additional(null, false, true),
                "billingState" to Additional(null, false, true)
            )
        )

    private fun goToBase(model: OrderActivityViewModel) {
        model.apply {
            locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            paymentBaseContentState.set(CollapsibleLayout.ContentState.EXPANDED)
            orderStep.set(OrderStep.PAYMENT_BASE)
        }
    }

    private fun goToExtras(model: OrderActivityViewModel) {
        model.apply {
            locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            paymentBaseContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            orderStep.set(OrderStep.PAYMENT_EXTRA)
        }
    }

    private fun givenAllPhotosRequired(model: OrderActivityViewModel) {
        model.userDocs.requiredImages = Images(true, true)
    }

    private fun givenAdditionalFields(model: OrderActivityViewModel) {
        model.apply {
            additionalFields.set(
                hashMapOf(
                    "userFirstName" to Additional(null, true, true),
                    "userLastName" to Additional(null, true, true)
                )
            )
        }
    }

    private fun givenFilledAdditionalFields(model: OrderActivityViewModel) {
        model.apply {
            additionalFields.set(
                hashMapOf(
                    "userFirstName" to Additional("Abraham", true, false),
                    "userLastName" to Additional("Lincoln", true, false)
                )
            )
        }
    }
}

/*
 *  Since container activity only has root FrameLayout, we use this class to wrap fragment's view in
 *  ScrollView. This way, we can use scrollTo() view action and scroll to views outside our
 *  view port.
 */
class ScrollableIdentityFragment : IdentityFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        ScrollView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(super.onCreateView(inflater, container, savedInstanceState))
        }
}
