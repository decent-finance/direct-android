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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.annotation.RestrictTo
import com.cexdirect.lib._di.CoreModule
import com.cexdirect.lib._di.DaggerDirectComponent
import com.cexdirect.lib._di.DirectComponent
import com.cexdirect.lib._di.IdentitySubcomponent
import com.cexdirect.lib._network.models.MonetaryData
import com.cexdirect.lib.check.CheckActivity
import com.cexdirect.lib.network.models.RuleData
import java.util.*
import kotlin.collections.HashSet

@SuppressLint("StaticFieldLeak")
object Direct {

    val fingerprint by lazy { UUID.randomUUID().toString() }

    var theme: Theme = Theme.LIGHT

    var credentials: Credentials = Credentials("", "")
    var userEmail = ""

    var pendingFiatAmount = MonetaryData("", "")
    var pendingCryptoAmount = MonetaryData("", "")
    var pendingOrderId = ""

    val rules = HashSet<RuleData>()

    var identitySubcomponent: IdentitySubcomponent? = null
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) get() {
            if (field == null) {
                field = directComponent.identitySubcomponent()
            }
            return field
        }

    lateinit var sourceUri: String
    lateinit var packageName: String
    lateinit var directComponent: DirectComponent
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) get
    lateinit var context: Context

    fun initWith(context: Context) {
        this.context = context
        packageName = context.packageName
        sourceUri = "android://$packageName"
        directComponent = DaggerDirectComponent
            .builder()
            .coreModule(CoreModule(context))
            .build()
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun releaseIdentitySubcomponent() {
        identitySubcomponent = null
    }

    fun startDirect() {
        check(credentials.placementId.isNotBlank() && credentials.secret.isNotBlank())
        Intent(context, CheckActivity::class.java)
            .apply { flags = FLAG_ACTIVITY_NEW_TASK }
            .let { context.startActivity(it) }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setPendingAmounts(fiat: MonetaryData, crypto: MonetaryData) {
        pendingFiatAmount = fiat
        pendingCryptoAmount = crypto
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun clear() {
        pendingFiatAmount = MonetaryData("", "")
        pendingCryptoAmount = MonetaryData("", "")
        pendingOrderId = ""
        userEmail = ""
    }
}
