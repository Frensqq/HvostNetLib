package com.example.netlib.data.remote

import com.example.netlib.data.remote.DTO.AuthRequest
import com.example.netlib.domain.model.websocket.ErrorEvent
import com.example.netlib.domain.model.websocket.HelloEvent
import com.example.netlib.domain.model.websocket.PongEvent
import com.example.netlib.domain.model.websocket.QueueDemoStartedEvent
import com.example.netlib.domain.model.websocket.SubscribedEvent
import com.example.netlib.domain.model.websocket.UnsubscribedEvent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Реальные интеграционные тесты WebSocket-сервера «Хвост».
 *
 * Используют существующий сервер, PBApi для авторизации
 * и KtorRealtimeClient для WebSocket-соединения.
 *
 * Учётная запись: demo@hvost.local
 */
class KtorRealtimeRealServerTest {

    companion object {
        private const val BASE_URL =
            "http://frens-project.tech:8090/api/"

        private const val WS_URL =
            "ws://frens-project.tech:8091/ws"

        private const val EMAIL = "demo@hvost.local"
        private const val PASSWORD = "Demo2027!"
        private const val TIMEOUT = 10_000L
    }

    private var token: String? = null

    private lateinit var service: PBApiService
    private lateinit var httpClient: HttpClient
    private lateinit var api: PBApi
    private lateinit var realtime: KtorRealtimeClient

    @Before
    fun setUp() = runBlocking {
        service = PBApiService(
            baseUrl = BASE_URL,
            getToken = { token }
        )

        httpClient = service.create()
        api = PBApi(httpClient)

        val auth = api.authorization(
            AuthRequest(
                identity = EMAIL,
                password = PASSWORD
            )
        )

        token = auth.token

        assertTrue("Сервер не вернул токен", token.orEmpty().isNotBlank())

        realtime = KtorRealtimeClient(
            connection = KtorWsConnection(
                httpClient = httpClient,
                wsUrl = WS_URL
            ),
            json = service.json,
            getToken = { token }
        )
    }

    @After
    fun tearDown() = runBlocking {
        if (::realtime.isInitialized) {
            realtime.disconnect()
        }

        if (::httpClient.isInitialized) {
            httpClient.close()
        }
    }

    /**
     * Запускает подключение и ждёт первое событие hello.
     * Подписка на Flow создаётся до connect(), чтобы не пропустить событие.
     */
    private suspend fun connectAndWaitHello(): HelloEvent = coroutineScope {
        val helloDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            realtime.events
                .filterIsInstance<HelloEvent>()
                .first()
        }

        realtime.connect()

        withTimeout(TIMEOUT) {
            helloDeferred.await()
        }
    }

    @Test
    fun connectionReceivesHello() = runBlocking {
        val hello = connectAndWaitHello()

        assertEquals(EMAIL, hello.user.email)
        assertTrue("У пользователя должен быть id", hello.user.id.isNotBlank())
        assertTrue("serverTime не должен быть пустым", hello.serverTime.isNotBlank())
        assertTrue("Клиент должен быть подключён", realtime.isConnected)
    }

    @Test
    fun pingReceivesPong() = runBlocking {
        connectAndWaitHello()

        val pongDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            realtime.events
                .filterIsInstance<PongEvent>()
                .first()
        }

        realtime.ping()

        val pong = withTimeout(TIMEOUT) {
            pongDeferred.await()
        }

        assertTrue("Время pong не должно быть пустым", pong.time.isNotBlank())
    }

    @Test
    fun subscribeAndUnsubscribeSlotsWork() = runBlocking {
        connectAndWaitHello()

        val doctors = api.getDoctors().items
        assumeTrue("На сервере нет врачей", doctors.isNotEmpty())

        val doctor = doctors.first()
        val today = LocalDate.now(ZoneId.of("Europe/Moscow"))

        val date = (1L..14L)
            .map { today.plusDays(it) }
            .firstOrNull { it.dayOfWeek.value in doctor.workDays }

        assumeTrue(
            "Не найден рабочий день врача в ближайшие 14 дней",
            date != null
        )

        val selectedDate = requireNotNull(date)
        val dateString = selectedDate.toString()

        val subscribedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            realtime.events
                .filterIsInstance<SubscribedEvent>()
                .first {
                    it.doctor == doctor.id && it.date == dateString
                }
        }

        realtime.subscribeSlots(
            doctor = doctor.id,
            date = dateString
        )

        val subscribed = withTimeout(TIMEOUT) {
            subscribedDeferred.await()
        }

        assertEquals("slots", subscribed.topic)
        assertEquals(doctor.id, subscribed.doctor)
        assertEquals(dateString, subscribed.date)

        val unsubscribedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            realtime.events
                .filterIsInstance<UnsubscribedEvent>()
                .first {
                    it.doctor == doctor.id && it.date == dateString
                }
        }

        realtime.unsubscribeSlots(
            doctor = doctor.id,
            date = dateString
        )

        val unsubscribed = withTimeout(TIMEOUT) {
            unsubscribedDeferred.await()
        }

        assertEquals("slots", unsubscribed.topic)
        assertEquals(doctor.id, unsubscribed.doctor)
        assertEquals(dateString, unsubscribed.date)
    }

    @Test
    fun queueDemoStartsForUpcomingAppointment() = runBlocking {
        connectAndWaitHello()

        val appointments = api.getAppointment(
            sort = "start",
            page = 1,
            perPage = 100
        ).items

        val now = Instant.now()

        val appointment = appointments.firstOrNull { item ->
            item.status != "done" &&
                    item.status != "cancelled" &&
                    runCatching {
                        Instant.parse(item.start.replace(" ", "T")).isAfter(now)
                    }.getOrDefault(false)
        }

        assumeTrue(
            "У демо-пользователя нет предстоящей записи для queue_demo",
            appointment != null
        )

        val selectedAppointment = requireNotNull(appointment)

        val startedDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            realtime.events
                .filterIsInstance<QueueDemoStartedEvent>()
                .first { it.appointmentId == selectedAppointment.id }
        }

        realtime.requestQueueDemo(selectedAppointment.id)

        val started = withTimeout(TIMEOUT) {
            startedDeferred.await()
        }

        assertEquals(selectedAppointment.id, started.appointmentId)
        assertTrue("stepSec должен быть положительным", started.stepSec > 0)
    }

    @Test
    fun invalidTokenReceivesUnauthorized() = runBlocking {
        // Отдельный HttpClient без действующего Authorization-заголовка.
        // Иначе валидный токен из основного клиента мог бы повлиять на тест.
        val invalidService = PBApiService(
            baseUrl = BASE_URL,
            getToken = { null }
        )
        val invalidHttpClient = invalidService.create()

        val invalidRealtime = KtorRealtimeClient(
            connection = KtorWsConnection(
                httpClient = invalidHttpClient,
                wsUrl = WS_URL
            ),
            json = invalidService.json,
            getToken = { "invalid-token-for-test" }
        )

        try {
            val errorDeferred = async(start = CoroutineStart.UNDISPATCHED) {
                invalidRealtime.events
                    .filterIsInstance<ErrorEvent>()
                    .first()
            }

            invalidRealtime.connect()

            val error = withTimeout(TIMEOUT) {
                errorDeferred.await()
            }

            assertEquals("unauthorized", error.code)
        } finally {
            invalidRealtime.disconnect()
            invalidHttpClient.close()
        }
    }
}
