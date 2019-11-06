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

class UserWalletTest {

    private lateinit var userWallet: UserWallet

    @Before
    fun setUp() {
        userWallet = UserWallet()
    }

    @Test
    fun valid() {
        userWallet.needsTag = false
        userWallet.address = "test"

        assertThat(userWallet.walletStatus).isEqualTo(FieldStatus.VALID)
    }

    @Test
    fun empty() {
        userWallet.needsTag = false
        userWallet.address = ""

        assertThat(userWallet.walletStatus).isEqualTo(FieldStatus.EMPTY)
    }

    @Test
    fun invalidAfterForceValidate() {
        userWallet.address = ""

        userWallet.forceValidate()

        assertThat(userWallet.walletStatus).isEqualTo(FieldStatus.INVALID)
    }
}
