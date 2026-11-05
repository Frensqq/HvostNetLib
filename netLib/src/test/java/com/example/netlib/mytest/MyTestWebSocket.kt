package com.example.netlib.mytest

import com.example.netlib.FakeWsConnection
import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.nextReconnectDelay
import com.example.netlib.data.remote.parse
import com.example.netlib.domain.model.websocket.DelayEvent
import com.example.netlib.domain.model.websocket.HelloEvent
import com.example.netlib.domain.model.websocket.QueueDemoStartedEvent
import com.example.netlib.domain.model.websocket.QueueEvent
import com.example.netlib.domain.model.websocket.SlotFreedEvent
import com.example.netlib.domain.model.websocket.SlotTakenEvent
import com.example.netlib.domain.model.websocket.StatusEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

//Проверка различных частей на работоспособность, тесты по заданию в WebSocketTest
class MyTestWebSocket {

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

        val result = assertIs<SlotTakenEvent>(parse(message))

        assertEquals("doctor-123", result.doctor)
        assertEquals("2026-11-04", result.date)
        assertEquals("10:30", result.time)
    }

    @Test
    fun doesNotReconnectAfterCloseCode4001() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.closeCode = 4001

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
    fun usesRequiredReconnectDelaySequence() {
        var currentDelay = 1_000L
        val actual = mutableListOf<Long>()

        repeat(7) {
            actual.add(currentDelay)
            currentDelay = nextReconnectDelay(currentDelay)
        }

        assertEquals(
            listOf(
                1_000L,
                2_000L,
                4_000L,
                8_000L,
                16_000L,
                30_000L,
                30_000L
            ),
            actual
        )
    }

    @Test
    fun restoresSubscriptionAfterReconnect() = runBlocking {
        val connection = FakeWsConnection()

        connection.message = """
            {
                "type": "hello",
                "user": {
                    "id": "user-123",
                    "email": "user@example.com",
                    "name": "User"
                },
                "serverTime": "2026-11-04T07:30:00.000Z"
            }
        """.trimIndent()

        connection.firstListenGate = CompletableDeferred()

        val client = KtorRealtimeClient(connection)

        val helloEvent = async {
            client.events.first { it is HelloEvent }
        }

        client.connect("test-token")

        assertIs<HelloEvent>(helloEvent.await())

        client.subscribeSlots(
            "doctor-123",
            "2026-11-04"
        )

        connection.firstListenGate?.complete(Unit)

        withTimeout(4_000) {
            while (
                connection.connectCount < 2 ||
                connection.sentMessages.count {
                    val json = Json.parseToJsonElement(it).jsonObject
                    json["type"]?.jsonPrimitive?.content == "subscribe"
                } < 2
            ) {
                delay(10)
            }
        }

        val subscribeMessages = connection.sentMessages.filter {
            val json = Json.parseToJsonElement(it).jsonObject
            json["type"]?.jsonPrimitive?.content == "subscribe"
        }

        assertEquals(2, subscribeMessages.size)

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

        val result = assertIs<SlotFreedEvent>(parse(message))

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

        client.unsubscribeSlots(
            "doctor-123",
            "2026-11-04"
        )

        val json = Json.parseToJsonElement(
            connection.sentMessages.last()
        ).jsonObject

        assertEquals(
            "unsubscribe",
            json["type"]?.jsonPrimitive?.content
        )

        assertEquals(
            "doctor-123",
            json["doctor"]?.jsonPrimitive?.content
        )

        assertEquals(
            "2026-11-04",
            json["date"]?.jsonPrimitive?.content
        )
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

        val result = assertIs<StatusEvent>(parse(message))

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

        val result = assertIs<DelayEvent>(parse(message))

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

        val result = assertIs<QueueEvent>(parse(message))

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

        val json = Json.parseToJsonElement(
            connection.sentMessages.last()
        ).jsonObject

        assertEquals(
            "queue_demo",
            json["type"]?.jsonPrimitive?.content
        )

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

        val result = assertIs<QueueDemoStartedEvent>(parse(message))

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

        client.disconnect()
    }
}