
package com.example.netlib

import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.nextReconnectDelay
import com.example.netlib.data.remote.parse
import com.example.netlib.domain.model.websocket.HelloEvent
import com.example.netlib.domain.model.websocket.SlotTakenEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WebSocketMockTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun slotTakenEventIsParsed() {
        val message = """
            {
                "type": "slot_taken",
                "doctor": "doctor-1",
                "date": "2026-11-11",
                "time": "10:00",
                "start": "2026-11-11T10:00:00Z"
            }
        """.trimIndent()

        val event = parse(message, json)

        assertIs<SlotTakenEvent>(event)
        assertEquals("doctor-1", event.doctor)
        assertEquals("2026-11-11", event.date)
        assertEquals("10:00", event.time)
        assertEquals("2026-11-11T10:00:00Z", event.start)
    }

    @Test
    fun reconnectDelayIncreasesAndIsLimited() {
        var delay = 1000L

        assertEquals(2000L, nextReconnectDelay(delay))
        delay = nextReconnectDelay(delay)

        assertEquals(4000L, nextReconnectDelay(delay))
        delay = nextReconnectDelay(delay)

        assertEquals(8000L, nextReconnectDelay(delay))
        delay = nextReconnectDelay(delay)

        assertEquals(16000L, nextReconnectDelay(delay))
        delay = nextReconnectDelay(delay)

        assertEquals(30000L, nextReconnectDelay(delay))
        assertEquals(30000L, nextReconnectDelay(30000L))
    }

    @Test
    fun subscriptionIsRestoredAfterReconnect() = runBlocking {
        val connection = FakeWsConnection()
        connection.closeCode = null

        val helloEvent = parse(connection.message, json)
        assertIs<HelloEvent>(helloEvent)

        val client = KtorRealtimeClient(
            connection,
            json
        ) { "test-token" }

        client.subscribeSlots("doctor-1", "2026-11-11")
        connection.sentMessages.clear()

        client.connect()

        withTimeout(5000) {
            while (connection.listenCount < 2) {
                delay(10)
            }
        }

        val subscriptions = connection.sentMessages.count {
            it.contains("\"doctor\":\"doctor-1\"")
        }

        assertEquals(2, connection.connectCount)
        assertEquals(2, connection.listenCount)
        assertEquals(2, subscriptions)

        client.disconnect()
    }

    @Test
    fun closeCode4001DoesNotReconnect() = runBlocking {
        val connection = FakeWsConnection()

        connection.disconnectFirst = false
        connection.closeCode = 4001

        val client = KtorRealtimeClient(
            connection,
            json
        ) { "test-token" }

        client.connect()

        withTimeout(3000) {
            while (connection.listenCount < 1) {
                delay(10)
            }
        }

        delay(100)

        assertEquals(1, connection.connectCount)
        assertEquals(1, connection.listenCount)

        client.disconnect()
    }
}
