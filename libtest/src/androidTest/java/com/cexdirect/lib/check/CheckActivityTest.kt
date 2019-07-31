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

package com.cexdirect.lib.check

import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.ErrorDispatcher
import com.cexdirect.lib.buy.BuyActivity
import com.cexdirect.lib.stub.StubActivity
import com.cexdirect.lib.util.TEST_PLACEMENT
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.*

class CheckActivityTest {

    @get:Rule
    val activityRule = IntentsTestRule(CheckActivity::class.java, true, false)

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    private val mockServer = MockWebServer()

    companion object {

        @JvmStatic
        @BeforeClass
        fun setUpAll() {
            Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
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

    @Ignore
    @Test
    fun loadDataAndLaunchDirect() {
        mockServer.dispatcher = CheckActivityDispatcher()
        activityRule.launchActivity(null)

        assertThat(mockServer.takeRequest().path).endsWith("v1/merchant/placement/check/$TEST_PLACEMENT")
        assertThat(mockServer.takeRequest().path).endsWith("v1/merchant/rules/1")

        intended(hasComponent(BuyActivity::class.java.name))
    }

    @Ignore
    @Test
    fun showStubScreenOnNetworkError() {
        mockServer.dispatcher = ErrorDispatcher()

        activityRule.launchActivity(null)

        assertThat(activityRule.activity.isFinishing).isTrue()
        intended(hasComponent(StubActivity::class.java.name))
    }

    @Ignore
    @Test
    fun showStubScreenOnInactiveMerchant() {
        mockServer.dispatcher = CheckActivityInactiveMerchantDispatcher()

        activityRule.launchActivity(null)

        assertThat(activityRule.activity.isFinishing).isTrue()
        intended(hasComponent(StubActivity::class.java.name))
    }
}
