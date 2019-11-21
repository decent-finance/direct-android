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

package com.cexdirect.lib.check

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DispatcherRule
import com.cexdirect.lib.FileUtils.aJson
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.CountriesResponse
import com.cexdirect.lib.network.models.PlacementInfoResponse
import com.cexdirect.lib.network.models.RuleData
import com.cexdirect.lib.network.models.RuleResponse
import com.google.gson.Gson
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class PlacementApiTest {

    val gson = Gson()

    @get:Rule
    val dispatcherRule = DispatcherRule()

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var merchantService: MerchantService

    @Mock
    lateinit var paymentService: PaymentService

    private lateinit var placementApi: PlacementApi

    @Before
    fun setUp() {
        Direct.credentials = Credentials("test-1", "foo")
        MockitoAnnotations.initMocks(this)

        placementApi = PlacementApi(MerchantFlow(merchantService), PaymentFlow(paymentService))
    }

    @After
    fun tearDown() {
        reset(merchantService, paymentService)
    }

    @Test
    fun returnFailureForInactivePlacement() = runBlockingTest {
        merchantService.stub {
            onBlocking { getPlacementInfo(any()) } doReturn gson.fromJson(
                aJson("placement_info.json"),
                PlacementInfoResponse::class.java
            )
        }

        placementApi.loadPlacementData(this) { it.activityStatus }

        placementApi.checkResult
            .test()
            .assertValue { it is Failure }
    }

    @Test
    fun returnFailWhenGetCountriesReturnsError() = runBlockingTest {
        merchantService.stub {
            onBlocking { getPlacementInfo(any()) } doReturn gson.fromJson(
                aJson("placement_info.json"),
                PlacementInfoResponse::class.java
            )
            onBlocking { getRule(any()) } doReturn RuleResponse(
                RuleData(
                    "",
                    "",
                    "",
                    Date().toGMTString()
                )
            )
        }
        paymentService.stub {
            onBlocking { getCountries() } doThrow IllegalStateException()
        }

        placementApi.loadPlacementData(this) { it.activityStatus }

        placementApi.checkResult
            .test()
            .assertValue { it is Failure }
    }

    @Test
    fun returnSuccessWhenLoaded() = runBlockingTest {
        merchantService.stub {
            onBlocking { getPlacementInfo(any()) } doReturn gson.fromJson(
                aJson("placement_info.json"),
                PlacementInfoResponse::class.java
            )
            onBlocking { getRule(any()) } doReturn RuleResponse(
                RuleData(
                    "",
                    "",
                    "",
                    Date().toGMTString()
                )
            )
        }
        paymentService.stub {
            onBlocking { getCountries() } doReturn CountriesResponse(emptyList())
        }

        placementApi.loadPlacementData(this) { true }

        placementApi.checkResult
            .test()
            .assertValue { it is Success }
    }
}

