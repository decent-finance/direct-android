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

import androidx.test.platform.app.InstrumentationRegistry
import com.cexdirect.lib._di.CoreModule
import com.cexdirect.lib._di.DirectComponent
import com.cexdirect.lib._di.OkHttpClientModule
import com.cexdirect.lib.di.MockNetworkModule
import com.cexdirect.lib.di.MockVmModule
import it.cosenonjaviste.daggermock.DaggerMockRule

class DirectNetworkMockRule : DaggerMockRule<DirectComponent>(
    DirectComponent::class.java,
    CoreModule(InstrumentationRegistry.getInstrumentation().targetContext),
    OkHttpClientModule(),
    MockNetworkModule(),
    MockVmModule()
) {
    init {
        set { Direct.directComponent = it }
    }
}
