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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.cexdirect.lib.util.observeOnce
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChangeEmailDialogViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var model: ChangeEmailDialogViewModel

    @Before
    fun setUp() {
        model = ChangeEmailDialogViewModel()
    }

    @Test
    fun saveEmail() {
        model.saveEmail()
        model.saveEvent.observeOnce { assertThat(it).isNull() }
    }

    @Test
    fun cancelDialog() {
        model.cancel()
        model.cancelEvent.observeOnce { assertThat(it).isNull() }
    }
}
