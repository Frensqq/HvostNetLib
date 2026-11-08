package com.example.netlib

import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.RequestAppointment
import com.example.netlib.domain.model.Slots
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.system.AuthResponse
import com.example.netlib.domain.model.system.NetworkResult
import com.example.netlib.domain.network.Network
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders

import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import io.ktor.utils.io.errors.IOException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PBRepositoryMockTest {
    private val networkOnlineMock  = object : Network {
        override fun isConnected(): Boolean = true
    }

    private val networkOfflineMock  = object : Network{
        override fun isConnected(): Boolean = false
    }

    private fun createMockRepository(
        status: HttpStatusCode = HttpStatusCode.OK,
        responseJson: String = "{}",
        throwIOException: Boolean = false
    ): PBRepositoryImpl {
        val mockEngine = MockEngine { request ->
            if (throwIOException) throw IOException("No internet")
            respond(
                content = responseJson,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine){
            expectSuccess=true
            install(ContentNegotiation){
                json(Json{
                    ignoreUnknownKeys = true
                })
            }
        }

        val api = PBApi(client)
        return PBRepositoryImpl(api , networkOnlineMock)
    }


    @Test
    fun TestAuthorizationAndSaveToken() = runTest {
        val jsonResponse = """
    {
      "token": "testToken",
      "record": {
        "id": "1",
        "email": "demo@hvost.local",
        "name": "Демо",
        "phone": "+79001234567",
        "avatar": "",
        "verified": true,
        "created": "2026-11-01 09:00:00.000Z",
        "updated": "2026-11-01 09:00:00.000Z"
      }
    }
""".trimIndent()
        val repo = createMockRepository(
            responseJson = jsonResponse
        )

        val result = repo.authorization(AuthRequest("demo@hvost.local", "Demo2027!"))



        assertIs<NetworkResult.Success<AuthResponse>>(result)
        assertEquals("testToken", result.data.token)

        repo.saveToken(result.data.token)
        assertEquals("testToken", repo.getToken())
    }


    @Test
    fun testAuthorizationWrongPassword() = runTest{
        val errorJson = """{ "status": 400, "message": "Failed to authenticate.", "data": {} }"""

        val repo = createMockRepository(
            status = HttpStatusCode.BadRequest,
            responseJson = errorJson
        )

        val result  = repo.authorization(data = AuthRequest("demo@hvost.local", "WrongPass"))

        assertIs<NetworkResult.Error>(result)
        assertEquals(400, result.errorResponse.status)
        assertTrue(result.errorResponse.message.contains("Failed to authenticate."))

    }

    @Test
    fun testGet16FreeSlots() = runTest {
        val slotsJson = (0 until 16).joinToString(",") { i ->
            val time = "09:${i.toString().padStart(2, '0')}"
            """{"time":"$time","start":"2026-11-02 06:00:00.000Z","free":${i % 2 == 0}}"""
        }
        val jsonResponse = """
        {
          "doctor": "doc1",
          "date": "2026-11-02",
          "weekday": 1,
          "working": true,
          "slots": [ $slotsJson ]
        }
    """.trimIndent()

        val repo = createMockRepository(responseJson = jsonResponse)
        val result = repo.getSlots("doc1", "2026-11-02")

        assertIs<NetworkResult.Success<Slots>>(result)
        val slots = result.data.slots
        assertEquals(16, slots.size)
        assertTrue(slots[0].free)
        assertTrue(!slots[1].free)
        assertEquals("09:00", slots[0].time)
        assertEquals("09:01", slots[1].time)
    }

    @Test
    fun testSlots400Response() = runTest {
        val errorJson = """
        { "status": 400, "message": "Это время уже занято.", "data": { "start": { "code": "slot_taken", "message": "Это время уже занято." } } }
        """.trimIndent()

        val repo = createMockRepository(
            status = HttpStatusCode.BadRequest,
            responseJson = errorJson
        )
        val result = repo.postAppointment(
            data = RequestAppointment(
                "testPet",
                "testDoctor",
                "testService",
                "2026-11-02 06:30:00.000Z",
                "TestComment"
            )
        )

        assertIs<NetworkResult.Error>(result)

        assertEquals(400, result.errorResponse.status)
        assertTrue(
            result.errorResponse.message.contains("Это время уже занято"),
            "Ожидали текст про занятое время. Получили: '${result.errorResponse.message}'"
        )

        val startError = result.errorResponse.data["start"]
        assertNotNull(startError)
        assertEquals("slot_taken", startError.code)
    }

    @Test
    fun testNoNetwork() = runTest {


            val repo = PBRepositoryImpl(PBApi(HttpClient()), networkOfflineMock)

            val result = repo.getServices()

            assertIs<NetworkResult.NoInternet>(result)
    }



}
