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

package com.cexdirect.lib.error

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.cexdirect.lib.BaseFragment
import com.cexdirect.lib.Direct
import com.cexdirect.lib.di.annotation.ErrorActivityFactory
import javax.inject.Inject

abstract class BaseErrorFragment : BaseFragment() {

    @field:[Inject ErrorActivityFactory]
    lateinit var modelFactory: ViewModelProvider.Factory

    protected val model: ErrorActivityViewModel by activityViewModelProvider { modelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Direct.directComponent.inject(this)
        super.onViewCreated(view, savedInstanceState)
    }
}
