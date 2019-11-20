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

package com.cexdirect.lib

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialog : BottomSheetDialogFragment() {

    protected inline fun <reified VM : BaseObservableViewModel> viewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(requireActivity(), factory()).get(VM::class.java) }

    protected inline fun <reified VM : BaseObservableViewModel> fragmentViewModelProvider(
        crossinline factory: () -> ViewModelProvider.Factory
    ) =
        lazy { ViewModelProviders.of(this, factory()).get(VM::class.java) }
}
