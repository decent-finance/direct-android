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

import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringLiveEvent
import com.cexdirect.lib.di.annotation.IdentityScope
import com.cexdirect.lib.order.events.SourceClickEvent
import dagger.Module
import dagger.Provides

@OpenForTesting
@Module
class SharedStateModule {

    @Provides
    @IdentityScope
    fun provideSourceClickEvent() = SourceClickEvent()

    @Provides
    @IdentityScope
    fun provideEmailChangedEvent() = StringLiveEvent()
}
