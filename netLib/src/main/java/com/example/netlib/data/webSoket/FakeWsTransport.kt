package com.example.netlib.data.webSoket


import com.example.netlib.data.webSoket.WsSession
import com.example.netlib.data.webSoket.WsTransport
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Тестовый транспорт: управляемое WebSocket-соединение без сети.
 *
 * - openCount — сколько раз клиент пытался подключиться (для бэкоффа).
 * - sent — все кадры, отправленные клиентом (для проверки subscribe).
 * - serverSend(raw) — сервер шлёт клиенту текстовый кадр.
 * - serverClose(code, reason) — сервер закрывает соединение с кодом.
 * - serverDrop() — соединение «оборвалось» без кода (эмуляция обрыва сети).
 * - failWith — если задан, open() бросит это исключение (эмуляция «сервер недоступен»).
 */
class FakeWsTransport : WsTransport {

    @Volatile var openCount: Int = 0
        private set

    @Volatile var failWith: Throwable? = null

    private val sentLock = Any()
    private val sentList = mutableListOf<String>()

    @Volatile private var current: FakeSession? = null

    override suspend fun open(url: String, block: suspend WsSession.() -> Unit) {
        openCount++
        failWith?.let { throw it }

        val session = FakeSession(
            onSend = { raw -> synchronized(sentLock) { sentList += raw } },
        )
        current = session
        try {
            session.runBlock(block)
        } finally {
            current = null
        }
    }

    suspend fun serverSend(raw: String) {
        current?.serverSend(raw) ?: error("FakeWsTransport: нет открытой сессии")
    }

    suspend fun serverClose(code: Int, reason: String = "") {
        current?.serverClose(code, reason) ?: error("FakeWsTransport: нет открытой сессии")
    }

    suspend fun serverDrop() {
        current?.serverDrop() ?: error("FakeWsTransport: нет открытой сессии")
    }

    fun sentSnapshot(): List<String> = synchronized(sentLock) { sentList.toList() }

    // ============================================================
    //  FakeSession
    // ============================================================

    private class FakeSession(
        private val onSend: (String) -> Unit,
    ) : WsSession {

        private val incomingChannel = Channel<Frame>(Channel.UNLIMITED)
        private val closed = CompletableDeferred<CloseReason?>()

        override val incoming: ReceiveChannel<Frame> get() = incomingChannel
        override val closeReason: Deferred<CloseReason?> get() = closed

        override suspend fun send(frame: Frame) {
            if (frame is Frame.Text) onSend(frame.readText())
        }

        override suspend fun close(reason: CloseReason) {
            closed.complete(reason)
            incomingChannel.close()
        }

        /** Дать клиенту пожить в block, пока не закроется incoming. */
        suspend fun runBlock(block: suspend WsSession.() -> Unit) {
            try {
                block()
            } finally {
                if (!closed.isCompleted) closed.complete(null)
                incomingChannel.close()
            }
        }

        suspend fun serverSend(raw: String) {
            incomingChannel.send(Frame.Text(raw))
        }

        suspend fun serverClose(code: Int, reason: String) {
            closed.complete(CloseReason(code.toShort(), reason))
            incomingChannel.close()
        }

        suspend fun serverDrop() {
            closed.complete(null)
            incomingChannel.close()
        }
    }
}