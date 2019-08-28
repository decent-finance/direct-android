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
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringLiveEvent
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib._di.annotation.*
import com.cexdirect.lib._util.DH
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.verification.VerificationActivityViewModel
import com.cexdirect.lib.verification.confirmation.ChangeEmailDialogViewModel
import com.cexdirect.lib.verification.confirmation.PaymentConfirmationFragmentViewModel
import com.cexdirect.lib.verification.identity.CvvInfoDialogViewModel
import com.cexdirect.lib.verification.identity.PhotoSourceDialogViewModel
import com.cexdirect.lib.verification.receipt.ReceiptFragmentViewModel
import dagger.Module
import dagger.Provides

@OpenForTesting
@Module
class IdentityVmModule {

    @Provides
    @VerificationActivityFactory
    @IdentityScope
    fun provideVerificationActivityViewModel(
        paymentApi: PaymentApi,
        orderApi: OrderApi,
        stringProvider: StringProvider,
        messenger: Messenger,
        dh: DH,
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        VerificationActivityViewModel.Factory(
            paymentApi,
            orderApi,
            stringProvider,
            messenger,
            dh,
            coroutineDispatcherProvider
        )

    @Provides
    @ReceiptFragmentFactory
    @IdentityScope
    fun provideReceiptFragmentViewModel(
        messenger: Messenger,
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        ReceiptFragmentViewModel.Factory(messenger, coroutineDispatcherProvider)

    @Provides
    @PaymentConfirmationFragmentFactory
    @IdentityScope
    fun providePaymentConfirmationFragmentViewModel(
        orderApi: OrderApi,
        emailChangedEvent: StringLiveEvent,
        messenger: Messenger,
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        PaymentConfirmationFragmentViewModel.Factory(
            orderApi,
            emailChangedEvent,
            messenger,
            coroutineDispatcherProvider
        )

    @Provides
    @PhotoSourceDialogFactory
    @IdentityScope
    fun providePhotoSourceDialogFactory(
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ): ViewModelProvider.Factory =
        PhotoSourceDialogViewModel.Factory(coroutineDispatcherProvider)

    @Provides
    @IdentityScope
    fun provideCvvInfoDialogViewModelFactory(
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ) =
        CvvInfoDialogViewModel.Factory(coroutineDispatcherProvider)

    @Provides
    @IdentityScope
    fun provideChangeEmailDialogViewModelFactory(
        coroutineDispatcherProvider: CoroutineDispatcherProvider
    ) =
        ChangeEmailDialogViewModel.Factory(coroutineDispatcherProvider)
}
