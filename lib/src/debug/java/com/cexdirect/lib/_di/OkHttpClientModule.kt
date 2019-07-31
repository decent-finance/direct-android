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

package com.cexdirect.lib._di

import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib._di.annotation.HeaderInterceptor
import com.cexdirect.lib._di.annotation.LoggingInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@OpenForTesting
@Module
open class OkHttpClientModule {

    @Provides
    @HeaderInterceptor
    @Singleton
    fun provideHeaderInterceptor() = Interceptor { chain ->
        var request = chain.request()

        val headers = request.headers.newBuilder()
            .add("contentType", "application/json")
            .add("UserAgent", "cexdirect-android-app")
            .build()

        request = request.newBuilder().headers(headers).build()

        chain.proceed(request)
    }

    @Provides
    @LoggingInterceptor
    @Singleton
    fun provideLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        @HeaderInterceptor headerInterceptor: Interceptor,
        @LoggingInterceptor loggingInterceptor: Interceptor
    ) =
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(dummySslSocketFactory(), dummyX509TrustManager)
            .hostnameVerifier(dummyHostnameVerifier())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build()
}
