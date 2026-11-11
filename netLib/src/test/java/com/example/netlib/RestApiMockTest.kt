package com.example.netlib

import com.example.netlib.data.remote.PBApi
import com.example.netlib.data.repository.AppointmentRepositoryImpl
import com.example.netlib.data.repository.AuthRepositoryImpl
import com.example.netlib.data.storage.TokenStorage
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.network.Network
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.client.request.url
import org.junit.Test
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RestApiMockTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun createClient(
        response: String,
        status: HttpStatusCode = HttpStatusCode.OK
    ) = HttpClient(MockEngine {
        respond(
            content = response,
            status = status,
            headers = headersOf(
                HttpHeaders.ContentType,
                "application/json"
            )
        )
    }) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        defaultRequest {
            url("http://localhost:8090/api/")
        }
    }

    private val onlineNetwork = object : Network {
        override fun isConnected(): Boolean = true
    }

    @Test
    fun authorizationSavesToken() = runTest {
        val response = """
            {
                "token": "test-token",
                "record": {
                    "id": "user-1",
                    "email": "test@mail.com",
                    "name": "Test",
                    "phone": "123456789",
                    "avatar": "",
                    "verified": true,
                    "created": "2026-01-01",
                    "updated": "2026-01-01"
                }
            }
        """.trimIndent()

        val client = createClient(response)
        val tokenStorage = TokenStorage()
        val repository = AuthRepositoryImpl(
            PBApi(client),
            onlineNetwork,
            tokenStorage
        )

        val result = repository.authorization(
            AuthData(
                identity = "test@mail.com",
                password = "password"
            )
        )

        assertIs<NetworkResult.Success<*>>(result)
        assertEquals("test-token", tokenStorage.getToken())

        client.close()
    }

    @Test
    fun authorizationWithWrongPasswordReturnsError() = runTest {
        val response = """
            {
                "status": 400,
                "message": "Failed to authenticate.",
                "data": {}
            }
        """.trimIndent()

        val client = createClient(response, HttpStatusCode.BadRequest)
        val repository = AuthRepositoryImpl(
            PBApi(client),
            onlineNetwork,
            TokenStorage()
        )

        val result = repository.authorization(
            AuthData(
                identity = "test@mail.com",
                password = "wrong-password"
            )
        )

        val error = assertIs<NetworkResult.Error>(result).errorResponse

        assertEquals(400, error.status)
        assertEquals("Failed to authenticate.", error.message)

        client.close()
    }

    @Test
    fun getSlotsReturns16Slots() = runTest {
        val slots = (0 until 16).joinToString(",") { index ->
            """
            {
                "time": "${9 + index / 2}:${if (index % 2 == 0) "00" else "30"}",
                "start": "2026-11-11T${9 + index / 2}:${if (index % 2 == 0) "00" else "30"}:00Z",
                "free": ${index % 2 == 0}
            }
            """.trimIndent()
        }

        val response = """
            {
                "doctor": "doctor-1",
                "date": "2026-11-11",
                "weekday": 3,
                "working": true,
                "slots": [$slots]
            }
        """.trimIndent()

        val client = createClient(response)
        val repository = AppointmentRepositoryImpl(
            PBApi(client),
            onlineNetwork
        )

        val result = repository.getSlots("doctor-1", "2026-11-11")

        val data = assertIs<NetworkResult.Success<*>>(result).data
        val slotsResult = data as com.example.netlib.domain.model.appointmentsModel.Slots

        assertEquals(16, slotsResult.slots.size)
        for (i in 0 until 16) {
            assertEquals(i % 2 == 0, slotsResult.slots[i].free)
        }

        client.close()
    }

    @Test
    fun occupiedSlotReturnsStartFieldError() = runTest {
        val response = """
            {
                "status": 400,
                "message": "Это время уже занято",
                "data": {
                    "start": {
                        "code": "validation_not_unique",
                        "message": "Это время уже занято"
                    }
                }
            }
        """.trimIndent()

        val client = createClient(response, HttpStatusCode.BadRequest)
        val repository = AppointmentRepositoryImpl(
            PBApi(client),
            onlineNetwork
        )

        val result = repository.getSlots("doctor-1", "2026-11-11")

        val error = assertIs<NetworkResult.Error>(result).errorResponse

        assertEquals(400, error.status)
        assertTrue(error.data.containsKey("start"))
        assertEquals(
            "Это время уже занято",
            error.data["start"]?.message
        )

        client.close()
    }

    @Test
    fun noInternetReturnsNetworkError() = runTest {
        val offlineNetwork = object : Network {
            override fun isConnected(): Boolean = false
        }

        val client = createClient("{}")
        val repository = AppointmentRepositoryImpl(
            PBApi(client),
            offlineNetwork
        )

        val result = repository.getSlots("doctor-1", "2026-11-11")

        assertIs<NetworkResult.NoInternet>(result)

        client.close()
    }
}