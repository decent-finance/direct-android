package com.cexdirect.lib._di

import dagger.Module
import dagger.Provides
import com.cexdirect.lib._di.annotation.HeaderInterceptor
import com.cexdirect.lib._di.annotation.LoggingInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class OkHttpClientModule {

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
    @Singleton
    fun provideOkHttp(@HeaderInterceptor headerInterceptor: Interceptor) =
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build()
}
