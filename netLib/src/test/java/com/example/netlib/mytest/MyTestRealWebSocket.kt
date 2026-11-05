
package com.example.netlib.mytest

import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.KtorWsConnection
import com.example.netlib.data.remote.PBApiService
import com.example.netlib.data.remote.parse
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.websocket.ErrorEvent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.put
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertTrue


//тесты с реальным сервером
class MyTestRealWebSocket {

    private suspend fun authorize(): String {
        val response = PBApiService.instance.authorization(
            AuthRequest(
                identity = "demo@hvost.local",
                password = "Demo2027!"
            )
        )

        PBApiService.token = response.token

        assertTrue(response.token.isNotBlank())

        return response.token
    }

    private fun createClient(): KtorRealtimeClient {
        return KtorRealtimeClient(
            KtorWsConnection(
                httpClient = PBApiService.httpClient,
                wsUrl = PBApiService.WS_URL
            )
        )
    }

    private suspend fun getFirstRecord(path: String) =
        PBApiService.httpClient.get(path) {
            parameter("perPage", 1)
        }.bodyAsText().let { response ->
            Json.parseToJsonElement(response)
                .jsonObject["items"]!!
                .jsonArray.first()
                .jsonObject
        }

    @Test
    fun connectsToRealServerAndReceivesHello() = runBlocking {
        val token = authorize()
        val client = createClient()

        try {
            val hello = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "HelloEvent"
                    }
                }
            }

            client.connect(token)

            val event = hello.await()

            assertEquals("HelloEvent", event::class.simpleName)
        } finally {
            client.disconnect()
        }
    }

    @Test
    fun sendsPingAndReceivesPong() = runBlocking {
        val token = authorize()
        val client = createClient()

        try {
            val hello = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "HelloEvent"
                    }
                }
            }

            client.connect(token)
            hello.await()

            val pong = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "PongEvent"
                    }
                }
            }

            client.ping()

            val event = pong.await()

            assertEquals("PongEvent", event::class.simpleName)
        } finally {
            client.disconnect()
        }
    }

    @Test
    fun subscribesAndUnsubscribesFromRealServer() = runBlocking {
        val token = authorize()

        val doctor = getFirstRecord(
            "collections/doctors/records"
        )

        val doctorId = doctor["id"]!!
            .jsonPrimitive.content

        val client = createClient()

        try {
            val hello = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "HelloEvent"
                    }
                }
            }

            client.connect(token)
            hello.await()

            val subscribed = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "SubscribedEvent"
                    }
                }
            }

            val date = LocalDate.now()
                .plusDays(1)
                .toString()

            client.subscribeSlots(doctorId, date)

            assertEquals(
                "SubscribedEvent",
                subscribed.await()::class.simpleName
            )

            val unsubscribed = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "UnsubscribedEvent"
                    }
                }
            }

            client.unsubscribeSlots(doctorId, date)

            assertEquals(
                "UnsubscribedEvent",
                unsubscribed.await()::class.simpleName
            )
        } finally {
            client.disconnect()
        }
    }

    @Test
    fun startsQueueDemoForRealAppointment() = runBlocking {
        val token = authorize()

        // Получаем питомца пользователя.
        val pet = getFirstRecord(
            "collections/pets/records"
        )

        val petId = pet["id"]!!
            .jsonPrimitive.content

        // Получаем врача и одну из его услуг.
        val doctor = getFirstRecord(
            "collections/doctors/records"
        )

        val doctorId = doctor["id"]!!
            .jsonPrimitive.content

        val serviceId = doctor["services"]!!
            .jsonArray.first()
            .jsonPrimitive.content

        // Ищем первый свободный слот на ближайшие 14 дней.
        val moscowZone = ZoneId.of("Europe/Moscow")
        val today = LocalDate.now(moscowZone)

        var freeStart: String? = null

        for (dayOffset in 1..14) {
            val date = today.plusDays(dayOffset.toLong()).toString()

            val slotsResponse = PBApiService.httpClient.get(
                "hvost/slots"
            ) {
                parameter("doctor", doctorId)
                parameter("date", date)
            }.bodyAsText()

            val slots = Json.parseToJsonElement(slotsResponse)
                .jsonObject["slots"]!!
                .jsonArray

            val freeSlot = slots.firstOrNull {
                it.jsonObject["free"]
                    ?.jsonPrimitive
                    ?.booleanOrNull == true
            }

            if (freeSlot != null) {
                freeStart = freeSlot.jsonObject["start"]!!
                    .jsonPrimitive.content
                break
            }
        }

        val start = requireNotNull(freeStart) {
            "Не найден свободный слот на ближайшие 14 дней"
        }

        // Создаём настоящую запись через REST API.
        val createdAppointment = PBApiService.httpClient.post(
            "collections/appointments/records"
        ) {
            contentType(ContentType.Application.Json)

            setBody(
                buildJsonObject {
                    put("pet", petId)
                    put("doctor", doctorId)
                    put("service", serviceId)
                    put("start", start)
                    put("comment", "WebSocket integration test")
                }
            )
        }.bodyAsText()

        val appointmentId = Json.parseToJsonElement(
            createdAppointment
        ).jsonObject["id"]!!
            .jsonPrimitive.content

        // Подключаемся к WebSocket и запускаем демонстрацию очереди.
        val client = createClient()

        try {
            val hello = async {
                withTimeout(10_000) {
                    client.events.first {
                        it::class.simpleName == "HelloEvent"
                    }
                }
            }

            client.connect(token)
            hello.await()

            client.requestQueueDemo(appointmentId)

            val response = withTimeout(10_000) {
                client.events.first {
                    it is com.example.netlib.domain.model.websocket.QueueDemoStartedEvent ||
                            it is ErrorEvent
                }
            }

            assertTrue(
                response is com.example.netlib.domain.model.websocket.QueueDemoStartedEvent,
                "Ответ WebSocket-сервера: $response"
            )
        } finally {
            client.disconnect()
        }
    }

    @Test
    fun rejectsInvalidTokenWithUnauthorizedAndClose4001() = runBlocking {
        val connection = KtorWsConnection(
            httpClient = PBApiService.httpClient,
            wsUrl = PBApiService.WS_URL
        )

        connection.connect("invalid-test-token")

        var unauthorizedReceived = false

        val closeCode = connection.listen { message ->
            val event = parse(message)

            if (event is ErrorEvent && event.code == "unauthorized") {
                unauthorizedReceived = true
            }
        }

        assertTrue(unauthorizedReceived)
        assertEquals(4001, closeCode)

        connection.disconnect()
    }
}
