package com.example.netlib

import com.example.netlib.domain.websocket.WsConnection
import kotlinx.coroutines.CompletableDeferred

class FakeWsConnection : WsConnection {

    val sentMessages = mutableListOf<String>()

    var message = """{"type":"hello"}"""

    var connectCount = 0
    var listenCount = 0

    var disconnectFirst = true
    var closeCode: Int? = null

    var firstListenGate: CompletableDeferred<Unit>? = null

    override suspend fun connect(token: String) {
        connectCount++
    }

    override suspend fun disconnect() {
    }

    override suspend fun send(message: String) {
        sentMessages.add(message)
    }

    override suspend fun listen(onMessage: suspend (String) -> Unit): Int? {
        listenCount++

        if (disconnectFirst && listenCount == 1) {
            onMessage(message)
            firstListenGate?.await()
            return null
        }

        onMessage(message)
        return closeCode
    }
}