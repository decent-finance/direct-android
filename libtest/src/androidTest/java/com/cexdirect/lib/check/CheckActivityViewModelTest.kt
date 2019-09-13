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

import androidx.test.rule.ActivityTestRule
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.MockCoroutineDispatcherProvider
import com.cexdirect.lib.network.MerchantApi
import com.cexdirect.lib.network.MerchantService
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.PaymentService
import com.cexdirect.lib.network.models.CountryData
import com.cexdirect.lib.network.models.PlacementInfo
import com.cexdirect.lib.terms.TermsActivity
import com.cexdirect.lib.util.PlacementValidator
import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CheckActivityViewModelTest {

    @get:Rule
    val activityRule = ActivityTestRule(TermsActivity::class.java, true, false)

    @Mock
    lateinit var merchantService: MerchantService

    @Mock
    lateinit var paymentService: PaymentService

    @Mock
    lateinit var placementValidator: PlacementValidator

    lateinit var ruleIds: RuleIds

    lateinit var model: CheckActivityViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        ruleIds = RuleIds()
        model = CheckActivityViewModel(
            MerchantApi(merchantService),
            PaymentApi(paymentService),
            placementValidator,
            ruleIds,
            MockCoroutineDispatcherProvider()
        )
    }

    @After
    fun tearDown() {
        reset(merchantService, paymentService, placementValidator)
    }

    @Test
    fun invokeFailForInactivePlacement() {
        val givenInfo = PlacementInfo("", "", false, emptyList(), listOf("foo", "bar"))
        val givenRunnable: Runnable = mock()

        model.processPlacementInfo(givenInfo) { givenRunnable.run() }

        verify(givenRunnable).run()
    }

    @Test
    fun updateIdsAndLoadCountriesForActivePlacement() {
        val givenInfo = PlacementInfo("", "", true, emptyList(), listOf("foo", "bar"))
        val givenRunnable: Runnable = mock()

        model.processPlacementInfo(givenInfo) { givenRunnable.run() }

        verify(givenRunnable, never()).run()
        assertThat(ruleIds.ids).containsOnly("foo", "bar")
        @Suppress("DeferredResultUnused")
        verify(paymentService).getCountriesAsync()
    }

    @Test
    fun saveCountriesAndLoadRule() {
        val givenCountry = CountryData("Belarus", "BY", null)
        val givenIds = listOf("foo", "bar")

        ruleIds.ids = givenIds
        model.saveCountriesAndLoadRules(listOf(givenCountry))

        assertThat(Direct.countries).containsOnly(givenCountry)
        @Suppress("DeferredResultUnused")
        verify(merchantService).getRuleAsync(eq("foo"))
    }

    @Test
    fun checkPlacement() {
        Direct.credentials = Credentials("foo", "s3cr3t")

        model.checkPlacement()

        @Suppress("DeferredResultUnused")
        verify(merchantService).getPlacementInfoAsync(anyString())
    }
}
