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

package com.cexdirect.lib.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

sealed class Resource<out T>

class Loading<T> : Resource<T>()
class Success<T>(val data: T?) : Resource<T>()
class Failure<T>(val code: Int = 0, val message: String = "Unknown Error") : Resource<T>()

fun <X, Y : Resource<Z>, Z> Resource<X>.enqueueWith(
    successBlock: (resource: Success<X>) -> LiveData<Resource<Z>>,
    failureBlock: (resource: Failure<X>) -> LiveData<Resource<Z>> = {
        ErrorEvent<Resource<Z>>().apply { postValue(Failure(it.code, it.message)) }
    },
    loadingBlock: (resource: Loading<X>) -> LiveData<Resource<Z>> = {
        LoadingEvent<Resource<Z>>().apply { postValue(Loading()) }
    }
): LiveData<Resource<Z>> {
    return when (this) {
        is Success -> successBlock.invoke(this)
        is Failure -> failureBlock.invoke(this)
        is Loading -> loadingBlock.invoke(this)
    }
}

class LoadingEvent<T> : MutableLiveData<T>()
class ErrorEvent<T> : MutableLiveData<T>()
