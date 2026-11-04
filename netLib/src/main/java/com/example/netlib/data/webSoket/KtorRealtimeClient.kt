package com.example.netlib.data.webSoket

import com.example.netlib.domain.model.webSoket.ClosedEvent
import com.example.netlib.domain.model.webSoket.ErrorEvent
import com.example.netlib.domain.model.webSoket.HelloEvent
import com.example.netlib.domain.webSoket.HvostRealtimeClient
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Ktor WebSocket-клиент «Хвост».
 *
 * Работает через WsTransport — в проде KtorWsTransport,
 * в тестах FakeWsTransport. Поведение (бэкофф, 4001, restore) — в этом классе.
 */
class KtorRealtimeClient(
    private val transport: WsTransport,
    private val baseUrl: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : HvostRealtimeClient {

    private val _events = MutableSharedFlow<Any>(extraBufferCapacity = 64)
    override val events: Flow<Any> = _events.asSharedFlow()

    @Volatile private var connected: Boolean = false
    override val isConnected: Boolean get() = connected

    @Volatile private var session: WsSession? = null
    private var loopJob: Job? = null

    private val subscriptions = mutableSetOf<Pair<String, String>>()
    private val subsLock = Any()

    @Volatile private var userClosed: Boolean = false

    // ------------------------------------------------------------
    //  connect / disconnect
    // ------------------------------------------------------------

    override suspend fun connect(token: String) {
        userClosed = false
        loopJob?.cancel()
        loopJob = scope.launch { runReconnectLoop(token) }
    }

    override suspend fun disconnect() {
        userClosed = true
        loopJob?.cancel()
        loopJob = null
        runCatching { session?.close(CloseReason(CloseReason.Codes.NORMAL, "client logout")) }
        session = null
        connected = false
        synchronized(subsLock) { subscriptions.clear() }
    }

    // ------------------------------------------------------------
    //  Цикл переподключения
    // ------------------------------------------------------------

    private suspend fun runReconnectLoop(token: String) {
        var attempt = 0
        while (!userClosed) {
            try {
                transport.open(buildUrlWithToken(token)) {
                    session = this
                    connected = true
                    try {
                        pumpIncoming(onHello = { attempt = 0 })
                    } finally {
                        connected = false
                        session = null
                    }
                }
                if (userClosed) return
                attempt++
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                val code = extractCloseCode(t)

                if (code == 4001) {
                    connected = false
                    session = null
                    _events.emit(ClosedEvent(4001, "unauthorized"))
                    return
                }

                connected = false
                session = null
                _events.emit(ClosedEvent(code, t.message ?: "disconnected"))
                attempt++
            }

            if (userClosed) return
            delay(ReconnectPolicy.backoffMs(attempt))
        }
    }

    private fun extractCloseCode(t: Throwable): Int {
        if (t is CloseCodeException) return t.code
        val fromMsg = Regex("""(?:code|close)[^\d]*(\d{3,4})""", RegexOption.IGNORE_CASE)
            .find(t.message ?: "")?.groupValues?.get(1)?.toIntOrNull()
        return fromMsg ?: 0
    }

    // ------------------------------------------------------------
    //  Приём
    // ------------------------------------------------------------

    private suspend fun WsSession.pumpIncoming(onHello: () -> Unit) {
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue

                val raw = frame.readText()
                val parsed = WsEventParser.parseIncoming(raw) ?: continue

                if (parsed is HelloEvent) {
                    onHello()
                    restoreSubscriptions()
                }

                if (parsed is ErrorEvent && parsed.code == "unauthorized") {
                    _events.emit(parsed)
                    throw CloseCodeException(4001, "unauthorized")
                }

                _events.emit(parsed)
            }
        } finally {
            val reason = runCatching { closeReason.await() }.getOrNull()
            if (reason != null) {
                throw CloseCodeException(reason.code.toInt(), reason.message)
            }
        }
    }

    private suspend fun WsSession.restoreSubscriptions() {
        val snapshot = synchronized(subsLock) { subscriptions.toList() }
        snapshot.forEach { (doctor, date) ->
            runCatching { send(Frame.Text(WsEventParser.encodeSubscribe(doctor, date))) }
        }
    }

    // ------------------------------------------------------------
    //  Отправка
    // ------------------------------------------------------------

    override suspend fun ping() = sendRaw(WsEventParser.encodePing())

    override suspend fun subscribeSlots(doctor: String, date: String) {
        synchronized(subsLock) { subscriptions += doctor to date }
        sendRaw(WsEventParser.encodeSubscribe(doctor, date))
    }

    override suspend fun unsubscribeSlots(doctor: String?, date: String?) {
        synchronized(subsLock) {
            if (doctor == null && date == null) subscriptions.clear()
            else subscriptions.removeAll { (d, dt) ->
                (doctor == null || d == doctor) && (date == null || dt == date)
            }
        }
        sendRaw(WsEventParser.encodeUnsubscribe(doctor, date))
    }

    override suspend fun requestQueueDemo(appointmentId: String) =
        sendRaw(WsEventParser.encodeQueueDemo(appointmentId))

    private suspend fun sendRaw(text: String) {
        val s = session ?: error("WebSocket не подключён")
        s.send(Frame.Text(text))
    }

    private fun buildUrlWithToken(token: String): String {
        val sep = if (baseUrl.contains('?')) '&' else '?'
        return "$baseUrl${sep}token=$token"
    }
}

/** Бросается из pumpIncoming, чтобы цикл различил коды закрытия. */
class CloseCodeException(val code: Int, message: String) : RuntimeException(message)