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

package com.cexdirect.lib.verification

import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.R
import com.cexdirect.lib.network.models.Additional
import com.cexdirect.lib.network.models.Images
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.util.FieldStatus
import com.cexdirect.lib.util.entryData
import com.cexdirect.lib.util.hasVisibility
import com.cexdirect.lib.verification.identity.PhotoType
import com.cexdirect.lib.verification.identity.VerificationStep
import com.cexdirect.lib.views.CollapsibleLayout
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.mockito.Mock

@MediumTest
class VerificationActivityDataValidationTest {

    @get:Rule
    val activityRule = ActivityTestRule(VerificationActivity::class.java, true, false)

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @Mock
    lateinit var messenger: Messenger

    @Mock
    lateinit var cexdSocket: CexdSocket

    private val mockServer = MockWebServer()

    @Before
    fun setUp() {
        mockServer.start(8080)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun validateEmptyUserLocation() {
        activityRule.launchActivity(givenIntent())

        activityRule.activity.model.createOrder()
        closeSoftKeyboard()

        onView(withText(R.string.cexd_invalid_email)).perform(scrollTo())
            .check(hasVisibility(View.VISIBLE))
        onView(withText(R.string.cexd_select_country))/*.perform(scrollTo())*/.check(
            hasVisibility(
                View.VISIBLE
            )
        )
    }

    @Test
    fun displayEmailErrorForIncompleteEmail() {
        activityRule.launchActivity(givenIntent())

        onView(withHint(R.string.cexd_email)).perform(typeText("aaaaaaa"))

        onView(withText(R.string.cexd_invalid_email))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun displayNoPhotoErrorForPassport() {
        activityRule.launchActivity(givenIntent())
        goToBase()
        givenAllPhotosRequired()

        onView(withId(R.id.fiPassport)).perform(click())
        onView(withId(R.id.fiNext)).perform(click())

        onView(withId(R.id.pdDocFrontError))
            .perform(scrollTo())
            .check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun displayFileTooBigError() {
        activityRule.launchActivity(givenIntent())
        goToBase()
        givenAllPhotosRequired()

        onView(withId(R.id.fiPassport)).perform(click())
        activityRule.activity.model.apply {
            userDocs.currentPhotoType = PhotoType.ID
            setImageSizeInvalid()
        }

        SystemClock.sleep(500) // FIXME make espresso wait for layout to settle

        onView(withText(R.string.cexd_file_too_big)).perform(scrollTo())
            .check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun displayValidationErrorsForCardNumber() {
        activityRule.launchActivity(givenIntent())
        goToBase()

        onView(withHint(R.string.cexd_card_number)).perform(scrollTo(), typeText("1234"))

        onView(withText(R.string.cexd_invalid_card_number)).check(hasVisibility(View.VISIBLE))
    }

    @Ignore // todo fix
    @Test
    fun displayValidationErrorsForCardExpDate() {
        activityRule.launchActivity(givenIntent())
        goToBase()

        onView(withHint(R.string.cexd_exp_date)).perform(scrollTo(), typeText("11"))
        closeSoftKeyboard()

        onView(withText(R.string.cexd_invalid_exp_date)).check(hasVisibility(View.VISIBLE))
    }

    @Ignore // todo fix
    @Test
    fun displayValidationErrorsForCardCvv() {
        activityRule.launchActivity(givenIntent())
        goToBase()

        onView(withHint(R.string.cexd_cvv)).perform(scrollTo(), typeText("9"))
        closeSoftKeyboard()

        onView(withText(R.string.cexd_invalid_cvv)).perform(scrollTo())
            .check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun initValidationMap() {
        activityRule.launchActivity(givenIntent())
        goToExtras()
        givenAdditionalFields()

        val model = activityRule.activity.model
        assertThat(model.validationMap).hasSize(2)
            .hasEntrySatisfying(entryData("userLastName", FieldStatus.EMPTY))
            .hasEntrySatisfying(entryData("userFirstName", FieldStatus.EMPTY))
    }

    @Ignore("loader is shown for some reason")
    @Test
    fun validateAdditionalFields() {
        activityRule.launchActivity(givenIntent())
        goToExtras()
        givenAdditionalFields()

        onView(withHint(R.string.cexd_last_name)).perform(scrollTo(), typeText("Smith"))
        val model = activityRule.activity.model
        model.uploadExtraPaymentData()

        assertThat(model.validationMap)
            .hasSize(2)
            .hasEntrySatisfying(entryData("userLastName", FieldStatus.VALID))
            .hasEntrySatisfying(entryData("userFirstName", FieldStatus.INVALID))
    }

    private fun goToBase() {
        activityRule.activity.stickyViewEvent.postValue(View.NO_ID)
        activityRule.activity.model.apply {
            locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            paymentBaseContentState.set(CollapsibleLayout.ContentState.EXPANDED)
            verificationStep.set(VerificationStep.PAYMENT_BASE)
        }
    }

    private fun goToExtras() {
        activityRule.activity.stickyViewEvent.postValue(View.NO_ID)
        activityRule.activity.model.apply {
            locationEmailContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            paymentBaseContentState.set(CollapsibleLayout.ContentState.COLLAPSED)
            verificationStep.set(VerificationStep.PAYMENT_EXTRA)
        }
    }

    private fun givenIntent() =
        Intent().apply {
            putExtra("crypto", "BTC")
            putExtra("cryptoAmount", "0.5")
            putExtra("fiat", "USD")
            putExtra("fiatAmount", "50")
        }

    private fun givenAllPhotosRequired() {
        activityRule.activity.model.userDocs.requiredImages = Images(true, true)
    }

    private fun givenAdditionalFields() {
        activityRule.activity.model.apply {
            additionalFields.set(
                hashMapOf(
                    "userFirstName" to Additional(null, true, true),
                    "userLastName" to Additional(null, true, true)
                )
            )
        }
    }
}
