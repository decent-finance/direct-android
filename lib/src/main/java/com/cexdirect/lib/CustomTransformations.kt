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

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations

fun <X> LiveData<X>.filter(predicate: (value: X) -> Boolean): LiveData<X> =
    MediatorLiveData<X>().apply {
        addSource(this@filter) {
            if (predicate.invoke(it)) {
                postValue(it)
            }
        }
    }

fun <X, Y> LiveData<X>.map(mapTransformation: Function<X, Y>): LiveData<Y> =
    Transformations.map(this, mapTransformation)
