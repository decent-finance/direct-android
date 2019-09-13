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

package com.cexdirect.lib.buy

import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.stub.StubActivity
import com.cexdirect.lib.util.MockServerIdlingResource
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.cexdirect.lib.util.WaitForActivityResource
import com.cexdirect.lib.views.PopularValuesView
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.*
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import java.util.*
import kotlin.collections.ArrayList

class BuyActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(BuyActivity::class.java, true, false)

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @Mock
    lateinit var messenger: Messenger

    @Mock
    lateinit var cexdSocket: CexdSocket

    private val mockServer = MockWebServer()

    @Before
    fun setUp() {
        Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
        Direct.rules.addAll(
                hashSetOf(
                        RuleData("1", "Terms", "Test terms", Date().toGMTString()),
                        RuleData("2", "Refund", "Test refund policy", Date().toGMTString())
                )
        )
        whenever(messenger.subscribeToExchangeRates(anyString())).thenReturn(MutableLiveData())
        Intents.init()
        mockServer.start(8080)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
        Intents.release()
        reset(messenger, cexdSocket)
    }

    @Ignore
    @Test
    fun loadData() {
        mockServer.dispatcher = BuyActivityDispatcher()
        activityRule.launchActivity(null)

        val paths = ArrayList<String>()
        paths.add(mockServer.takeRequest().path!!)
        paths.add(mockServer.takeRequest().path!!)
        paths.add(mockServer.takeRequest().path!!)

        assertThat(paths).hasSize(3)

        assertThat(paths.find { it.endsWith("v1/orders/opened") }).isNotNull()
        assertThat(paths.find { it.endsWith("v1/merchant/precisions/$TEST_PLACEMENT") }).isNotNull()
        assertThat(paths.find { it.endsWith("v1/payments/currencies/$TEST_PLACEMENT") }).isNotNull()
    }

    @Ignore
    @Test
    fun displayPopularValues() {
        val activityIdlingRes = WaitForActivityResource(BuyActivity::class.java.name)
        val mockServerIdlingRes = MockServerIdlingResource(mockServer)
        IdlingRegistry.getInstance().register(activityIdlingRes, mockServerIdlingRes)

        mockServer.dispatcher = BuyActivityDispatcher()
        activityRule.launchActivity(null)

        onView(isAssignableFrom(PopularValuesView::class.java)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("100 USD")).check(matches(isDisplayed()))
        onView(withText("150 USD")).check(matches(isDisplayed()))
        onView(withText("200 USD")).check(matches(isDisplayed()))

        IdlingRegistry.getInstance().unregister(activityIdlingRes, mockServerIdlingRes)
    }

    @Test
    fun showStubScreenOnError() {
        mockServer.dispatcher = BuyActivityErrorDispatcher()

        activityRule.launchActivity(null)

        intended(hasComponent(StubActivity::class.java.name))
    }
}
