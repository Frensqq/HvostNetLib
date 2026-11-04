package com.example.netlib.data.remote

import com.example.netlib.domain.websocket.WsConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.send
import io.ktor.client.request.url
import io.ktor.websocket.readText
import io.ktor.websocket.readReason
class KtorWsConnection(
    private val httpClient: HttpClient,
    private val wsUrl: String
) : WsConnection {
    private var session: WebSocketSession? = null

    override suspend fun connect(token: String) {
        session = httpClient.webSocketSession {
            url(wsUrl)
            url.parameters.append("token", token)
        }
    }

    override suspend fun disconnect() {
        session?.close()
        session = null
    }

    override suspend fun send(message: String) {
        val currentSession = session
            ?: throw IllegalStateException("WebSocket is not connected")

        currentSession.send(Frame.Text(message))
    }

    suspend fun receive(): String? {
        val currentSession = session
            ?: throw IllegalStateException("WebSocket is not connected")

        return when (val frame = currentSession.incoming.receive()) {
            is Frame.Text -> frame.readText()
            else -> null
        }
    }

    override suspend fun listen(onMessage: suspend (String) -> Unit): Int? {
        val currentSession = session
            ?: throw IllegalStateException("WebSocket is not connected")

        var closeCode: Int? = null

        for (frame in currentSession.incoming) {
            when (frame) {
                is Frame.Text -> onMessage(frame.readText())
                is Frame.Close -> {
                    closeCode = frame.readReason()?.code?.toInt()
                }
                else -> Unit
            }
        }

        return closeCode
    }
}
