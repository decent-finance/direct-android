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

package com.cexdirect.lib.network.ws

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cexdirect.lib.OpenForTesting
import com.cexdirect.lib.map
import com.google.gson.Gson
import okhttp3.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@OpenForTesting
class CexdSocket(
    private val client: OkHttpClient,
    private val wsUrlProvider: WsUrlProvider,
    private val gson: Gson
) {

    private val subscriptions = HashMap<String, SubscriptionMessage<BaseSocketMessage>>()

    private var webSocket: WebSocket? = null
    private var connected = false
    private val connecting = AtomicBoolean(false)
    private val lastPongTimestamp = AtomicLong(0)

    private val socketMessage = MutableLiveData<String>()
    val parsedMessage = socketMessage.map(Function<String, Pair<String, String>> {
        gson.fromJson(it, BaseSocketMessage::class.java).event to it
    })

    private val pingPongObserver = Observer<Pair<String, String>> { data ->
        if (data.first == "pong") {
            lastPongTimestamp.set(System.currentTimeMillis())
            handler.postDelayed(sendPingRunnable, PING_PONG_DELAY)
        }
    }

    private val ping = gson.toJson(BaseSocketMessage("ping"))
    private val sendPingRunnable = Runnable {
        sendRawMessage(ping)
        handler.postDelayed(checkPongRunnable, MAX_PONG_DELAY)
    }
    private val checkPongRunnable = Runnable {
        if (lastPongTimestamp.get() < System.currentTimeMillis() - MAX_PONG_DELAY) {
            reconnect()
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        handler.post { parsedMessage.observeForever(pingPongObserver) }

        val request = Request.Builder()
            .url(wsUrlProvider.provideWsUrl())
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connected = true
                connecting.set(false)
                sendPingRunnable.run()
                subscriptions.entries.forEach { sendMessage(it.value) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
                Log.e("Socket", response?.message, t)
                reconnect()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Socket", "Received $text")
                socketMessage.postValue(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                // do nothing
            }
        })
    }

    private fun reconnect() {
        if (!connecting.get()) {
            Log.d("Socket", "Reconnecting")
            connecting.compareAndSet(false, true)
            stop()
            start()
        }
    }

    fun stop() {
        handler.removeCallbacks(checkPongRunnable)
        handler.removeCallbacks(sendPingRunnable)
        handler.post { parsedMessage.removeObserver(pingPongObserver) }
        webSocket?.close(CLOSE_STATUS, "Stopped")
    }

    private fun sendRawMessage(msg: String) {
        if (connected) {
            webSocket?.send(msg)
        }
    }

    fun <T : BaseSocketMessage> sendMessage(msg: SubscriptionMessage<T>) {
        subscriptions[msg.invoke().event] = msg
        if (connected) {
            val json = gson.toJson(msg.invoke())
            webSocket?.send(json)
            Log.d("Socket", "Sent $json")
        }
    }

    fun removeSubscriptionByKey(key: String) {
        subscriptions.remove(key)
    }

    fun removeAllSubscriptions() {
        subscriptions.clear()
    }

    companion object {
        const val PING_PONG_DELAY = 10_000L
        const val CLOSE_STATUS = 1000
        const val MAX_PONG_DELAY = 1500L
    }
}

typealias SubscriptionMessage<T> = () -> T
