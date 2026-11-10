package com.example.netlib.data.remote

import com.example.netlib.data.mapper.toDomain
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
import com.example.netlib.domain.model.websocket.WebSocketEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun parse(message: String, json: Json): WebSocketEvent? {
    return runCatching {
        val element = json.parseToJsonElement(message)
        val type = (element as? JsonObject)
            ?.get("type")
            ?.jsonPrimitive
            ?.content
            ?: return null

        when (type) {
            "hello" -> json.decodeFromString<HelloEventDTO>(message).toDomain()
            "pong" -> json.decodeFromString<PongEventDTO>(message).toDomain()
            "error" -> json.decodeFromString<ErrorEventDTO>(message).toDomain()
            "subscribed" -> json.decodeFromString<SubscribedEventDTO>(message).toDomain()
            "unsubscribed" -> json.decodeFromString<UnsubscribedEventDTO>(message).toDomain()
            "slot_taken" -> json.decodeFromString<SlotTakenEventDTO>(message).toDomain()
            "slot_freed" -> json.decodeFromString<SlotFreedEventDTO>(message).toDomain()
            "status" -> json.decodeFromString<StatusEventDTO>(message).toDomain()
            "delay" -> json.decodeFromString<DelayEventDTO>(message).toDomain()
            "queue" -> json.decodeFromString<QueueEventDTO>(message).toDomain()
            "queue_demo_started" -> json.decodeFromString<QueueDemoStartedEventDTO>(message).toDomain()
            else -> null
        }
    }.getOrNull()
}
