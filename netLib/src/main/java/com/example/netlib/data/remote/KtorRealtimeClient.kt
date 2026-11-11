
package com.example.netlib.data.remote

import com.example.netlib.data.remote.DTO.websocketDTO.PingOutgoingDTO
import com.example.netlib.data.remote.DTO.websocketDTO.QueueDemoOutgoingDTO
import com.example.netlib.data.remote.DTO.websocketDTO.SubscribeOutgoingDTO
import com.example.netlib.data.remote.DTO.websocketDTO.UnsubscribeOutgoingDTO
import com.example.netlib.domain.model.websocket.ErrorEvent
import com.example.netlib.domain.model.websocket.HelloEvent
import com.example.netlib.domain.model.websocket.WebSocketEvent
import com.example.netlib.domain.websocket.HvostRealtimeClient
import com.example.netlib.domain.websocket.WsConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

class KtorRealtimeClient(
    private val connection: WsConnection,
    private val json: Json,
    private val getToken: () -> String?
) : HvostRealtimeClient {

    private var clientScope: CoroutineScope? = null
    private var listenerJob: Job? = null

    private val lifecycleMutex = Mutex()
    private val subscriptionMutex = Mutex()

    private val _events = MutableSharedFlow<WebSocketEvent>()

    override val events: Flow<WebSocketEvent> = _events.asSharedFlow()

    @Volatile
    private var connected = false

    override val isConnected: Boolean
        get() = connected

    private var lastCloseCode: Int? = null

    private var activeDoctor: String? = null
    private var activeDate: String? = null

    override suspend fun connect() {
        lifecycleMutex.withLock {
            if (listenerJob?.isActive == true) return

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            clientScope = scope

            listenerJob = scope.launch {
                var retryDelay = 1000L

                try {
                    while (isActive) {
                        try {
                            lastCloseCode = null

                            val currentToken = getToken()

                            if (currentToken.isNullOrBlank()) break

                            connection.connect(currentToken)
                            connected = true

                            val closeCode = connection.listen { message ->
                                val event = parse(message, json)

                                if (event is ErrorEvent &&
                                    event.code == "unauthorized"
                                ) {
                                    lastCloseCode = 4001
                                }

                                if (event is HelloEvent) {
                                    retryDelay = 1000L

                                    subscriptionMutex.withLock {
                                        val doctor = activeDoctor
                                        val date = activeDate

                                        if (doctor != null && date != null) {
                                            connection.send(
                                                json.encodeToString(
                                                    SubscribeOutgoingDTO(
                                                        doctor = doctor,
                                                        date = date
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }

                                if (event != null) {
                                    _events.emit(event)
                                }
                            }

                            if (closeCode != null) {
                                lastCloseCode = closeCode
                            }

                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Переподключение выполняется ниже
                        } finally {
                            connected = false
                            connection.disconnect()
                        }

                        if (!isActive || lastCloseCode == 4001) break

                        delay(retryDelay.milliseconds)
                        retryDelay = nextReconnectDelay(retryDelay)
                    }
                } finally {
                    connected = false
                    connection.disconnect()
                }
            }
        }
    }

    override suspend fun disconnect() {
        lifecycleMutex.withLock {
            connected = false

            listenerJob?.cancelAndJoin()
            listenerJob = null

            clientScope?.cancel()
            clientScope = null
        }
    }

    override suspend fun ping() {
        connection.send(json.encodeToString(PingOutgoingDTO()))
    }

    override suspend fun subscribeSlots(doctor: String, date: String) {
        subscriptionMutex.withLock {
            activeDoctor = doctor
            activeDate = date

            if (connected) {
                connection.send(
                    json.encodeToString(
                        SubscribeOutgoingDTO(
                            doctor = doctor,
                            date = date
                        )
                    )
                )
            }
        }
    }

    override suspend fun unsubscribeSlots(
        doctor: String?,
        date: String?
    ) {
        subscriptionMutex.withLock {
            activeDoctor = null
            activeDate = null

            if (connected) {
                connection.send(
                    json.encodeToString(
                        UnsubscribeOutgoingDTO(
                            doctor = doctor,
                            date = date
                        )
                    )
                )
            }
        }
    }

    override suspend fun requestQueueDemo(appointmentId: String) {
        connection.send(
            json.encodeToString(
                QueueDemoOutgoingDTO(appointmentId = appointmentId)
            )
        )
    }
}

internal fun nextReconnectDelay(currentDelay: Long): Long {
    return (currentDelay * 2).coerceAtMost(30_000L)
}
