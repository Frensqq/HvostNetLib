package com.example.netlib

import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.nextReconnectDelay
import com.example.netlib.data.remote.parse
import com.example.netlib.domain.model.websocket.SlotTakenEvent
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


//тесты по заданию
class WebSocketTest {
    @Test
    fun parsesSlot() {
        val message = """
            {
                "type": "slot_taken",
                "doctor": "doctor-123",
                "date": "2026-11-04",
                "time": "10:30",
                "start": "2026-11-04 07:30:00.000Z"
            }
        """.trimIndent()

        val event = assertIs<SlotTakenEvent>(parse(message))

        assertEquals("doctor-123", event.doctor)
        assertEquals("2026-11-04", event.date)
        assertEquals("10:30", event.time)
    }

    @Test
    fun reconnect30Seconds() {
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
    fun restoresSubscription() = runBlocking {
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
            client.events.first()
        }

        client.connect("test-token")

        helloEvent.await()

        client.subscribeSlots("doctor-123", "2026-11-04")

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

        val subscribeCount = connection.sentMessages.count {
            val json = Json.parseToJsonElement(it).jsonObject
            json["type"]?.jsonPrimitive?.content == "subscribe"
        }

        assertEquals(2, subscribeCount)

        client.disconnect()
    }

    @Test
    fun NotReconnectCloseCode4001() = runBlocking {
        val connection = FakeWsConnection()
        connection.disconnectFirst = false
        connection.closeCode = 4001

        val client = KtorRealtimeClient(connection)

        client.connect("test-token")

        delay(100)

        assertEquals(1, connection.connectCount)

        client.disconnect()
    }
}