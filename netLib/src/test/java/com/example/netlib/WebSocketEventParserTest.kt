package com.example.netlib

import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.parse
import com.example.netlib.domain.model.websocket.DelayEvent
import com.example.netlib.domain.model.websocket.QueueDemoStartedEvent
import com.example.netlib.domain.model.websocket.QueueEvent
import com.example.netlib.domain.model.websocket.SlotFreedEvent
import com.example.netlib.domain.model.websocket.SlotTakenEvent
import com.example.netlib.domain.model.websocket.StatusEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class WebSocketEventParserTest {

    @Test
    fun parseSlotTakenEvent() {
        val message = """
            {
                "type": "slot_taken",
                "doctor": "doctor-123",
                "date": "2026-11-04",
                "time": "10:30",
                "start": "2026-11-04 07:30:00.000Z"
            }
        """.trimIndent()

        val event = parse(message)

        val result = assertIs<SlotTakenEvent>(event)

        assertEquals("doctor-123", result.doctor)
        assertEquals("2026-11-04", result.date)
        assertEquals("10:30", result.time)
    }


    @Test
    fun doesNotReconnectAfterUnauthorized() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """{"type":"error","code":"unauthorized"}"""
        val client = KtorRealtimeClient(connection)

        client.connect("test-token")



        delay(100)

        assertEquals(1, connection.connectCount)

        client.disconnect()
    }

    @Test
    fun reconnectsAfterConnectionLost() = runBlocking {
        val connection = FakeWsConnection()
        val client = KtorRealtimeClient(connection)

        client.connect("test-token")

        delay(3500)

        assertEquals(3, connection.connectCount)

        client.disconnect()
    }

    @Test
    fun restoresSubscriptionAfterReconnect() = runBlocking {
        val connection = FakeWsConnection()
        val client = KtorRealtimeClient(connection)

        client.subscribeSlots("doctor-123", "2026-11-04")
        client.connect("test-token")

        delay(1500)

        assertEquals(2, connection.connectCount)

        val subscribeMessages = connection.sentMessages.filter {
            val json = Json.parseToJsonElement(it).jsonObject
            json["type"]?.jsonPrimitive?.content == "subscribe"
        }

        assertEquals(1, subscribeMessages.size)

        client.disconnect()
    }

    @Test
    fun parseSlotFreedEvent() {
        val message = """
        {
            "type": "slot_freed",
            "doctor": "doctor-123",
            "date": "2026-11-04",
            "time": "10:30",
            "start": "2026-11-04 07:30:00.000Z"
        }
    """.trimIndent()

        val event = parse(message)

        val result = assertIs<SlotFreedEvent>(event)

        assertEquals("doctor-123", result.doctor)
        assertEquals("2026-11-04", result.date)
        assertEquals("10:30", result.time)
    }

    @Test
    fun emitsSlotFreedEvent() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """
        {
            "type": "slot_freed",
            "doctor": "doctor-123",
            "date": "2026-11-04",
            "time": "10:30",
            "start": "2026-11-04 07:30:00.000Z"
        }
    """.trimIndent()

        val client = KtorRealtimeClient(connection)

        val event = async {
            client.events.first()
        }

        client.connect("test-token")

        val result = assertIs<SlotFreedEvent>(event.await())

        assertEquals("doctor-123", result.doctor)

        client.disconnect()
    }

    @Test
    fun sendsUnsubscribeMessage() = runBlocking {
        val connection = FakeWsConnection()
        val client = KtorRealtimeClient(connection)

        client.unsubscribeSlots("doctor-123", "2026-11-04")

        val message = connection.sentMessages.last()
        val json = Json.parseToJsonElement(message).jsonObject

        assertEquals("unsubscribe", json["type"]?.jsonPrimitive?.content)
        assertEquals("doctor-123", json["doctor"]?.jsonPrimitive?.content)
        assertEquals("2026-11-04", json["date"]?.jsonPrimitive?.content)
    }

    @Test
    fun parseStatusEvent() {
        val message = """
        {
            "type": "status",
            "appointmentId": "appointment-123",
            "number": 5,
            "status": "confirmed",
            "statusLabel": "Подтверждена",
            "previous": "pending",
            "start": "2026-11-04 07:30:00.000Z"
        }
    """.trimIndent()

        val event = parse(message)

        val result = assertIs<StatusEvent>(event)

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(5, result.number)
        assertEquals("confirmed", result.status)
    }

    @Test
    fun emitsStatusEvent() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """
        {
            "type": "status",
            "appointmentId": "appointment-123",
            "number": 5,
            "status": "confirmed"
        }
    """.trimIndent()

        val client = KtorRealtimeClient(connection)

        val event = async {
            client.events.first()
        }

        client.connect("test-token")

        val result = assertIs<StatusEvent>(event.await())

        assertEquals("appointment-123", result.appointmentId)

        client.disconnect()
    }

    @Test
    fun parseDelayEvent() {
        val message = """
        {
            "type": "delay",
            "appointmentId": "appointment-123",
            "number": 5,
            "minutes": 15,
            "newTime": "10:45",
            "message": "Приём задерживается"
        }
    """.trimIndent()

        val event = parse(message)

        val result = assertIs<DelayEvent>(event)

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(15, result.minutes)
        assertEquals("10:45", result.newTime)
    }

    @Test
    fun parseQueueEvent() {
        val message = """
        {
            "type": "queue",
            "appointmentId": "appointment-123",
            "number": 5,
            "state": "waiting",
            "ahead": 3,
            "etaMin": 20,
            "delayMin": 10,
            "message": "Ожидайте своей очереди"
        }
    """.trimIndent()

        val event = parse(message)

        val result = assertIs<QueueEvent>(event)

        assertEquals("appointment-123", result.appointmentId)
        assertEquals("waiting", result.state)
        assertEquals(3, result.ahead)
        assertEquals(20, result.etaMin)
    }

    @Test
    fun sendsQueueDemoMessage() = runBlocking {
        val connection = FakeWsConnection()
        val client = KtorRealtimeClient(connection)

        client.requestQueueDemo("appointment-123")

        val message = connection.sentMessages.last()
        val json = Json.parseToJsonElement(message).jsonObject

        assertEquals("queue_demo", json["type"]?.jsonPrimitive?.content)
        assertEquals(
            "appointment-123",
            json["appointmentId"]?.jsonPrimitive?.content
        )
    }

    @Test
    fun emitsDelayEvent() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """
        {
            "type": "delay",
            "appointmentId": "appointment-123",
            "number": 5,
            "minutes": 15
        }
    """.trimIndent()

        val client = KtorRealtimeClient(connection)

        val event = async {
            client.events.first()
        }

        client.connect("test-token")

        val result = assertIs<DelayEvent>(event.await())

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(15, result.minutes)

        client.disconnect()
    }

    @Test
    fun emitsQueueEvent() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """
        {
            "type": "queue",
            "appointmentId": "appointment-123",
            "number": 5,
            "state": "waiting",
            "ahead": 3,
            "etaMin": 20
        }
    """.trimIndent()

        val client = KtorRealtimeClient(connection)

        val event = async {
            client.events.first()
        }

        client.connect("test-token")

        val result = assertIs<QueueEvent>(event.await())

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(3, result.ahead)

        client.disconnect()
    }

    @Test
    fun parseQueueDemoStartedEvent() {
        val message = """
        {
            "type": "queue_demo_started",
            "appointmentId": "appointment-123",
            "stepSec": 8
        }
    """.trimIndent()

        val event = parse(message)

        val result = assertIs<QueueDemoStartedEvent>(event)

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(8, result.stepSec)
    }

    @Test
    fun emitsQueueDemoStartedEvent() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.message = """
        {
            "type": "queue_demo_started",
            "appointmentId": "appointment-123",
            "stepSec": 8
        }
    """.trimIndent()

        val client = KtorRealtimeClient(connection)

        val event = async {
            client.events.first()
        }

        client.connect("test-token")

        val result = assertIs<QueueDemoStartedEvent>(event.await())

        assertEquals("appointment-123", result.appointmentId)
        assertEquals(8, result.stepSec)

        client.disconnect()
    }
}
