package com.example.netlib.data.remote.DTO.websocketDTO

import kotlinx.serialization.Serializable

@Serializable
internal data class PingOutgoingDTO(val type: String = "ping")

@Serializable
internal data class SubscribeOutgoingDTO(
    val type: String = "subscribe",
    val topic: String = "slots",
    val doctor: String,
    val date: String
)

@Serializable
internal data class UnsubscribeOutgoingDTO(
    val type: String = "unsubscribe",
    val topic: String = "slots",
    val doctor: String? = null,
    val date: String? = null
)

@Serializable
internal data class QueueDemoOutgoingDTO(val type: String = "queue_demo", val appointmentId: String)
