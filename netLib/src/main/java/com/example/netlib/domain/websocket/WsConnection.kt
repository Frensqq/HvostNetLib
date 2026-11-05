package com.example.netlib.domain.websocket

interface WsConnection {
    suspend fun connect(token: String)
    suspend fun disconnect()
    suspend fun send(message: String)
    suspend fun listen(onMessage: suspend (String) -> Unit): Int?
}