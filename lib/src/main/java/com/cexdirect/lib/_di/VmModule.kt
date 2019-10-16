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

import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.CoroutineDispatcherProvider
import com.cexdirect.lib.ExitDialogViewModel
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib._di.annotation.BuyActivityFactory
import com.cexdirect.lib._di.annotation.CheckActivityFactory
import com.cexdirect.lib._di.annotation.ErrorActivityFactory
import com.cexdirect.lib._di.annotation.TermsActivityFactory
import com.cexdirect.lib.buy.BuyActivityViewModel
import com.cexdirect.lib.check.CheckFragmentViewModel
import com.cexdirect.lib.check.RuleIds
import com.cexdirect.lib.error.ErrorActivityViewModel
import com.cexdirect.lib.network.AnalyticsApi
import com.cexdirect.lib.network.MerchantApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.terms.TermsFragmentViewModel
import com.cexdirect.lib.util.PlacementValidator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@OpenForTesting
@Module
class VmModule {

    @Provides
    @Singleton
    fun provideCoroutineDispatcherProvider() = CoroutineDispatcherProvider()

    @Provides
    @BuyActivityFactory
    @Singleton
    fun provideBuyActivityViewModel(
        merchantApi: MerchantApi,
        paymentApi: PaymentApi,
        analyticsApi: AnalyticsApi,
        messenger: Messenger,
        coroutineDispatcherProvider: CoroutineDispatcherProvider,
        stringProvider: StringProvider
    ): ViewModelProvider.Factory =
        BuyActivityViewModel.Factory(
            merchantApi,
            paymentApi,
            analyticsApi,
            messenger,
            coroutineDispatcherProvider,
            stringProvider
        )

    @Provides
    @ErrorActivityFactory
    @Singleton
    fun provideErrorActivityViewModel(
        messenger: Messenger,
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        ErrorActivityViewModel.Factory(messenger, coroutineDispatcherProvider)

    @Provides
    @CheckActivityFactory
    @Singleton
    fun provideCheckActivityViewModel(
        merchantApi: MerchantApi,
        paymentApi: PaymentApi,
        placementValidator: PlacementValidator,
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        CheckFragmentViewModel.Factory(
            merchantApi,
            paymentApi,
            placementValidator,
            RuleIds(),
            coroutineDispatcherProvider
        )

    @Provides
    @TermsActivityFactory
    @Singleton
    fun provideTermsActivityViewModel(
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        TermsFragmentViewModel.Factory(coroutineDispatcherProvider)

    @Provides
    @Singleton
    fun provideExitDialogViewModel(
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ) =
        ExitDialogViewModel.Factory(coroutineDispatcherProvider)
}
