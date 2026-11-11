
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
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

    private val _events = MutableSharedFlow<WebSocketEvent>()

    override val events: Flow<WebSocketEvent> = _events.asSharedFlow()

    private var lastCloseCode: Int? = null

    @Volatile
    private var connected = false

    override val isConnected: Boolean
        get() = connected

    private var activeDoctor: String? = null
    private var activeDate: String? = null

    override suspend fun connect() {
        if (listenerJob?.isActive == true) return

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        clientScope = scope

        listenerJob = scope.launch {
            var retryDelay = 1_000L

            try {
                while (isActive) {
                    try {
                        lastCloseCode = null

                        val currentToken = getToken()

                        if (currentToken.isNullOrBlank()) {
                            break
                        }

                        connection.connect(currentToken)
                        connected = true

                        val closeCode = connection.listen { message ->
                            val event = parse(message, json)

                            if (event is ErrorEvent && event.code == "unauthorized") {
                                lastCloseCode = 4001
                            }

                            if (event is HelloEvent) {
                                retryDelay = 1_000L

                                val doctor = activeDoctor
                                val date = activeDate

                                if (doctor != null && date != null) {
                                    val subscribeMessage = json.encodeToString(
                                        SubscribeOutgoingDTO(
                                            doctor = doctor,
                                            date = date
                                        )
                                    )

                                    connection.send(subscribeMessage)
                                }
                            }

                            if (event != null) {
                                _events.emit(event)
                            }
                        }

                        if (closeCode != null) {
                            lastCloseCode = closeCode
                        }

                        connected = false
                        connection.disconnect()

                        if (lastCloseCode == 4001) break

                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
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

    override suspend fun disconnect() {
        connected = false

        listenerJob?.cancel()
        connection.disconnect()
        listenerJob?.join()

        clientScope?.cancel()

        listenerJob = null
        clientScope = null
    }

    override suspend fun ping() {
        val message = json.encodeToString(PingOutgoingDTO())
        connection.send(message)
    }

    override suspend fun subscribeSlots(doctor: String, date: String) {
        val message = json.encodeToString(
            SubscribeOutgoingDTO(
                doctor = doctor,
                date = date
            )
        )

        connection.send(message)

        activeDoctor = doctor
        activeDate = date
    }

    override suspend fun unsubscribeSlots(doctor: String?, date: String?) {
        val message = json.encodeToString(
            UnsubscribeOutgoingDTO(
                doctor = doctor,
                date = date
            )
        )

        connection.send(message)

        activeDoctor = null
        activeDate = null
    }

    override suspend fun requestQueueDemo(appointmentId: String) {
        val message = json.encodeToString(
            QueueDemoOutgoingDTO(appointmentId = appointmentId)
        )

        connection.send(message)
    }
}

internal fun nextReconnectDelay(currentDelay: Long): Long {
    return (currentDelay * 2).coerceAtMost(30_000L)
}
