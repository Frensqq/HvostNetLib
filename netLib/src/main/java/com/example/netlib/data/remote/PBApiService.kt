
package com.example.netlib.data.remote

import com.example.netlib.domain.websocket.HvostRealtimeClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object PBApiService {

    const val BASE_URL = "http://frens-project.tech:8090/api/"
    const val WS_URL = "ws://frens-project.tech:8091/ws"

    var token: String? = null

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(PBApiService.json)
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
            }

            install(WebSockets) {
                pingInterval = 0
            }

            defaultRequest {
                url(BASE_URL)
                token?.let { header("Authorization", it) }
            }
        }
    }

    val instance: PBApi by lazy {
        PBApi(client = httpClient)
    }

    val realtime: HvostRealtimeClient by lazy {
        KtorRealtimeClient(
            connection = KtorWsConnection(
                httpClient = httpClient,
                wsUrl = WS_URL
            )
        )
    }
}
