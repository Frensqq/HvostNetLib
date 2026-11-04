package com.example.netlib.websoketForkTest

import com.example.netlib.data.remote.PBApiService
import com.example.netlib.domain.model.RequestAppointment
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.system.NetworkResult
import com.example.netlib.domain.model.webSoket.HelloEvent
import com.example.netlib.domain.model.webSoket.SlotFreedEvent
import com.example.netlib.domain.model.webSoket.SlotTakenEvent
import com.example.netlib.domain.model.webSoket.SubscribedEvent
import com.example.netlib.domain.model.webSoket.UnsubscribedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Г.03 (живые слоты) — проверка на реальном сервере.
 *
 * Сценарий:
 *  1. demo подписывается на врача+дату через WS.
 *  2. test@ записывается на свободный слот REST-ом → demo получает slot_taken.
 *  3. test@ отменяет запись → demo получает slot_freed.
 *  4. demo отписывается → unsubscribed; после этого события не приходят.
 *
 * ⚠ Требует реального сервера и двух пользователей. Не для CI.
 */
class WsSlotLiveTest {

    private val api get() = PBApiService.instance
    private val rt get() = PBApiService.realtime

    @Test
    fun `live slots - taken and freed`() = runBlocking {
        // ---------- 1. Авторизация двух пользователей ----------
        val demoToken = auth("demo@hvost.local", "Demo2027!")
        val testToken = auth("test@hvost.local", "Test2027!")

        // ---------- 2. Подготовка: врач, дата, свободный слот ----------
        api.saveToken(demoToken)
        val doctorId = api.getDoctors().items.first().id
        val date = "2026-11-10"
        val slot = firstFreeSlot(doctorId, date)

// demo подписывается на слоты
        val events = mutableListOf<Any>()
        val job = launch(Dispatchers.IO) { rt.events.collect { events += it } }
        rt.connect(demoToken)

// ← ЖДЁМ hello: с этого момента session != null и можно слать
        waitFor(events, 5_000) { it is HelloEvent }
        assertTrue("нет hello, got=${names(events)}", events.any { it is HelloEvent })

        rt.subscribeSlots(doctorId, date)
        waitFor(events, 3_000) { it is SubscribedEvent }
        assertTrue(
            "нет подтверждения подписки, got=${names(events)}",
            events.any { it is SubscribedEvent && it.doctor == doctorId && it.date == date }
        )

        // ---------- 3. test@ занимает слот → demo получает slot_taken ----------
        api.saveToken(testToken)
        val petId = api.getPet().items.first().id
        val serviceId = firstDoctorServiceId(doctorId) ?: error("у врача нет услуг")

        val booked = api.postAppointment(
            RequestAppointment(
                pet = petId,
                doctor = doctorId,
                service = serviceId,
                start = slot.start,
                comment = "live slots test",
            )
        )
        println("booked appointment id=${booked.id}, number=${booked.number}")

        waitFor(events, 5_000) { it is SlotTakenEvent }
        val taken = events.filterIsInstance<SlotTakenEvent>().firstOrNull()
        assertTrue("не пришёл slot_taken, got=${names(events)}", taken != null)
        taken!!
        println("slot_taken: doctor=${taken.doctor} date=${taken.date} time=${taken.time} start=${taken.start}")
        assert(taken.doctor == doctorId)
        assert(taken.date == date)
        assert(taken.start == slot.start)

        // ---------- 4. test@ отменяет → demo получает slot_freed ----------
        api.cancelAppointment(booked.id, com.example.netlib.domain.model.Cancel(reason = "test"))
        waitFor(events, 5_000) { it is SlotFreedEvent }
        val freed = events.filterIsInstance<SlotFreedEvent>().firstOrNull()
        assertTrue("не пришёл slot_freed, got=${names(events)}", freed != null)
        freed!!
        println("slot_freed: doctor=${freed.doctor} date=${freed.date} time=${freed.time} start=${freed.start}")
        assert(freed.start == slot.start)

        // ---------- 5. Отписка → unsubscribed, и новых событий нет ----------
        rt.unsubscribeSlots(doctorId, date)
        waitFor(events, 3_000) { it is UnsubscribedEvent }
        assertTrue(
            "нет unsubscribed, got=${names(events)}",
            events.any { it is UnsubscribedEvent }
        )

        val takenBefore = events.count { it is SlotTakenEvent }
        // test@ снова занимает слот — demo НЕ должен получить slot_taken
        api.saveToken(testToken)
        val booked2 = api.postAppointment(
            RequestAppointment(
                pet = petId,
                doctor = doctorId,
                service = serviceId,
                start = slot.start,
                comment = "after unsubscribe",
            )
        )
        delay(3_000)
        val takenAfter = events.count { it is SlotTakenEvent }
        assertTrue(
            "после unsubscribe slot_taken быть не должно: было $takenBefore, стало $takenAfter",
            takenBefore == takenAfter
        )

        // убираем за собой
        runCatching { api.cancelAppointment(booked2.id, com.example.netlib.domain.model.Cancel(reason = "cleanup")) }

        println("✓ live slots OK")
        rt.disconnect()
        job.cancel()
    }

    // ============================================================
    //  helpers
    // ============================================================

    private suspend fun auth(identity: String, password: String): String {
        val r = api.authorization(AuthRequest(identity, password))
        return r.token
    }

    /** Первый свободный слот (free=true). */
    private suspend fun firstFreeSlot(doctorId: String, date: String): com.example.netlib.domain.model.Slot {
        val slots = api.getSlots(doctorId, date)
        val free = slots.slots.firstOrNull { it.free }
            ?: error("нет свободных слотов у $doctorId на $date")
        return free
    }

    /** Первый id услуги врача. */
    private suspend fun firstDoctorServiceId(doctorId: String): String? {
        val doc = api.getDoctors().items.firstOrNull { it.id == doctorId } ?: return null
        return doc.services.firstOrNull()
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