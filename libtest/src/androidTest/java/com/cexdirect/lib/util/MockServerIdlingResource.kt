/*
 *    Copyright 2019 CEX.​IO Ltd (UK)
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

package com.cexdirect.lib.util

import androidx.test.espresso.IdlingResource
import okhttp3.mockwebserver.MockWebServer
import java.util.*
import kotlin.concurrent.timerTask

class MockServerIdlingResource(private val mockWebServer: MockWebServer) : IdlingResource {

    private var callback: IdlingResource.ResourceCallback? = null

    private var idle = false

    init {
        Timer().schedule(timerTask { idle = true }, 3_000)
    }

    override fun getName() = "mock-server"

    override fun isIdleNow(): Boolean {
        if (mockWebServer.requestCount >= 2 && idle) {
            callback?.onTransitionToIdle()
            return true
        }

        return false
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }
}
