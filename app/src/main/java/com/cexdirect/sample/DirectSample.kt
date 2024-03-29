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

package com.cexdirect.sample

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.cexdirect.lib.Credentials
import com.cexdirect.lib.Direct
import com.cexdirect.lib.OrderStatusCallback
import com.cexdirect.lib.network.models.OrderStatus

@SuppressLint("Registered")
@Suppress("unused")
open class DirectSample : Application() {

    override fun onCreate() {
        super.onCreate()
        Direct.credentials = Credentials(BuildConfig.PLACEMENT_ID, BuildConfig.SECRET)
        Direct.registerOrderStatusCallback(object : OrderStatusCallback {
            override fun onOrderStatusChanged(newStatus: OrderStatus, orderId: String?) {
                Log.w("ORDER", "${newStatus.name} $orderId")
            }
        })
    }
}
