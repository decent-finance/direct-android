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
import com.cexdirect.lib.buy.CalcApi
import com.cexdirect.lib.check.PlacementApi
import com.cexdirect.lib.network.*
import com.cexdirect.lib.network.deserializers.OrderStatusDeserializer
import com.cexdirect.lib.network.models.OrderStatus
import com.cexdirect.lib.network.serializers.DateDeserializer
import com.cexdirect.lib.network.webview.Client
import com.cexdirect.lib.network.ws.LiveSocket
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.network.ws.WsUrlProvider
import com.cexdirect.lib.order.OrderProcessingApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Singleton

@OpenForTesting
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateDeserializer())
        .registerTypeAdapter(OrderStatus::class.java, OrderStatusDeserializer())
        .create()

    @Provides
    @Singleton
    fun provideRetrofit(client: Lazy<OkHttpClient>, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.REST_URL)
        .callFactory(object : Call.Factory {
            override fun newCall(request: Request): Call {
                return client.get().newCall(request)
            }
        })
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideMerchantRepo(retrofit: Retrofit) =
        MerchantFlow(retrofit.create(MerchantService::class.java))

    @Provides
    @Singleton
    fun providePaymentRepo(retrofit: Retrofit) =
        PaymentFlow(retrofit.create(PaymentService::class.java))

    @Provides
    @Singleton
    fun provideOrderService(retrofit: Retrofit) =
        OrderFlow(retrofit.create(OrderService::class.java))

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit) =
        AnalyticsFlow(retrofit.create(AnalyticsService::class.java))

    @Provides
    @Singleton
    fun provideCalcApi(
        analyticsFlow: AnalyticsFlow,
        merchantFlow: MerchantFlow,
        paymentFlow: PaymentFlow,
        messenger: Messenger
    ): CalcApi =
        CalcApi(analyticsFlow, merchantFlow, paymentFlow, messenger)

    @Provides
    fun providePlacementApi(
        merchantFlow: MerchantFlow,
        paymentFlow: PaymentFlow
    ): PlacementApi =
        PlacementApi(merchantFlow, paymentFlow)

    @Provides
    fun provideOrderApi(
        paymentFlow: PaymentFlow,
        orderFlow: OrderFlow,
        messenger: Messenger
    ): OrderProcessingApi =
        OrderProcessingApi(paymentFlow, orderFlow, messenger)

    @Provides
    @Singleton
    fun provideWsUrlProvider() = WsUrlProvider()

    @Provides
    @Singleton
    fun provideCexdSocket(client: OkHttpClient, wsUrlProvider: WsUrlProvider, gson: Gson) =
        LiveSocket(client, wsUrlProvider, gson)

    @Provides
    @Singleton
    fun provideMessenger(cexdSocket: LiveSocket, gson: Gson) = Messenger(cexdSocket, gson)

    @Provides
    @Singleton
    fun provideWebViewClient() = Client()
}
