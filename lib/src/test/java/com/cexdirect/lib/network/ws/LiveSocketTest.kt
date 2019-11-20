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

package com.cexdirect.lib.network.ws

import com.cexdirect.lib.network.models.OrderInfoBody
import com.cexdirect.lib.network.models.OrderInfoCredentials
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import okhttp3.*
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LiveSocketTest {

    @Mock
    lateinit var client: OkHttpClient

    @Mock
    lateinit var webSocket: WebSocket

    private lateinit var liveSocket: LiveSocket

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(client.newWebSocket(any(), any())).thenReturn(webSocket)
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson())
    }

    @After
    fun tearDown() {
        reset(client, webSocket)
    }

    @Test
    fun addSubscription() {
        liveSocket.sendMessage { givenOrderInfoSubscription() }

        assertThat(liveSocket.subscriptions).hasSize(1).containsKey("orderInfo")
    }

    @Test
    fun removeSubscription() {
        liveSocket.sendMessage { givenOrderInfoSubscription() }

        liveSocket.removeSubscriptionByKey("orderInfo")

        assertThat(liveSocket.subscriptions).isEmpty()
    }

    @Test
    fun sendMessagesOnStart() {
        liveSocket.sendMessage { givenOrderInfoSubscription() }

        liveSocket.start()
        liveSocket.listener.onOpen(webSocket, givenResponse())

        verify(webSocket).send(argWhere<String> { it.contains("ping") })
        verify(webSocket).send(argWhere<String> { it.contains("orderInfo") })
    }

    private fun givenResponse(): Response =
        Response.Builder()
            .code(200)
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .message("")
            .build()

    private fun givenOrderInfoSubscription(): OrderInfoSubscription =
        OrderInfoSubscription(
            OrderInfoBody(
                OrderInfoCredentials("abc123", "def456")
            ), "orderInfo"
        )
}
