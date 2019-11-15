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
import com.cexdirect.lib.ExitDialogViewModel
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.buy.CalcActivityViewModel
import com.cexdirect.lib.buy.CalcApi
import com.cexdirect.lib.check.CheckActivityViewModel
import com.cexdirect.lib.check.PlacementApi
import com.cexdirect.lib.di.annotation.BuyActivityFactory
import com.cexdirect.lib.di.annotation.CheckActivityFactory
import com.cexdirect.lib.di.annotation.ErrorActivityFactory
import com.cexdirect.lib.di.annotation.TermsActivityFactory
import com.cexdirect.lib.error.ErrorActivityViewModel
import com.cexdirect.lib.network.ws.Messenger
import com.cexdirect.lib.terms.TermsActivityViewModel
import com.cexdirect.lib.util.PlacementValidator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@OpenForTesting
@Module
class VmModule {

    @Provides
    @BuyActivityFactory
    @Singleton
    fun provideBuyActivityViewModel(
        calcApi: CalcApi,
        stringProvider: StringProvider
    ): ViewModelProvider.Factory =
        CalcActivityViewModel.Factory(calcApi, stringProvider)

    @Provides
    @ErrorActivityFactory
    @Singleton
    fun provideErrorActivityViewModel(messenger: Messenger): ViewModelProvider.Factory =
        ErrorActivityViewModel.Factory(messenger)

    @Provides
    @CheckActivityFactory
    @Singleton
    fun provideCheckActivityViewModel(
        placementApi: PlacementApi,
        placementValidator: PlacementValidator
    ): ViewModelProvider.Factory =
        CheckActivityViewModel.Factory(placementApi, placementValidator)

    @Provides
    @TermsActivityFactory
    @Singleton
    fun provideTermsActivityViewModel(): ViewModelProvider.Factory =
        TermsActivityViewModel.Factory()

    @Provides
    @Singleton
    fun provideExitDialogViewModel() = ExitDialogViewModel.Factory()
}
