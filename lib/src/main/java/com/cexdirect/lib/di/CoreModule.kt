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

import android.content.Context
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.StringProvider
import com.cexdirect.lib.util.DH
import com.cexdirect.lib.util.Encryptor
import com.cexdirect.lib.util.PlacementValidator
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@OpenForTesting
@Module
class CoreModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideStringProvider() = StringProvider(context)

    @Provides
    @Singleton
    fun provideEncryptor() = Encryptor()

    @Provides
    @Singleton
    fun provideDH(encryptor: Encryptor) = DH(/*encryptor, SecureRandom()*/)

    @Provides
    @Singleton
    fun providePlacementValidator() = PlacementValidator(context)
}
