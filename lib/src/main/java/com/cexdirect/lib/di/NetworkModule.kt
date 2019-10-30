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

package com.cexdirect.lib.di

import com.cexdirect.lib.BuildConfig
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.deserializers.OrderStatusDeserializer
import com.cexdirect.lib.network.models.OrderStatus
import com.cexdirect.lib.network.serializers.DateDeserializer
import com.cexdirect.lib.network.webview.Client
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.network.ws.WsUrlProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton

@OpenForTesting
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideGson() = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .registerTypeAdapter(OrderStatus::class.java, OrderStatusDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson) = Retrofit.Builder()
        .baseUrl(BuildConfig.REST_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideMerchantRepo(retrofit: Retrofit) =
        MerchantApi(retrofit.create(MerchantService::class.java))

    @Provides
    @Singleton
    fun providePaymentRepo(retrofit: Retrofit) =
        PaymentApi(retrofit.create(PaymentService::class.java))

    @Provides
    @Singleton
    fun provideOrderService(retrofit: Retrofit) =
        OrderApi(retrofit.create(OrderService::class.java))

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit) =
        AnalyticsApi(retrofit.create(AnalyticsService::class.java))

    @Provides
    @Singleton
    fun provideWsUrlProvider() = WsUrlProvider()

    @Provides
    @Singleton
    fun provideCexdSocket(client: OkHttpClient, wsUrlProvider: WsUrlProvider, gson: Gson) =
        CexdSocket(client, wsUrlProvider, gson)

    @Provides
    @Singleton
    fun provideMessenger(cexdSocket: CexdSocket, gson: Gson) = Messenger(cexdSocket, gson)

    @Provides
    @Singleton
    fun provideWebViewClient() = Client()
}
