package com.example.netlib.data.webSoket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel

interface WsTransport {
    suspend fun open(url: String, block: suspend WsSession.() -> Unit)
}

/** Прод-реализация через Ktor. */
class KtorWsTransport(
    private val http: HttpClient,
) : WsTransport {
    override suspend fun open(url: String, block: suspend WsSession.() -> Unit) {
        http.webSocket(urlString = url) {
            block(KtorWsSession(this))
        }
    }
}