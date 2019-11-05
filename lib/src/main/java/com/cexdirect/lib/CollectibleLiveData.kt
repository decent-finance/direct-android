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

package com.cexdirect.lib

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import java.util.concurrent.atomic.AtomicInteger

class CollectibleLiveData<T : ApiResponse<V>, V, A, C : Collection<D>, D>(
    scope: CoroutineScope,
    initial: A,
    private val items: C,
    private val operation: (acc: A, value: V) -> A,
    private val block: (item: D) -> Deferred<T>
) : LiveData<Resource<A>>() {

    private var accumulator: A = initial
    private val counter = AtomicInteger(0)
    private var current: D? = null
    private val liveData: ExecutableLiveData<T, V> = ExecutableLiveData(scope) {
        block.invoke(current ?: error("current is null"))
    }
    val observer = Observer<Resource<V>> { value ->
        if (value != null) {
            when (value) {
                is Loading -> postValue(Loading())
                is Failure -> postValue(Failure(value.code, value.message))
                is Success -> {
                    val counter = counter.incrementAndGet()
                    accumulator = operation.invoke(accumulator, value.data!!)
                    if (counter >= items.size) {
                        postValue(Success(accumulator))
                    }
                }
            }
        }
    }

    override fun onActive() {
        super.onActive()
        liveData.observeForever(observer)
    }

    override fun onInactive() {
        super.onInactive()
        if (!hasObservers()) {
            liveData.removeObserver(observer)
        }
    }

    fun execute() {
        items.forEach {
            current = it
            liveData.execute()
        }
    }
}
