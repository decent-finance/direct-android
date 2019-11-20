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
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UserSsnTest {

    lateinit var userSsn: UserSsn

    @Before
    fun setUp() {
        userSsn = UserSsn()
    }

    @Test
    fun empty() {
        userSsn.ssn = ""

        assertThat(userSsn.ssnStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun invalid() {
        userSsn.ssn = "12-345-0011"

        assertThat(userSsn.ssnStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun valid() {
        userSsn.ssn = "111-22-3456"

        assertThat(userSsn.ssnStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun invalidAfterForceValidate() {
        userSsn.ssn = ""

        userSsn.forceValidate()

        assertThat(userSsn.ssnStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun validAfterForceValidate() {
        userSsn.ssn = "111-22-3456"

        userSsn.forceValidate()

        assertThat(userSsn.ssnStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun returnFormattedValue() {
        userSsn.ssn = "111-22-3456"

        val actual = userSsn.getFormattedValue()

        assertThat(actual).doesNotContain("-")
    }
}
