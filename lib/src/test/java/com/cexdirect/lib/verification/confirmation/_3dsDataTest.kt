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

package com.cexdirect.lib.verification.confirmation

import com.cexdirect.lib.Direct
import com.cexdirect.lib.network.models._3DsExtras
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class _3dsDataTest {

    lateinit var _3dsData: _3dsData

    @Before
    fun setUp() {
        _3dsData = _3dsData()
    }

    @Test
    fun setTermUrl() {
        Direct.pendingOrderId = "foo"
        _3dsData.txId = "bar"

        assertThat(_3dsData.termUrl).isNotBlank().contains("foo", "bar")
    }

    @Test
    fun haveData() {
        _3dsData.apply {
            _3dsUrl = "https://example.com"
            _3dsExtras = _3DsExtras()
        }

        assertThat(_3dsData.hasData()).isTrue()
    }

    @Test
    fun haveNoData() {
        _3dsData.apply {
            _3dsUrl = ""
            _3dsExtras = null
        }

        assertThat(_3dsData.hasData()).isFalse()
    }
}
