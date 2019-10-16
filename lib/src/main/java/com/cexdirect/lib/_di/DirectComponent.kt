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

import com.cexdirect.lib.ExitDialog
import com.cexdirect.lib.buy.BuyFragment
import com.cexdirect.lib.buy.PairSelectionBottomSheetDialog
import com.cexdirect.lib.check.CheckFragment
import com.cexdirect.lib.error.BaseErrorDescriptionFragment
import com.cexdirect.lib.error.ErrorFragment
import com.cexdirect.lib.network.ws.CexdSocket
import com.cexdirect.lib.terms.TermsFragment
import com.google.gson.Gson
import dagger.Component
import javax.inject.Singleton

@Component(modules = [CoreModule::class, OkHttpClientModule::class, NetworkModule::class, VmModule::class])
@Singleton
interface DirectComponent {

    fun identitySubcomponent(): IdentitySubcomponent

    fun inject(baseErrorFragment: BaseErrorDescriptionFragment)

    fun inject(pairSelectionBottomSheetDialog: PairSelectionBottomSheetDialog)

    fun socket(): CexdSocket

    fun inject(exitDialog: ExitDialog)

    fun gson(): Gson

    fun inject(checkFragment: CheckFragment)

    fun inject(termsFragment: TermsFragment)

    fun inject(errorFragment: ErrorFragment)

    fun inject(buyFragment: BuyFragment)
}
