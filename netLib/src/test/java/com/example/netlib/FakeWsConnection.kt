package com.example.netlib

import com.example.netlib.domain.websocket.WsConnection

class FakeWsConnection : WsConnection {

    val sentMessages = mutableListOf<String>()

    var message = """
        {
            "type": "hello",
            "user": {
                "id": "user-1",
                "email": "test@mail.com",
                "name": "Test"
            },
            "serverTime": "2026-11-11T10:00:00Z"
        }
    """.trimIndent()

    var connectCount = 0
    var listenCount = 0

    var disconnectFirst = true
    var closeCode: Int? = null

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

        onMessage(message)

        if (disconnectFirst && listenCount == 1) {
            return null
        }

        return closeCode
    }
}