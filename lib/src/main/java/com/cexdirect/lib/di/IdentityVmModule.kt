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

import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringLiveEvent
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.di.annotation.IdentityScope
import com.cexdirect.lib.di.annotation.PhotoSourceDialogFactory
import com.cexdirect.lib.di.annotation.VerificationActivityFactory
import com.cexdirect.lib.network.OrderApi
import com.cexdirect.lib.network.PaymentApi
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.verification.VerificationActivityViewModel
import com.cexdirect.lib.verification.confirmation.ChangeEmailDialogViewModel
import com.cexdirect.lib.verification.identity.CvvInfoDialogViewModel
import com.cexdirect.lib.verification.identity.PhotoSourceDialogViewModel
import com.cexdirect.lib.verification.scanner.QrScannerActivityViewModel
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
        emailChangedEvent: StringLiveEvent
    ): ViewModelProvider.Factory =
        VerificationActivityViewModel.Factory(
            paymentApi,
            orderApi,
            stringProvider,
            messenger,
            dh,
            emailChangedEvent
        )

    @Provides
    @PhotoSourceDialogFactory
    @IdentityScope
    fun providePhotoSourceDialogFactory(): ViewModelProvider.Factory =
        PhotoSourceDialogViewModel.Factory()

    @Provides
    @IdentityScope
    fun provideCvvInfoDialogViewModelFactory() = CvvInfoDialogViewModel.Factory()

    @Provides
    @IdentityScope
    fun provideChangeEmailDialogViewModelFactory() = ChangeEmailDialogViewModel.Factory()

    @Provides
    @IdentityScope
    fun provideQrScannerViewModelFactory() = QrScannerActivityViewModel.Factory()
}
