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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.ExchangeRate
import com.cexdirect.lib.network.models.Precision
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.stub.StubActivity
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.cexdirect.lib.views.PopularValuesView
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import java.util.*

class CalcActivityTest {

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @Mock
    lateinit var calcApi: CalcApi

    private lateinit var scenario: ActivityScenario<CalcActivity>

    private lateinit var liveData: MutableLiveData<Resource<Pair<List<Precision>, List<ExchangeRate>>>>

    @Before
    fun setUp() {
        Intents.init()
        Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
        Direct.rules.addAll(
            hashSetOf(
                RuleData("1", "Terms", "Test terms", Date().toGMTString()),
                RuleData("2", "Refund", "Test refund policy", Date().toGMTString())
            )
        )
        whenever(calcApi.subscribeToExchangeRates()).thenReturn(MutableLiveData())
        liveData = MutableLiveData()
        whenever(calcApi.calcData).thenReturn(liveData)
    }

    @After
    fun tearDown() {
        Intents.release()
        reset(calcApi)
    }

    @Test
    fun loadDataWhenLaunched() {
        scenario = ActivityScenario.launch(CalcActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)

        verify(calcApi).loadCalcData(any())
    }

    @Test
    fun displayPopularValues() {
        scenario = ActivityScenario.launch(CalcActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            liveData.postValue(givenResource())
        }

        SystemClock.sleep(500)

        onView(isAssignableFrom(PopularValuesView::class.java)).check(matches(isDisplayed()))
        onView(withText("100 USD")).check(matches(isDisplayed()))
        onView(withText("150 USD")).check(matches(isDisplayed()))
        onView(withText("200 USD")).check(matches(isDisplayed()))
    }

    @Test
    fun showStubScreenOnError() {
        scenario = ActivityScenario.launch(CalcActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            liveData.postValue(Failure(500, "Internal server error"))
        }

        intended(hasComponent(StubActivity::class.java.name))
    }

    private fun givenResource(): Success<Pair<List<Precision>, List<ExchangeRate>>> {
        val precisions = listOf(
            Precision(
                "fiat",
                "USD",
                2,
                2,
                "trunk",
                "100",
                "1000"
            ),
            Precision(
                "crypto",
                "BTC",
                8,
                8,
                "trunk",
                "0.01",
                "0.5"
            )
        )
        val rates = listOf(
            ExchangeRate(
                "USD",
                "BTC",
                1.0,
                0.0,
                1.0,
                listOf("100", "150", "200"),
                emptyList()
            )
        )
        return Success(precisions to rates)
    }
}
