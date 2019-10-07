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

package com.cexdirect.lib.verification

import com.cexdirect.lib.network.models.OrderStatus
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class StatusWatcher(initial: OrderStatus = OrderStatus.INCOMPLETE) {

    private val currentStatus = AtomicReference(initial)
    private val screenChanged = AtomicBoolean(false)

    fun updateAndDo(newStatus: OrderStatus, updateAction: () -> Unit) {
        if (isUpdatedStatus(newStatus) or screenWasChanged(newStatus)) {
            currentStatus.set(newStatus)
            updateAction.invoke()
        }
    }

    private fun screenWasChanged(newStatus: OrderStatus) =
        currentStatus.get() == newStatus && screenChanged.getAndSet(false)

    private fun isUpdatedStatus(newStatus: OrderStatus) =
        currentStatus.get() != newStatus

    fun setScreenChanged() {
        screenChanged.compareAndSet(false, true)
    }

    fun getStatus(): OrderStatus = currentStatus.get()
}
