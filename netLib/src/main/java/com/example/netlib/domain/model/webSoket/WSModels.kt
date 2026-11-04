package com.example.netlib.domain.model.webSoket


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
    val status: String,          // pending | confirmed | done | cancelled
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
    val newTime: String? = null, // null при minutes = 0 (задержка отменена)
    val message: String = ""
)

@Serializable
data class QueueEvent(
    val appointmentId: String,
    val number: Int,
    val state: String,           // waiting | next | call
    val ahead: Int = 0,
    val etaMin: Int = 0,
    val room: String? = null,    // null при state = waiting
    val delayMin: Int = 0,
    val message: String = "",
    val demo: Boolean = false
)

@Serializable
data class QueueDemoStartedEvent(
    val appointmentId: String,
    val stepSec: Int = 8
)

// ============================================================
//  ИСХОДЯЩИЕ СООБЩЕНИЯ КЛИЕНТА
//  Сериализуются WsEventParser-ом и отправляются строкой.
// ============================================================

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
    val doctor: String? = null,   // null — отписаться от всех врачей
    val date: String? = null      // null — отписаться от всех дат
)

@Serializable
data class QueueDemoOutgoing(
    val type: String = "queue_demo",
    val appointmentId: String
)


data class ClosedEvent(
    val code: Int,
    val reason: String
)