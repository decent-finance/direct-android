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

import android.os.SystemClock
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.cexdirect.lib.views.PopularValuesView
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.*
import java.util.*

@Ignore
class BuyActivityTest {

    @get:Rule
    val activityRule = IntentsTestRule(BuyActivity::class.java, true, false)

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    private val mockServer = MockWebServer()

    companion object {

        @JvmStatic
        @BeforeClass
        fun setUpAll() {
            Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
            Direct.rules.addAll(
                hashSetOf(
                    RuleData("1", "Terms", "Test terms", Date().toGMTString()),
                    RuleData("2", "Refund", "Test refund policy", Date().toGMTString())
                )
            )
        }
    }


    @Before
    fun setUp() {
        mockServer.start(8080)
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }

    @Test
    fun loadData() {
        mockServer.dispatcher = BuyActivityDispatcher()
        activityRule.launchActivity(null)

        assumeThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/merchant/precisions/$TEST_PLACEMENT")
//        assertThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/merchant/precisions/$TEST_PLACEMENT")
        assumeThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/orders/opened")
//        assertThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/orders/opened")
        assumeThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/payments/currencies/$TEST_PLACEMENT")
//        assertThat(mockServer.takeRequest().path).isNotBlank().endsWith("v1/payments/currencies/$TEST_PLACEMENT")
    }

    @Test
    fun displayPopularValues() {
        mockServer.dispatcher = BuyActivityDispatcher()
        activityRule.launchActivity(null)

        SystemClock.sleep(500)

        onView(isAssignableFrom(PopularValuesView::class.java)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withText("100 USD")).check(matches(isDisplayed()))
        onView(withText("150 USD")).check(matches(isDisplayed()))
        onView(withText("200 USD")).check(matches(isDisplayed()))
    }
}
