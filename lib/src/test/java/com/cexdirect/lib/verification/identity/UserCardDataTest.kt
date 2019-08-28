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

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UserCardDataTest {

    lateinit var userCardData: UserCardData

    @Before
    fun setUp() {
        userCardData = UserCardData(mock())
    }

    @Test
    fun cardDataPresent() {
        userCardData.number = "0000 0000 0000 0001"
        userCardData.cvv = "123"
        userCardData.expiry = "11/99"

        assertThat(userCardData.isCardDataPresent()).isTrue()
    }

    @Test
    fun cardDataNotPresent() {
        userCardData.number = ""
        userCardData.cvv = ""
        userCardData.expiry = ""

        assertThat(userCardData.isCardDataPresent()).isFalse()
    }
}
