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

package com.cexdirect.lib.order

import com.cexdirect.lib.network.models.OrderStatus
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.Before
import org.junit.Test

class StatusWatcherTest {

    private lateinit var watcher: StatusWatcher

    @Before
    fun setUp() {
        watcher = StatusWatcher()
    }

    @Test
    fun invokeUpdateFunctionWhenStatusSetToPssReady() {
        val action = mock<() -> Unit>()

        watcher.updateAndDo(OrderStatus.PSS_READY, action)

        verify(action).invoke()
    }

    @Test
    fun dontInvokeUpdateFunctionAfterUnsuccessfulProcessing() {
        val action = mock<() -> Unit>()

        watcher.updateAndDo(OrderStatus.PSS_READY) {}
        watcher.updateAndDo(OrderStatus.PSS_PENDING) {}
        watcher.updateAndDo(OrderStatus.PSS_READY, action)

        verify(action, never()).invoke()
        assertThat(watcher.getStatus()).isEqualTo(OrderStatus.PSS_PENDING)
    }
}
