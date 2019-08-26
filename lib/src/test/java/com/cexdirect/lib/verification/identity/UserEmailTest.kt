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

import com.cexdirect.lib._util.EmailStatus
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class UserEmailTest {

    lateinit var userEmail: UserEmail

    @Before
    fun setUp() {
        userEmail = UserEmail()
    }

    @Test
    fun empty() {
        userEmail.email = ""

        assertThat(userEmail.emailStatus).isEqualTo(EmailStatus.EMPTY)
    }

    @Test
    fun invalid() {
        userEmail.email = "incorrect_em@il"

        assertThat(userEmail.emailStatus).isEqualTo(EmailStatus.INVALID)
    }

    @Test
    fun valid() {
        userEmail.email = "support@cex.io"

        assertThat(userEmail.emailStatus).isEqualTo(EmailStatus.VALID)
    }
}
