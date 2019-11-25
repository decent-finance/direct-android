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

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DirectNetworkMockRule
import com.cexdirect.lib.SingleLiveEvent
import com.cexdirect.lib.buy.CalcActivity
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.stub.StubActivity
import com.cexdirect.lib.util.PlacementValidator
import com.cexdirect.lib.util.TEST_PLACEMENT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.*
import org.mockito.Mock

class CheckActivityTest {

    @get:Rule
    val mockRule = DirectNetworkMockRule()

    @Mock
    lateinit var placementApi: PlacementApi

    @Mock
    lateinit var validator: PlacementValidator

    private lateinit var scenario: ActivityScenario<CheckActivity>

    private lateinit var liveData: SingleLiveEvent<Resource<Boolean>>

    companion object {

        @JvmStatic
        @BeforeClass
        fun setUpAll() {
            Direct.credentials = Credentials(TEST_PLACEMENT, "superTopSecret")
        }
    }

    @Before
    fun setUp() {
        Intents.init()
        whenever(validator.isPlacementUriAllowed(any())).thenReturn(true)
        liveData = SingleLiveEvent()
        whenever(placementApi.checkResult).thenReturn(liveData)
    }

    @After
    fun tearDown() {
        Intents.release()
        reset(placementApi, validator)
    }

    @Test
    fun loadDataAndLaunchDirect() {
        scenario = ActivityScenario.launch(CheckActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            liveData.postValue(Success(true))
        }

        verify(placementApi).loadPlacementData(any(), any())
        intended(hasComponent(CalcActivity::class.java.name))
    }

    @Test
    fun showStubScreenOnNetworkError() {
        scenario = ActivityScenario.launch(CheckActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            liveData.postValue(Failure(500, "Internal server error"))
        }

        intended(hasComponent(StubActivity::class.java.name))
    }

    @Test
    fun showStubScreenOnInactiveMerchant() {
        scenario = ActivityScenario.launch(CheckActivity::class.java)

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onActivity {
            liveData.postValue(Failure(0, "Placement inactive"))
        }

        intended(hasComponent(StubActivity::class.java.name))
    }
}
