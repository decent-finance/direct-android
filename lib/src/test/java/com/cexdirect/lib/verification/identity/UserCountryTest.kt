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

package com.cexdirect.lib.verification.identity

import com.cexdirect.lib.network.models.CountryData
import com.cexdirect.lib.network.models.emptyCountry
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UserCountryTest {

    private lateinit var userCountry: UserCountry

    @Before
    fun setUp() {
        userCountry = UserCountry()
    }

    @Test
    fun showStateWhenStatesPresent() {
        userCountry.selectedCountry =
            CountryData("United States", "US", arrayListOf(CountryData("California", "CA", null)))

        assertThat(userCountry.shouldShowState).isTrue()
    }

    @Test
    fun dontShowStateWhenNoStatesPresent() {
        userCountry.selectedCountry = CountryData("Mexico", "MX", null)

        assertThat(userCountry.shouldShowState).isFalse()
    }

    @Test
    fun countryIsNotSelectedWhenEmpty() {
        userCountry.selectedCountry = emptyCountry()

        assertThat(userCountry.isValid()).isFalse()
    }

    @Test
    fun countryIsNotSelectedWhenStateIsNotSelected() {
        userCountry.selectedCountry =
            CountryData("United States", "US", arrayListOf(CountryData("California", "CA", null)))
        userCountry.selectedState = emptyCountry()

        assertThat(userCountry.isValid()).isFalse()
    }

    @Test
    fun countryIsSelected() {
        userCountry.selectedCountry = CountryData("Mexico", "MX", null)

        assertThat(userCountry.isValid()).isTrue()
    }

    @Test
    fun countryIsSelectedWhenStateSelected() {
        userCountry.selectedCountry =
            CountryData("United States", "US", arrayListOf(CountryData("California", "CA", null)))
        userCountry.selectedState = CountryData("California", "CA", null)

        assertThat(userCountry.isValid()).isTrue()
    }

    @Test
    fun notValidAfterForceValidate() {
        userCountry.forceValidate()

        assertThat(userCountry.isValid()).isFalse()
    }

    @Test
    fun notValidAfterForceValidateWhenStateIsNotSelected() {
        userCountry.selectedCountry =
            CountryData("United States", "US", arrayListOf(CountryData("California", "CA", null)))

        userCountry.forceValidate()

        assertThat(userCountry.isValid()).isFalse()
    }
}
