package com.example.netlib.data.remote.DTO.websocketDTO

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketUserDTO(val id: String, val email: String, val name: String = "")

@Serializable
data class HelloEventDTO(val user: WebSocketUserDTO, val serverTime: String)

@Serializable
data class PongEventDTO(val time: String = "")

@Serializable
data class ErrorEventDTO(val code: String, val message: String = "")

@Serializable
data class SubscribedEventDTO(val topic: String, val doctor: String? = null, val date: String? = null)

@Serializable
data class UnsubscribedEventDTO(val topic: String, val doctor: String? = null, val date: String? = null)

@Serializable
data class SlotTakenEventDTO(val doctor: String, val date: String, val time: String, val start: String)

@Serializable
data class SlotFreedEventDTO(val doctor: String, val date: String, val time: String, val start: String)

@Serializable
data class StatusEventDTO(
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
data class DelayEventDTO(
    val appointmentId: String,
    val number: Int,
    val minutes: Int,
    val newTime: String? = null,
    val message: String = ""
)

@Serializable
data class QueueEventDTO(
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
data class QueueDemoStartedEventDTO(val appointmentId: String, val stepSec: Int = 8)
