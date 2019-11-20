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

package com.cexdirect.lib.order.identity

import com.cexdirect.lib.util.FieldStatus
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
    fun cardDataIsValid() {
        userCardData.number = "0000 0000 0000 0001"
        userCardData.cvv = "123"
        userCardData.expiry = "11/99"

        assertThat(userCardData.isValid()).isTrue()
    }

    @Test
    fun cardDataIsNotValid() {
        userCardData.number = "4321"
        userCardData.cvv = "1"
        userCardData.expiry = "23/11"

        assertThat(userCardData.isValid()).isFalse()
    }

    @Test
    fun dataIsNotValidAfterForceValidate() {
        userCardData.apply {
            number = ""
            cvv = ""
            expiry = ""
        }

        userCardData.forceValidate()

        assertThat(userCardData.isValid()).isFalse()
    }

    @Test
    fun dataIsValidAfterForceValidate() {
        userCardData.apply {
            number = "0000 0000 0000 0001"
            cvv = "123"
            expiry = "11/99"
        }

        userCardData.forceValidate()

        assertThat(userCardData.isValid()).isTrue()
    }

    @Test
    fun cardNumberIsValid() {
        userCardData.number = "0000 0000 0000 1234"

        assertThat(userCardData.numberStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun cardNumberIsNotValid() {
        userCardData.number = "0000 0000 0000 123"

        assertThat(userCardData.numberStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun cardNumberIsEmpty() {
        userCardData.number = ""

        assertThat(userCardData.numberStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun cvvIsValid() {
        userCardData.cvv = "123"

        assertThat(userCardData.cvvStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun cvvIsNotValid() {
        userCardData.cvv = "7"

        assertThat(userCardData.cvvStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun cvvIsEmpty() {
        userCardData.cvv = ""

        assertThat(userCardData.cvvStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun expDateIsValid() {
        userCardData.expiry = "01/26"

        assertThat(userCardData.expiryStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun expDateIsInvalidWithIllegalMonth() {
        userCardData.expiry = "13/24"

        assertThat(userCardData.expiryStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun expDateIsInvalidWithIllegalYear() {
        userCardData.expiry = "04/08"

        assertThat(userCardData.expiryStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun expDateIsInvalidWhenIncomplete() {
        userCardData.expiry = "04/1"

        assertThat(userCardData.expiryStatus).isEqualTo(FieldStatus.INVALID)
    }
}
