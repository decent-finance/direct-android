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
    @Singleton
    fun provideOkHttp() =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build()
}
