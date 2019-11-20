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

class TermsTest {

    lateinit var terms: Terms

    @Before
    fun setUp() {
        terms = Terms()
    }

    @Test
    fun changeStatusToValid() {
        terms.termsAccepted = true

        assertThat(terms.termsStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun changeStatusToEmpty() {
        terms.termsAccepted = false

        assertThat(terms.termsStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun changeStatusToInvalid() {
        terms.termsAccepted = false

        terms.forceValidate()

        assertThat(terms.termsStatus).isEqualTo(FieldStatus.INVALID)
    }

    @Test
    fun isAccepted() {
        terms.termsAccepted = true

        assertThat(terms.accepted()).isTrue()
    }

    @Test
    fun isAcceptedAfterForceValidate() {
        terms.termsAccepted = true

        terms.forceValidate()

        assertThat(terms.accepted()).isTrue()
    }

    @Test
    fun notAcceptedAfterForceValidate() {
        terms.termsAccepted = false

        terms.forceValidate()

        assertThat(terms.accepted()).isFalse()
    }
}

