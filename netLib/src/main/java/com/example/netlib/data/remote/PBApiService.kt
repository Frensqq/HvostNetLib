package com.example.netlib.data.remote

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

    val instance: PBApi by lazy {
        PBApi(
            client = HttpClient(OkHttp) {
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

                install(Logging) {
                    level = LogLevel.ALL
                }

                install(HttpTimeout) {
                    requestTimeoutMillis = 30_000
                }

                defaultRequest {
                    url(BASE_URL)
                    token?.let { header("Authorization", it) }
                }
            },
        )
    }


    val wsClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(WebSockets) {
                pingInterval = 15_000
            }
            install(Logging) {
                level = LogLevel.INFO   // токен в URL не должен светиться в логах
            }
        }
    }
}
