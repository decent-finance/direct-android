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

import com.cexdirect.lib.ExitDialog
import com.cexdirect.lib.buy.CalcActivity
import com.cexdirect.lib.buy.PairSelectionBottomSheetDialog
import com.cexdirect.lib.check.CheckActivity
import com.cexdirect.lib.error.BaseErrorFragment
import com.cexdirect.lib.error.ErrorActivity
import com.cexdirect.lib.network.ws.LiveSocket
import com.cexdirect.lib.terms.TermsActivity
import com.google.gson.Gson
import dagger.Component
import javax.inject.Singleton

@Component(modules = [CoreModule::class, OkHttpClientModule::class, NetworkModule::class, VmModule::class])
@Singleton
interface DirectComponent {

    fun identitySubcomponent(): IdentitySubcomponent

    fun inject(checkActivity: CheckActivity)

    fun inject(termsActivity: TermsActivity)

    fun inject(buyActivity: CalcActivity)

    fun inject(errorActivity: ErrorActivity)

    fun inject(baseErrorFragment: BaseErrorFragment)

    fun inject(pairSelectionBottomSheetDialog: PairSelectionBottomSheetDialog)

    fun socket(): LiveSocket

    fun inject(exitDialog: ExitDialog)

    fun gson(): Gson
}
