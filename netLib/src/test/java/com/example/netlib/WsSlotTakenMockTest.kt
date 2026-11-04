package com.example.netlib



import com.example.netlib.data.webSoket.FakeWsTransport
import com.example.netlib.data.webSoket.KtorRealtimeClient
import com.example.netlib.domain.model.webSoket.ClosedEvent
import com.example.netlib.domain.model.webSoket.SlotTakenEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Г.06 — мок-тесты WebSocket-клиента (без сети, без реального сервера).
 *
 * 1. разбор slot_taken
 * 2. бэкофф переподключения (1, 2, 4…)
 * 3. восстановление подписки после reconnect
 * 4. код 4001 — без переподключения
 *
 * Используют FakeWsTransport (см. рядом) вместо Ktor.
 */
class WsMockTest {

    private companion object {
        const val HELLO =
            """{"type":"hello","user":{"id":"u1","email":"demo@hvost.local"},"serverTime":"2026-11-01T09:00:00Z"}"""
    }

    // ============================================================
    //  1. разбор slot_taken
    // ============================================================
    @Test
    fun `slot_taken parsed to SlotTakenEvent`() = runBlocking {
        val fake = FakeWsTransport()
        val client = KtorRealtimeClient(fake, "ws://fake/ws")

        val events = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { client.events.collect { events += it } }

        client.connect("token")
        delay(200)

        fake.serverSend(HELLO)
        fake.serverSend(
            """{"type":"slot_taken","doctor":"doc1","date":"2026-11-02",
                "time":"10:30","start":"2026-11-02 07:30:00.000Z"}"""
        )
        delay(300)

        val taken = events.filterIsInstance<SlotTakenEvent>().firstOrNull()
        assertTrue(
            "slot_taken не пришёл: events=${events.map { it::class.simpleName }}",
            taken != null
        )
        assertEquals("doc1", taken!!.doctor)
        assertEquals("2026-11-02", taken.date)
        assertEquals("10:30", taken.time)
        assertEquals("2026-11-02 07:30:00.000Z", taken.start)

        client.disconnect()
        job.cancel()
    }

    // ============================================================
    //  2. бэкофф 1, 2, 4…
    // ============================================================
    @Test
    fun `backoff grows on repeated failures`() = runBlocking {
        val fake = FakeWsTransport().apply { failWith = IOException("server down") }
        val client = KtorRealtimeClient(fake, "ws://fake/ws")

        val closed = mutableListOf<ClosedEvent>()
        val job = launch(Dispatchers.IO) {
            client.events.collect { if (it is ClosedEvent) closed += it }
        }

        val t0 = System.currentTimeMillis()
        client.connect("token")
        delay(8_000)   // 1 + 2 + 4 = 7 c на 3 попытки, 8 c с запасом
        val elapsed = System.currentTimeMillis() - t0

        println("openCount=${fake.openCount}, closed=${closed.size} за ${elapsed}ms")

        assertTrue(
            "ожидалось ≥3 попыток, got openCount=${fake.openCount}",
            fake.openCount >= 3
        )
        assertTrue(
            "слишком много, бэкофф не работает: ${fake.openCount}",
            fake.openCount <= 6
        )

        client.disconnect()
        job.cancel()
    }

    // ============================================================
    //  3. восстановление подписки после reconnect
    // ============================================================
    @Test
    fun `resubscribes after reconnect`() = runBlocking {
        val fake = FakeWsTransport()
        val client = KtorRealtimeClient(fake, "ws://fake/ws")

        client.connect("token")
        delay(200)

        // первое соединение: hello, подписка
        fake.serverSend(HELLO)
        delay(100)
        client.subscribeSlots("doc1", "2026-11-02")
        delay(200)

        val firstSent = fake.sentSnapshot()
        assertTrue(
            "первый subscribe не ушёл: $firstSent",
            firstSent.any {
                it.contains("\"doctor\":\"doc1\"") && it.contains("\"2026-11-02\"")
            }
        )
        assertEquals("первый open не случился", 1, fake.openCount)

        // обрыв
        fake.serverDrop()
        delay(1_500)   // 1 c паузы + открытие

        assertTrue(
            "реконнект не произошёл, openCount=${fake.openCount}",
            fake.openCount >= 2
        )

        // новый hello → restore subscribe
        fake.serverSend(HELLO)
        delay(300)

        val secondSent = fake.sentSnapshot()
        val subscribeCount = secondSent.count {
            it.contains("\"type\":\"subscribe\"") &&
                    it.contains("\"doctor\":\"doc1\"") &&
                    it.contains("\"2026-11-02\"")
        }
        println("subscribe в sent: $subscribeCount (всего ${secondSent.size})")
        assertTrue(
            "после reconnect подписка не восстановлена: sent=$secondSent",
            subscribeCount >= 2
        )

        client.disconnect()
    }

    // ============================================================
    //  4. 4001 → без реконнекта
    // ============================================================
    @Test
    fun `close 4001 stops reconnect`() = runBlocking {
        val fake = FakeWsTransport()
        val client = KtorRealtimeClient(fake, "ws://fake/ws")

        val closed = mutableListOf<ClosedEvent>()
        val job = launch(Dispatchers.IO) {
            client.events.collect { if (it is ClosedEvent) closed += it }
        }

        client.connect("invalid-token")
        delay(200)

        fake.serverClose(4001, "unauthorized")
        delay(1_500)   // больше бэкоффа — реконнекта быть не должно

        assertTrue(
            "ClosedEvent(4001) не пришёл: $closed",
            closed.any { it.code == 4001 }
        )
        assertEquals(
            "после 4001 должен быть ровно один open, got=${fake.openCount}",
            1, fake.openCount
        )

        client.disconnect()
        job.cancel()
    }
}