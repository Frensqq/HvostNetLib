package com.example.netlib.domain.model.websocket


import kotlinx.serialization.Serializable

@Serializable
data class PingOutgoing(
    val type: String = "ping"
)

@Serializable
data class SubscribeOutgoing(
    val type: String = "subscribe",
    val topic: String = "slots",
    val doctor: String,
    val date: String
)

@Serializable
data class UnsubscribeOutgoing(
    val type: String = "unsubscribe",
    val topic: String = "slots",
    val doctor: String? = null,
    val date: String? = null
)

@Serializable
data class QueueDemoOutgoing(
    val type: String = "queue_demo",
    val appointmentId: String
)