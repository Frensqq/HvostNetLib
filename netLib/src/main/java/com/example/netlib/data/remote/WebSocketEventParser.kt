package com.example.netlib.data.remote

import com.example.netlib.domain.model.websocket.DelayEvent
import com.example.netlib.domain.model.websocket.ErrorEvent
import com.example.netlib.domain.model.websocket.HelloEvent
import com.example.netlib.domain.model.websocket.PongEvent
import com.example.netlib.domain.model.websocket.QueueDemoStartedEvent
import com.example.netlib.domain.model.websocket.QueueEvent
import com.example.netlib.domain.model.websocket.SlotFreedEvent
import com.example.netlib.domain.model.websocket.SlotTakenEvent
import com.example.netlib.domain.model.websocket.StatusEvent
import com.example.netlib.domain.model.websocket.SubscribedEvent
import com.example.netlib.domain.model.websocket.UnsubscribedEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive


fun parse(
    message: String,
    json: Json
): Any? {
    return runCatching {
        val json = json
        val element = json.parseToJsonElement(message)
        val type = (element as? JsonObject)
            ?.get("type")
            ?.jsonPrimitive
            ?.content
            ?: return null

        when (type) {
            "hello" -> json.decodeFromString<HelloEvent>(message)
            "pong" -> json.decodeFromString<PongEvent>(message)
            "error" -> json.decodeFromString<ErrorEvent>(message)
            "subscribed" -> json.decodeFromString<SubscribedEvent>(message)
            "unsubscribed" -> json.decodeFromString<UnsubscribedEvent>(message)
            "slot_taken" -> json.decodeFromString<SlotTakenEvent>(message)
            "slot_freed" -> json.decodeFromString<SlotFreedEvent>(message)
            "status" -> json.decodeFromString<StatusEvent>(message)
            "delay" -> json.decodeFromString<DelayEvent>(message)
            "queue" -> json.decodeFromString<QueueEvent>(message)
            "queue_demo_started" -> json.decodeFromString<QueueDemoStartedEvent>(message)
            else -> null
        }
    }.getOrNull()
}
