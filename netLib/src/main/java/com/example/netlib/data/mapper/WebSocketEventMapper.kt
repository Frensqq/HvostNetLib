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

internal fun WebSocketUserDTO.toDomain() = WebSocketUser(
    id = id,
    email = email,
    name = name)

internal fun HelloEventDTO.toDomain() = HelloEvent(
    user = user.toDomain(),
    serverTime =  serverTime
)

internal fun PongEventDTO.toDomain() = PongEvent(
    time = time
)

internal fun ErrorEventDTO.toDomain() = ErrorEvent(
    code= code,
    message = message
)

internal fun SubscribedEventDTO.toDomain() = SubscribedEvent(
    topic = topic,
    doctor = doctor,
    date = date
)

internal fun UnsubscribedEventDTO.toDomain() = UnsubscribedEvent(
    topic = topic,
    doctor = doctor,
    date = date
)

internal fun SlotTakenEventDTO.toDomain() = SlotTakenEvent(
    doctor = doctor,
    date = date,
    time = time,
    start = start
)

internal fun SlotFreedEventDTO.toDomain() = SlotFreedEvent(
    doctor = doctor,
    date = date,
    time = time,
    start = start
)

internal fun StatusEventDTO.toDomain() = StatusEvent(
    appointmentId = appointmentId,
    number = number,
    status = status,
    statusLabel = statusLabel,
    previous = previous ,
    cancelReason = cancelReason,
    start = start,
    changedAt = changedAt
)

internal fun DelayEventDTO.toDomain() = DelayEvent(
    appointmentId =  appointmentId,
    number = number,
    minutes = minutes,
    newTime = newTime,
    message =  message)

internal fun QueueEventDTO.toDomain() = QueueEvent(
    appointmentId = appointmentId,
    number = number,
    state = state,
    ahead = ahead,
    etaMin = etaMin,
    room = room,
    delayMin = delayMin,
    message = message,
    demo = demo
)

internal fun QueueDemoStartedEventDTO.toDomain() = QueueDemoStartedEvent(appointmentId, stepSec)
