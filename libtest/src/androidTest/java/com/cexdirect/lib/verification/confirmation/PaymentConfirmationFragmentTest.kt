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

package com.cexdirect.lib.verification.confirmation

import android.view.View
import android.webkit.WebView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.R
import com.cexdirect.lib.network.models.Tds
import com.cexdirect.lib.network.models.TdsExtras
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.util.hasVisibility
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock

class PaymentConfirmationFragmentTest {

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @Mock
    lateinit var messenger: Messenger

    @Mock
    lateinit var cexdSocket: CexdSocket

    private val mockServer = MockWebServer()

    private lateinit var scenario: FragmentScenario<PaymentConfirmationFragment>

    @Before
    fun setUp() {
        mockServer.start(8080)

        whenever(messenger.subscribeToOrderInfo()).thenReturn(MutableLiveData())

        scenario = launchFragmentInContainer(
            themeResId = R.style.Direct, instantiate = { PaymentConfirmationFragment() }
        )
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun showTimer() {
        scenario.onFragment {
            it.model.askForEmailConfirmation()
        }.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.fpcTimer)).check(matches(isDisplayed()))
    }

    @Test
    fun hideTimerWhenCountdownFinishes() {
        scenario.onFragment {
            it.model.askForEmailConfirmation()
            it.model.checkCode.timer.onFinish()
        }.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.fpcTimer)).check(matches(not(isDisplayed())))
        onView(withText(R.string.cexd_code_not_received)).check(matches(isDisplayed()))
    }

    @Test
    fun openEditEmailDialog() {
        scenario.onFragment {
            it.model.askForEmailConfirmation()
        }.moveToState(Lifecycle.State.RESUMED)

        onView(withText(R.string.cexd_edit_email)).perform(click())

        onView(withText(R.string.cexd_edit_your_email)).check(matches(isDisplayed()))
    }

    @Test
    fun showUserEmail() {
        scenario.onFragment {
            it.model.askForEmailConfirmation()
            it.model.userEmail.email = "example@example.com"
        }.moveToState(Lifecycle.State.RESUMED)

        onView(withText("example@example.com")).check(matches(isDisplayed()))
    }

    @Test
    fun showWebViewWhenAskedFor3ds() {
        scenario.onFragment {
            it.model.askFor3ds(Tds("abc123", "POST", "https://example.com", "FOO", TdsExtras()))
            it.model.userEmail.email = "example@example.com"
        }.moveToState(Lifecycle.State.RESUMED)

        onView(isAssignableFrom(WebView::class.java)).check(hasVisibility(View.VISIBLE))
    }
}
