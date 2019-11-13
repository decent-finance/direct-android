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
import com.cexdirect.lib.DispatcherRule
import com.cexdirect.lib.terms.TermsActivity
import com.cexdirect.lib.util.PlacementValidator
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CheckActivityViewModelTest {

    @get:Rule
    val activityRule = ActivityTestRule(TermsActivity::class.java, true, false)

    @get:Rule
    val dispatcherRule = DispatcherRule()

    @Mock
    lateinit var placementApi: PlacementApi

    @Mock
    lateinit var placementValidator: PlacementValidator

    lateinit var model: CheckActivityViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(placementApi.checkResult).thenReturn(mock())
        model = CheckActivityViewModel(placementApi, placementValidator)
    }

    @After
    fun tearDown() {
        reset(placementApi, placementValidator)
    }

    @Test
    fun checkPlacement() {
        Direct.credentials = Credentials("foo", "s3cr3t")

        model.loadPlacementData()

        @Suppress("DeferredResultUnused")
        verify(placementApi).loadPlacementData(any(), any())
    }
}
