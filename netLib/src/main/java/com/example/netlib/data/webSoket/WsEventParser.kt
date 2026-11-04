package com.example.netlib.data.webSoket

import com.example.netlib.domain.model.webSoket.DelayEvent
import com.example.netlib.domain.model.webSoket.ErrorEvent
import com.example.netlib.domain.model.webSoket.HelloEvent
import com.example.netlib.domain.model.webSoket.PingOutgoing
import com.example.netlib.domain.model.webSoket.PongEvent
import com.example.netlib.domain.model.webSoket.QueueDemoOutgoing
import com.example.netlib.domain.model.webSoket.QueueDemoStartedEvent
import com.example.netlib.domain.model.webSoket.QueueEvent
import com.example.netlib.domain.model.webSoket.SlotFreedEvent
import com.example.netlib.domain.model.webSoket.SlotTakenEvent
import com.example.netlib.domain.model.webSoket.StatusEvent
import com.example.netlib.domain.model.webSoket.SubscribeOutgoing
import com.example.netlib.domain.model.webSoket.SubscribedEvent
import com.example.netlib.domain.model.webSoket.UnsubscribeOutgoing
import com.example.netlib.domain.model.webSoket.UnsubscribedEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

object WsEventParser {

    private val json = Json {
        ignoreUnknownKeys = true   // сервер может добавить поля — не падаем
        isLenient = true
        explicitNulls = false // отсутствующее поле = null в nullable-полях
        encodeDefaults = true
    }

    /**
     * Возвращает один из data-классов пакета websocket
     * (HelloEvent, SlotTakenEvent, StatusEvent, ...) или null.
     *
     * null означает: не JSON-объект, нет поля "type",
     * неизвестный тип или не удалось разобрать по схеме.
     * Такие сообщения клиент просто игнорирует.
     */
    fun parseIncoming(raw: String): Any? {
        val obj = runCatching { json.parseToJsonElement(raw) as? JsonObject }
            .getOrNull() ?: return null

        val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return null

        return runCatching {
            when (type) {
                "hello"              -> json.decodeFromJsonElement(HelloEvent.serializer(), obj)
                "pong"               -> json.decodeFromJsonElement(PongEvent.serializer(), obj)
                "error"              -> json.decodeFromJsonElement(ErrorEvent.serializer(), obj)
                "subscribed"         -> json.decodeFromJsonElement(SubscribedEvent.serializer(), obj)
                "unsubscribed"       -> json.decodeFromJsonElement(UnsubscribedEvent.serializer(), obj)
                "slot_taken"         -> json.decodeFromJsonElement(SlotTakenEvent.serializer(), obj)
                "slot_freed"         -> json.decodeFromJsonElement(SlotFreedEvent.serializer(), obj)
                "status"             -> json.decodeFromJsonElement(StatusEvent.serializer(), obj)
                "delay"              -> json.decodeFromJsonElement(DelayEvent.serializer(), obj)
                "queue"              -> json.decodeFromJsonElement(QueueEvent.serializer(), obj)
                "queue_demo_started" -> json.decodeFromJsonElement(QueueDemoStartedEvent.serializer(), obj)
                else                 -> null
            }
        }.getOrNull()
    }

    // ========================================================
    //  ИСХОДЯЩИЕ
    // ========================================================

    fun encodePing(): String =
        json.encodeToString(PingOutgoing.serializer(), PingOutgoing())

    fun encodeSubscribe(doctor: String, date: String): String =
        json.encodeToString(
            SubscribeOutgoing.serializer(),
            SubscribeOutgoing(doctor = doctor, date = date)
        )

    fun encodeUnsubscribe(doctor: String? = null, date: String? = null): String =
        json.encodeToString(
            UnsubscribeOutgoing.serializer(),
            UnsubscribeOutgoing(doctor = doctor, date = date)
        )

    fun encodeQueueDemo(appointmentId: String): String =
        json.encodeToString(
            QueueDemoOutgoing.serializer(),
            QueueDemoOutgoing(appointmentId = appointmentId)
        )
}