/*
 * Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.util

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PlacementValidatorTest {

    private lateinit var context: Context
    private lateinit var validator: PlacementValidator

    @Before
    internal fun setUp() {
        context = mock {
            on { packageName } doReturn "com.cexdirect.direct.sample"
        }
        validator = PlacementValidator(context)
    }

    @Test
    fun validateGivenUri() {
        val actual = validator.isPlacementUriAllowed("android://com.cexdirect.direct.sample")
        assertThat(actual).isTrue()
    }
}
