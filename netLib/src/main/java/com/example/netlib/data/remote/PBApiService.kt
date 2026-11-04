package com.example.netlib.data.remote

import com.example.netlib.data.webSoket.KtorRealtimeClient
import com.example.netlib.data.webSoket.KtorWsTransport
import com.example.netlib.domain.webSoket.HvostRealtimeClient
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
    const val WS_URL   = "ws://frens-project.tech:8091/ws"

    var token: String? = null

    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }

            install(Logging) { level = LogLevel.ALL }

            install(HttpTimeout) { requestTimeoutMillis = 30_000 }

            install(WebSockets) {
                pingInterval = 0   // сервер сам шлёт ping каждые 15 с
            }

            defaultRequest {
                url(BASE_URL)
                token?.let { header("Authorization", it) }
            }
        }
    }

    val instance: PBApi by lazy { PBApi(client = httpClient) }

    val realtime: HvostRealtimeClient by lazy {
        KtorRealtimeClient(
            transport = KtorWsTransport(httpClient),
            baseUrl = WS_URL,
        )
    }
}