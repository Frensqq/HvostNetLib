package com.example.netlib.domain.model.websocket

sealed interface WebSocketEvent

data class WebSocketUser(val id: String, val email: String, val name: String = "")

data class HelloEvent(val user: WebSocketUser, val serverTime: String) : WebSocketEvent
data class PongEvent(val time: String = "") : WebSocketEvent
data class ErrorEvent(val code: String, val message: String = "") : WebSocketEvent
data class SubscribedEvent(val topic: String, val doctor: String? = null, val date: String? = null) : WebSocketEvent
data class UnsubscribedEvent(val topic: String, val doctor: String? = null, val date: String? = null) : WebSocketEvent
data class SlotTakenEvent(val doctor: String, val date: String, val time: String, val start: String) : WebSocketEvent
data class SlotFreedEvent(val doctor: String, val date: String, val time: String, val start: String) : WebSocketEvent

data class StatusEvent(
    val appointmentId: String,
    val number: Int,
    val status: String,
    val statusLabel: String = "",
    val previous: String = "",
    val cancelReason: String = "",
    val start: String = "",
    val changedAt: String = ""
) : WebSocketEvent

data class DelayEvent(
    val appointmentId: String,
    val number: Int,
    val minutes: Int,
    val newTime: String? = null,
    val message: String = ""
) : WebSocketEvent

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
) : WebSocketEvent

data class QueueDemoStartedEvent(val appointmentId: String, val stepSec: Int = 8) : WebSocketEvent
