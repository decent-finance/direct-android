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
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.util.hasVisibility
import com.cexdirect.sample.R
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock


class PaymentConfirmationTest {

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

        whenever(messenger.subscribeToOrderInfo()).thenReturn(MutableLiveData())
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun showTimer() {
        activityRule.launchActivity(givenIntent())

        activityRule.activity.replaceFragment(1)
        activityRule.activity.model.askForEmailConfirmation()

        onView(withId(R.id.fpcTimer))
            .perform(scroll())
            .check(hasVisibility(View.VISIBLE))
    }

    @Test
    fun hideTimerWhenCountdownFinishes() {
        activityRule.launchActivity(givenIntent())

        activityRule.activity.replaceFragment(1)
        activityRule.activity.model.askForEmailConfirmation()
        activityRule.activity.model.checkCode.timer.onFinish()

        onView(withId(R.id.fpcTimer)).check(hasVisibility(View.GONE))
        onView(withText(R.string.cexd_code_not_received))
            .perform(scroll())
            .check(hasVisibility(View.VISIBLE))
    }

    private fun givenIntent() =
        Intent().apply {
            putExtra("crypto", "BTC")
            putExtra("cryptoAmount", "0.5")
            putExtra("fiat", "USD")
            putExtra("fiatAmount", "50")
        }
}

fun scroll() = object : ViewAction {
    override fun getDescription() = "Scroll to view"

    override fun getConstraints(): Matcher<View> = allOf(
        withEffectiveVisibility(Visibility.VISIBLE), isDescendantOfA(
            anyOf(
                isAssignableFrom(ScrollView::class.java),
                isAssignableFrom(HorizontalScrollView::class.java),
                isAssignableFrom(NestedScrollView::class.java)
            )
        )
    )

    override fun perform(uiController: UiController, view: View) {
        GeneralSwipeAction(Swipe.SLOW, GeneralLocation.CENTER, CoordinatesProvider {
            val coord = GeneralLocation.CENTER.calculateCoordinates(view)
            floatArrayOf(coord[0], 0f)
        }, Press.FINGER)
    }

}
