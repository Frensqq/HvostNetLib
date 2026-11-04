package com.example.netlib.domain.model.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketUser(
    val id: String,
    val email: String,
    val name: String = ""
)

@Serializable
data class HelloEvent(
    val user: WebSocketUser,
    val serverTime: String
)

@Serializable
data class PongEvent(
    val time: String = ""
)

@Serializable
data class ErrorEvent(
    val code: String,
    val message: String = ""
)

@Serializable
data class SubscribedEvent(
    val topic: String,
    val doctor: String? = null,
    val date: String? = null
)

@Serializable
data class UnsubscribedEvent(
    val topic: String,
    val doctor: String? = null,
    val date: String? = null
)

@Serializable
data class SlotTakenEvent(
    val doctor: String,
    val date: String,
    val time: String,
    val start: String
)

@Serializable
data class SlotFreedEvent(
    val doctor: String,
    val date: String,
    val time: String,
    val start: String
)

@Serializable
data class StatusEvent(
    val appointmentId: String,
    val number: Int,
    val status: String,
    val statusLabel: String = "",
    val previous: String = "",
    val cancelReason: String = "",
    val start: String = "",
    val changedAt: String = ""
)

@Serializable
data class DelayEvent(
    val appointmentId: String,
    val number: Int,
    val minutes: Int,
    val newTime: String? = null,
    val message: String = ""
)

@Serializable
data class QueueEvent(
    val appointmentId: String,
    val number: Int,
    val state: String,
    val ahead: Int = 0,
    val etaMin: Int = 0,
    val room: String? = null,
    val delayMin: Int = 0,
    val message: String = "",
    val demo: Boolean = false
)

@Serializable
data class QueueDemoStartedEvent(
    val appointmentId: String,
    val stepSec: Int = 8
)
