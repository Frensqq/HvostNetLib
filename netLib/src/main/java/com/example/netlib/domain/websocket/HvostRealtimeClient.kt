package com.example.netlib.domain.websocket

import com.example.netlib.domain.model.websocket.WebSocketEvent
import kotlinx.coroutines.flow.Flow

interface HvostRealtimeClient {
    val events: Flow<WebSocketEvent>
    val isConnected: Boolean

    suspend fun connect(token: String)
    suspend fun disconnect()
    suspend fun ping()

    suspend fun subscribeSlots(doctor: String, date: String)
    suspend fun unsubscribeSlots(doctor: String? = null, date: String? = null)

    suspend fun requestQueueDemo(appointmentId: String)
}