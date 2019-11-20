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

package com.cexdirect.lib.order.identity.country

import com.cexdirect.lib.network.models.CountryData
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Test

class CountrySorterTest {

    @Test
    fun sortCountriesByName() {
        val given = listOf(
            CountryData("Austria", "AT", null),
            CountryData("Switzerland", "CH", null),
            CountryData("Germany", "DE", null),
            CountryData("Barbados", "BB", null)
        )

        val actual = given.sortByName()

        assertThat(actual)
            .containsExactly(
                CountryData("Austria", "AT", null),
                CountryData("Barbados", "BB", null),
                CountryData("Germany", "DE", null),
                CountryData("Switzerland", "CH", null)
            )
    }
}
