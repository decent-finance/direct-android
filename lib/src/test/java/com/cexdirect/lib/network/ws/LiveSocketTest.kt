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

import android.os.Handler
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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class LiveSocketTest {

    @Mock
    lateinit var client: OkHttpClient

    @Mock
    lateinit var webSocket: WebSocket

    @Mock
    lateinit var handler: Handler

    private lateinit var executor: ScheduledExecutorService
    private lateinit var liveSocket: LiveSocket

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(client.newWebSocket(any(), any())).thenReturn(webSocket)

        executor = Executors.newSingleThreadScheduledExecutor()

        doAnswer {
            val runnable = it.getArgument<Runnable>(0)
            val delay = it.getArgument<Long>(1)
            executor.schedule(runnable, delay, TimeUnit.MILLISECONDS)
            true
        }.whenever(handler).postDelayed(any(), any())
    }

    @After
    fun tearDown() {
        reset(client, webSocket)
        executor.shutdownNow()
    }

    @Test
    fun addSubscription() {
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson())

        liveSocket.sendMessage { givenOrderInfoSubscription() }

        assertThat(liveSocket.subscriptions).hasSize(1).containsKey("orderInfo")
    }

    @Test
    fun dontAddSubscription() {
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson())

        liveSocket.sendMessage(false) { givenOrderInfoSubscription() }

        assertThat(liveSocket.subscriptions).isEmpty()
    }

    @Test
    fun removeSubscription() {
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson())

        liveSocket.sendMessage { givenOrderInfoSubscription() }

        liveSocket.removeSubscriptionByKey("orderInfo")

        assertThat(liveSocket.subscriptions).isEmpty()
    }

    @Test
    fun sendMessagesOnStart() {
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson())

        liveSocket.sendMessage { givenOrderInfoSubscription() }

        liveSocket.start()
        liveSocket.listener.onOpen(webSocket, givenResponse())

        verify(webSocket).send(argWhere<String> { it.contains("ping") })
        verify(webSocket).send(argWhere<String> { it.contains("orderInfo") })
    }

    @Test
    fun startSocket() {
        val urlProvider = mock<WsUrlProvider> {
            on { provideWsUrl() } doReturn "wss://example.com"
        }
        liveSocket = LiveSocket(client, urlProvider, Gson(), handler)

        liveSocket.start()

        verify(handler).post(notNull())
        verify(urlProvider).provideWsUrl()
        verify(client).newWebSocket(any(), eq(liveSocket.listener))
    }

    @Test
    fun stopSocket() {
        liveSocket = LiveSocket(client, WsUrlProvider(), Gson(), handler)

        liveSocket.start()
        liveSocket.stop()

        verify(handler).removeCallbacks(eq(liveSocket.checkPongRunnable))
        verify(handler).removeCallbacks(eq(liveSocket.sendPingRunnable))
        verify(webSocket).close(eq(LiveSocket.CLOSE_STATUS), eq("Stopped"))
    }

    @Test
    fun callStopAndStartOnReconnect() {
        liveSocket = spy(LiveSocket(client, WsUrlProvider(), Gson(), handler))

        liveSocket.reconnect()

        verify(liveSocket).stop()
        verify(liveSocket).start()
    }

    @Test
    fun reconnectWhenMsgWasNotSent() {
        whenever(webSocket.send(any<String>())).thenReturn(false)

        liveSocket = spy(LiveSocket(client, WsUrlProvider(), Gson(), handler))

        liveSocket.sendMessage(false) { givenOrderInfoSubscription() }

        verify(liveSocket).reconnect()
    }

    @Test
    fun reconnectWhenRawMsgWasNotSent() {
        whenever(webSocket.send(any<String>())).thenReturn(false)

        liveSocket = spy(LiveSocket(client, WsUrlProvider(), Gson(), handler))

        liveSocket.sendRawMessage("hi!")

        verify(liveSocket).reconnect()
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
