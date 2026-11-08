package com.example.netlib.data.remote

import com.example.netlib.domain.model.websocket.*
import com.example.netlib.domain.websocket.HvostRealtimeClient
import com.example.netlib.domain.websocket.WsConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class KtorRealtimeClient(
    private val connection: WsConnection,
    private val json: Json
) : HvostRealtimeClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<Any>(replay = 1)

    override val events: Flow<Any> = _events.asSharedFlow()

    private var lastCloseCode: Int? = null
    private var listenerJob: Job? = null

    override var isConnected: Boolean = false
        private set

    private var activeDoctor: String? = null
    private var activeDate: String? = null

    override suspend fun connect(token: String) {
        if (listenerJob?.isActive == true) return

        listenerJob = scope.launch {
            var retryDelay = 1_000L

            while (isActive) {
                try {
                    lastCloseCode = null
                    connection.connect(token)
                    isConnected = true

                    val closeCode = connection.listen { message ->
                        val event = parse(
                            message,
                            json
                        )

                        if (event is ErrorEvent && event.code == "unauthorized") {
                            lastCloseCode = 4001
                        }

                        if (event is HelloEvent) {
                            retryDelay = 1_000L

                            val doctor = activeDoctor
                            val date = activeDate

                            if (doctor != null && date != null) {
                                val subscribeMessage = json.encodeToString(
                                    SubscribeOutgoing(
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

                    isConnected = false
                    connection.disconnect()

                    if (lastCloseCode == 4001) break

                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    isConnected = false
                    connection.disconnect()
                }

                if (!isActive || lastCloseCode == 4001) break

                delay(retryDelay)
                retryDelay = nextReconnectDelay(retryDelay)
            }

            isConnected = false
        }
    }

    override suspend fun disconnect() {
        listenerJob?.cancel()
        listenerJob = null
        connection.disconnect()
        isConnected = false
    }

    override suspend fun ping() {
        val message = json.encodeToString(PingOutgoing())
        connection.send(message)
    }

    override suspend fun subscribeSlots(doctor: String, date: String) {
        val message = json.encodeToString(
            SubscribeOutgoing(
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
            UnsubscribeOutgoing(
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
            QueueDemoOutgoing(appointmentId = appointmentId)
        )

        connection.send(message)
    }
}

internal fun nextReconnectDelay(currentDelay: Long): Long {
    return (currentDelay * 2).coerceAtMost(30_000L)
}