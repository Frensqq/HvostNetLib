package com.example.netlib.websoketForkTest

import com.example.netlib.data.remote.PBApiService
import com.example.netlib.data.webSoket.KtorRealtimeClient
import com.example.netlib.data.webSoket.KtorWsTransport
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.webSoket.ClosedEvent
import com.example.netlib.domain.model.webSoket.HelloEvent
import com.example.netlib.domain.model.webSoket.PongEvent
import com.example.netlib.domain.model.webSoket.QueueDemoStartedEvent
import com.example.netlib.domain.model.webSoket.QueueEvent
import com.example.netlib.domain.model.webSoket.SubscribedEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ручные проверки WebSocket вживую на frens-project.tech.
 * НЕ для CI — нужны реальная сеть и работающий сервер.
 * Запускать по одному методу.
 */
class WsLiveTest {

    private val SECRET = "hvost-ws-secret"

    private val api get() = PBApiService.instance
    private val rt get() = PBApiService.realtime

    // ============================================================
    //  1. happy path
    // ============================================================
    @Test
    fun `1 happy path hello pong subscribe`() = runBlocking {
        val token = authToken()
        val events = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { rt.events.collect { events += it } }

        rt.connect(token)

        waitFor(events, 5_000) { it is HelloEvent }
        assertTrue("нет hello, got=${names(events)}", events.any { it is HelloEvent })

        rt.ping()
        waitFor(events, 3_000) { it is PongEvent }
        assertTrue("нет pong, got=${names(events)}", events.any { it is PongEvent })

        val doctorId = firstDoctorId()
        val date = "2026-11-10"
        rt.subscribeSlots(doctorId, date)

        waitFor(events, 3_000) {
            it is SubscribedEvent && it.doctor == doctorId && it.date == date
        }
        assertTrue(
            "нет subscribed для $doctorId/$date, got=${names(events)}",
            events.any { it is SubscribedEvent && it.doctor == doctorId && it.date == date }
        )
        assertFalse(
            "неожиданное закрытие: ${events.filterIsInstance<ClosedEvent>()}",
            events.any { it is ClosedEvent }
        )

        println("✓ 1 OK: events=${names(events)}")
        rt.disconnect()
        job.cancel()
    }

    // ============================================================
    //  2. invalid token → ClosedEvent(4001)
    //  ⚠ ИСПРАВЛЕНО: KtorRealtimeClient теперь через KtorWsTransport
    // ============================================================
    @Test
    fun `2 invalid token closes 4001 without reconnect`() = runBlocking {
        val client = KtorRealtimeClient(
            transport = KtorWsTransport(PBApiService.httpClient),
            baseUrl = PBApiService.WS_URL,
        )
        val closed = mutableListOf<ClosedEvent>()
        val job = launch(Dispatchers.IO) {
            client.events.collect { if (it is ClosedEvent) closed += it }
        }

        client.connect("invalid-token-xxx")
        delay(3_000)

        assertTrue(
            "ожидался ClosedEvent(4001), got=$closed",
            closed.any { it.code == 4001 }
        )

        val n = closed.size
        delay(3_000)
        assertTrue(
            "после 4001 реконнект пошёл: $n → ${closed.size}",
            closed.size == n
        )

        println("✓ 2 OK: closed=$closed")
        client.disconnect()
        job.cancel()
    }

    // ============================================================
    //  3. бэкофф на мёртвом endpoint
    //  ⚠ ИСПРАВЛЕНО: KtorRealtimeClient теперь через KtorWsTransport
    // ============================================================
    @Test
    fun `3 backoff grows on dead endpoint`() = runBlocking {
        val client = KtorRealtimeClient(
            transport = KtorWsTransport(PBApiService.httpClient),
            baseUrl = "ws://frens-project.tech:9999/ws",
        )
        val closed = mutableListOf<ClosedEvent>()
        val job = launch(Dispatchers.IO) {
            client.events.collect { if (it is ClosedEvent) closed += it }
        }

        val t0 = System.currentTimeMillis()
        client.connect("any")
        delay(12_000)

        println("attempts=${closed.size} за ${System.currentTimeMillis() - t0}ms")
        closed.forEachIndexed { i, e -> println("  [$i] code=${e.code} reason=${e.reason}") }

        assertTrue("ожидалось ≥3 попыток, got=${closed.size}", closed.size >= 3)
        assertTrue("слишком много (бэкофф не работает): ${closed.size}", closed.size <= 5)

        println("✓ 3 OK")
        client.disconnect()
        job.cancel()
    }

    // ============================================================
    //  4. восстановление подписки после disconnect-all (1012)
    // ============================================================
    @Test
    fun `4 restore subscription after disconnect-all`() = runBlocking {
        val token = authToken()
        val events = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { rt.events.collect { events += it } }

        rt.connect(token)
        waitFor(events, 5_000) { it is HelloEvent }
        assertTrue("нет hello", events.any { it is HelloEvent })

        val doctorId = firstDoctorId()
        val date = "2026-11-10"
        rt.subscribeSlots(doctorId, date)
        waitFor(events, 3_000) {
            it is SubscribedEvent && it.doctor == doctorId && it.date == date
        }
        assertTrue(
            "нет subscribed, got=${names(events)}",
            events.any { it is SubscribedEvent && it.doctor == doctorId }
        )

        val hellosBefore = events.count { it is HelloEvent }
        val subsBefore = events.count { it is SubscribedEvent }

        val resp = disconnectAll()
        println("disconnect-all: $resp")

        delay(6_000)

        val hellosAfter = events.count { it is HelloEvent }
        val subsAfter = events.count { it is SubscribedEvent }

        println("hello: $hellosBefore → $hellosAfter")
        println("subscribed: $subsBefore → $subsAfter")
        println("events: ${names(events)}")

        assertTrue(
            "после reconnect должен прийти второй hello: $hellosBefore → $hellosAfter",
            hellosAfter > hellosBefore
        )
        assertTrue(
            "после reconnect должен восстановиться subscribe: $subsBefore → $subsAfter",
            subsAfter > subsBefore
        )

        println("✓ 4 OK")
        rt.disconnect()
        job.cancel()
    }

    // ============================================================
    //  5. queue_demo
    // ============================================================
    @Test
    fun `5 queue_demo yields queue events`() = runBlocking {
        val token = authToken()
        val events = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { rt.events.collect { events += it } }

        rt.connect(token)
        waitFor(events, 5_000) { it is HelloEvent }

        val apptId = firstUpcomingAppointmentId(token)
        if (apptId == null) {
            println("⚠ 5 skipped: у demo нет предстоящих записей")
            rt.disconnect(); job.cancel(); return@runBlocking
        }

        rt.requestQueueDemo(apptId)

        waitFor(events, 3_000) { it is QueueDemoStartedEvent }
        assertTrue(
            "нет queue_demo_started, got=${names(events)}",
            events.any { it is QueueDemoStartedEvent }
        )

        waitFor(events, 15_000) { it is QueueEvent }
        assertTrue(
            "не пришли queue-события за 15 с, got=${names(events)}",
            events.any { it is QueueEvent }
        )

        println("✓ 5 OK: events=${names(events)}")
        rt.disconnect()
        job.cancel()
    }

    // ============================================================
    //  helpers
    // ============================================================

    private suspend fun authToken(): String {
        val resp = api.authorization(AuthRequest("demo@hvost.local", "Demo2027!"))
        return resp.token
    }

    private suspend fun firstDoctorId(): String {
        val list = api.getDoctors()
        return list.items.first().id
    }

    private suspend fun firstUpcomingAppointmentId(token: String): String? {
        api.saveToken(token)
        val filter = "(status='pending'||status='confirmed')"
        val list = api.getAppointment(filter = filter)
        return list.items.firstOrNull()?.id
    }

    private suspend fun disconnectAll(): String = try {
        val http = HttpClient()
        try {
            val r = http.get(
                "http://frens-project.tech:8091/internal/disconnect-all?secret=$SECRET"
            )
            "status=${r.status}"
        } finally { http.close() }
    } catch (e: Exception) {
        "failed: ${e.message}"
    }

    private suspend fun waitFor(
        list: MutableList<Any>,
        timeoutMs: Long,
        predicate: (Any) -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (list.any(predicate)) return
            delay(50)
        }
    }

    private fun names(list: List<Any>): List<String> =
        list.map { it::class.simpleName ?: it.toString() }
}