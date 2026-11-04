package com.example.netlib.websoketForkTest




import com.example.netlib.data.webSoket.WsEventParser
import com.example.netlib.domain.model.webSoket.DelayEvent
import com.example.netlib.domain.model.webSoket.QueueEvent
import com.example.netlib.domain.model.webSoket.SlotTakenEvent
import com.example.netlib.domain.model.webSoket.StatusEvent
import org.junit.Assert.*
import org.junit.Test

class WsEventParserTest {

    @Test
    fun `slot_taken разбирается в SlotTakenEvent`() {
        val raw = """
            {"type":"slot_taken","doctor":"doc1","date":"2026-11-02",
             "time":"10:30","start":"2026-11-02 07:30:00.000Z"}
        """.trimIndent()

        val ev = WsEventParser.parseIncoming(raw)

        assertTrue(ev is SlotTakenEvent)
        ev as SlotTakenEvent
        assertEquals("doc1", ev.doctor)
        assertEquals("2026-11-02", ev.date)
        assertEquals("10:30", ev.time)
        assertEquals("2026-11-02 07:30:00.000Z", ev.start)
    }

    @Test
    fun `queue без room разбирается с room = null`() {
        val raw = """{"type":"queue","appointmentId":"a1","number":1012,
                     "state":"waiting","ahead":2,"etaMin":25,
                     "message":"Перед вами 2 пациента"}"""

        val ev = WsEventParser.parseIncoming(raw)

        assertTrue(ev is QueueEvent)
        ev as QueueEvent
        assertEquals("waiting", ev.state)
        assertEquals(2, ev.ahead)
        assertNull(ev.room)
        assertFalse(ev.demo)
    }

    @Test
    fun `delay с minutes=0 разбирается с newTime = null`() {
        val raw = """{"type":"delay","appointmentId":"a1","number":1012,
                     "minutes":0,"message":"Задержка отменена"}"""

        val ev = WsEventParser.parseIncoming(raw)

        assertTrue(ev is DelayEvent)
        ev as DelayEvent
        assertEquals(0, ev.minutes)
        assertNull(ev.newTime)
    }

    @Test
    fun `status разбирается`() {
        val raw = """{"type":"status","appointmentId":"a1","number":1012,
                     "status":"confirmed","statusLabel":"Подтверждена",
                     "previous":"pending","changedAt":"2026-11-01T09:01:00.000Z"}"""

        val ev = WsEventParser.parseIncoming(raw)

        assertTrue(ev is StatusEvent)
        ev as StatusEvent
        assertEquals("confirmed", ev.status)
        assertEquals("Подтверждена", ev.statusLabel)
        assertEquals("pending", ev.previous)
    }

    @Test
    fun `битый JSON возвращает null`() {
        assertNull(WsEventParser.parseIncoming("{not json"))
    }

    @Test
    fun `неизвестный type возвращает null`() {
        assertNull(WsEventParser.parseIncoming("""{"type":"unknown"}"""))
    }

    @Test
    fun `encodeSubscribe формирует корректный JSON`() {
        val s = WsEventParser.encodeSubscribe(doctor = "doc1", date = "2026-11-02")
        assertTrue(s.contains("\"type\":\"subscribe\""))
        assertTrue(s.contains("\"topic\":\"slots\""))
        assertTrue(s.contains("\"doctor\":\"doc1\""))
        assertTrue(s.contains("\"date\":\"2026-11-02\""))
    }

    @Test
    fun `encodePing содержит type`() {
        val s = WsEventParser.encodePing()
        assertEquals("""{"type":"ping"}""", s)
    }

    @Test
    fun `encodeQueueDemo содержит type и appointmentId`() {
        val s = WsEventParser.encodeQueueDemo("a1")
        assertTrue(s.contains("\"type\":\"queue_demo\""))
        assertTrue(s.contains("\"appointmentId\":\"a1\""))
    }
}