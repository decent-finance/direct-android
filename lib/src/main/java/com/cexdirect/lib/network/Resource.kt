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

import com.cexdirect.lib.Direct
import com.cexdirect.lib.network.models.ApiResponse
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException

sealed class Resource<out T>

class Loading<T> : Resource<T>()
class Success<T>(val data: T? = null) : Resource<T>()
class Failure<T>(val code: Int = 0, val message: String = "Unknown Error") : Resource<T>()

fun <T> Throwable.mapFailure(): Failure<T> =
    if (this is HttpException) {
        val response = this.response()!!.errorBody()!!.string()
        try {
            val type = object : TypeToken<ApiResponse<Void>>() {}.type
            val body = Direct.directComponent.gson()
                .fromJson<ApiResponse<Void>>(response, type)
            Failure<T>(body.code, body.message ?: "Error")
        } catch (e: Exception) {
            Failure<T>(0, this.message ?: "Error")
        }
    } else {
        Failure<T>(0, this.message ?: "Error")
    }
