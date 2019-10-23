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

package com.cexdirect.lib

import androidx.lifecycle.MutableLiveData
import com.cexdirect.lib.network.Failure
import com.cexdirect.lib.network.Loading
import com.cexdirect.lib.network.Resource
import com.cexdirect.lib.network.Success
import com.cexdirect.lib.network.models.ApiResponse
import com.cexdirect.lib.network.models.Extractable
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import retrofit2.HttpException

open class ExecutableLiveData<T : ApiResponse<V>, V>(
    private val scope: CoroutineScope,
    private val block: () -> Deferred<T>
) : MutableLiveData<Resource<V>>() {

    open fun execute() {
        postValue(Loading())
        scope.launch {
            runCatching {
                val response = block().await() as Extractable<V>
                withContext(Dispatchers.Main) {
                    postValue(Success(response.extract()))
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    if (it is HttpException) {
                        val response = it.response()!!.errorBody()!!.string()
                        try {
                            val type = object : TypeToken<ApiResponse<Void>>() {}.type
                            val body = Direct.directComponent.gson()
                                .fromJson<ApiResponse<Void>>(response, type)
                            postValue(Failure(it.code(), body.message ?: ""))
                        } catch (e: Exception) {
                            postValue(Failure(0, it.message ?: ""))
                        }
                    } else {
                        postValue(Failure(0, it.message ?: ""))
                    }
                }
            }
        }
    }
}
