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

package com.cexdirect.lib.buy

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.DispatcherRule
import com.cexdirect.lib.FileUtils.aJson
import com.cexdirect.lib.fromString
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.models.EventData
import com.cexdirect.lib.network.ws.Messenger
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

class CalcApiTest {

    val gson = Gson()

    @get:Rule
    val dispatcherRule = DispatcherRule()

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var messenger: Messenger

    @Mock
    lateinit var analyticsService: AnalyticsService

    @Mock
    lateinit var merchantService: MerchantService

    @Mock
    lateinit var paymentService: PaymentService

    private lateinit var calcApi: CalcApi

    @Before
    fun setUp() {
        Direct.credentials = Credentials("test-1", "foo")
        Direct.sourceUri = "android://com.example.test"
        MockitoAnnotations.initMocks(this)

        calcApi = CalcApi(
            AnalyticsFlow(analyticsService),
            MerchantFlow(merchantService),
            PaymentFlow(paymentService),
            messenger
        )

        whenever(messenger.subscribeToExchangeRates(any())).thenReturn(MutableLiveData())
    }

    @After
    fun tearDown() {
        reset(messenger, analyticsService, merchantService, paymentService)
    }

    @Test
    fun subscribeToRates() {
        calcApi.subscribeToExchangeRates()

        verify(messenger).subscribeToExchangeRates(eq("test-1"))
    }

    @Test
    fun removeRatesSubscriptions() {
        calcApi.unsubscribeFromExchangeRates()

        verify(messenger).removeExchangesSubscription()
        verify(messenger, never()).removeOrderInfoSubscription()
    }

    @Test
    fun returnSuccessAfterBuyEventSucceeded() = runBlockingTest {
        analyticsService.stub {
            onBlocking { sendBuyEvent(any()) } doReturn gson.fromString(aJson("ok_empty_response.json"))
        }

        val actual = calcApi.sendBuyEvent(this, EventData(fiat = null, crypto = null))

        actual.test().assertValue { it is Success }
    }

    @Test
    fun returnFailureAfterBuyEventFailed() = runBlockingTest {
        analyticsService.stub {
            onBlocking { sendBuyEvent(any()) } doThrow IllegalStateException("Failure")
        }

        val actual = calcApi.sendBuyEvent(this, EventData(fiat = null, crypto = null))

        actual.test().assertValue { it is Failure }
    }

    @Test
    fun returnSuccessAfterCalcDataSucceeded() = runBlockingTest {
        analyticsService.stub {
            onBlocking { sendOpenEvent(any()) } doReturn gson.fromString(aJson("ok_empty_response.json"))
        }
        merchantService.stub {
            onBlocking { getCurrencyPrecisions(any()) } doReturn gson.fromString(aJson("precisions.json"))
        }
        paymentService.stub {
            onBlocking { getExchangeRates(any()) } doReturn gson.fromString(aJson("rates.json"))
        }

        calcApi.loadCalcData(this)

        calcApi.calcData.test().assertValue { it is Success }
    }

    @Test
    fun returnFailureAfterCalcDataFailed() = runBlockingTest {
        analyticsService.stub {
            onBlocking { sendOpenEvent(any()) } doReturn gson.fromString(aJson("ok_empty_response.json"))
        }
        merchantService.stub {
            onBlocking { getCurrencyPrecisions(any()) } doThrow java.lang.IllegalStateException("Failure")
        }
        paymentService.stub {
            onBlocking { getExchangeRates(any()) } doReturn gson.fromString(aJson("rates.json"))
        }

        calcApi.loadCalcData(this)

        calcApi.calcData.test().assertValue { it is Failure }
    }
}
