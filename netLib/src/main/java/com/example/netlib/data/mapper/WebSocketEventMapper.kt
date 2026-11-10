package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.websocketDTO.DelayEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.ErrorEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.HelloEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.PongEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.QueueDemoStartedEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.QueueEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.SlotFreedEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.SlotTakenEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.StatusEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.SubscribedEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.UnsubscribedEventDTO
import com.example.netlib.data.remote.DTO.websocketDTO.WebSocketUserDTO
import com.example.netlib.domain.model.websocket.*

fun WebSocketUserDTO.toDomain() = WebSocketUser(id, email, name)
fun HelloEventDTO.toDomain() = HelloEvent(user.toDomain(), serverTime)
fun PongEventDTO.toDomain() = PongEvent(time)
fun ErrorEventDTO.toDomain() = ErrorEvent(code, message)
fun SubscribedEventDTO.toDomain() = SubscribedEvent(topic, doctor, date)
fun UnsubscribedEventDTO.toDomain() = UnsubscribedEvent(topic, doctor, date)
fun SlotTakenEventDTO.toDomain() = SlotTakenEvent(doctor, date, time, start)
fun SlotFreedEventDTO.toDomain() = SlotFreedEvent(doctor, date, time, start)

fun StatusEventDTO.toDomain() = StatusEvent(
    appointmentId, number, status, statusLabel, previous, cancelReason, start, changedAt
)

fun DelayEventDTO.toDomain() = DelayEvent(appointmentId, number, minutes, newTime, message)

fun QueueEventDTO.toDomain() = QueueEvent(
    appointmentId, number, state, ahead, etaMin, room, delayMin, message, demo
)

fun QueueDemoStartedEventDTO.toDomain() = QueueDemoStartedEvent(appointmentId, stepSec)
