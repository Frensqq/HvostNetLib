package com.example.netlib.data.webSoket

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel

interface WsSession {
    val incoming: ReceiveChannel<Frame>
    val closeReason: Deferred<CloseReason?>
    suspend fun send(frame: Frame)
    suspend fun close(reason: CloseReason = CloseReason(CloseReason.Codes.NORMAL, ""))
}

/** Обёртка над реальной Ktor-сессией. */
class KtorWsSession(
    private val delegate: DefaultClientWebSocketSession,
) : WsSession {
    override val incoming get() = delegate.incoming
    override val closeReason get() = delegate.closeReason
    override suspend fun send(frame: Frame) = delegate.send(frame)
    override suspend fun close(reason: CloseReason) = delegate.close(reason)
}