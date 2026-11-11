
package com.example.netlib.data.remote

import com.example.netlib.domain.websocket.WsConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText

class KtorWsConnection(
    private val httpClient: HttpClient,
    private val wsUrl: String
) : WsConnection {

    @Volatile
    private var session: DefaultWebSocketSession? = null

    override suspend fun connect(token: String) {
        disconnect()

        session = httpClient.webSocketSession {
            url(wsUrl)
            url.parameters.append("token", token)
        }
    }

    override suspend fun disconnect() {
        val currentSession = session
        session = null
        currentSession?.close()
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

    override suspend fun listen(
        onMessage: suspend (String) -> Unit
    ): Int? {
        val currentSession = session
            ?: throw IllegalStateException("WebSocket is not connected")

        for (frame in currentSession.incoming) {
            when (frame) {
                is Frame.Text -> onMessage(frame.readText())
                is Frame.Close -> {
                    return currentSession.closeReason.await()?.code?.toInt()
                }
                else -> Unit
            }
        }

        return currentSession.closeReason.await()?.code?.toInt()
    }
}
