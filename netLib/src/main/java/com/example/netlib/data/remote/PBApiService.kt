
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
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class PBApiService(
    private val baseUrl: String,
    private val getToken: () -> String?
) {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun create(): HttpClient {
       return HttpClient(OkHttp) {
                expectSuccess = true

                install(ContentNegotiation) {
                    json(json)
                }

                install(Logging) {
                    level = LogLevel.HEADERS

                    sanitizeHeader { header ->
                        header == HttpHeaders.Authorization
                    }
                }

                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                }

                install(WebSockets) {
                    pingInterval = 0
                }

                defaultRequest {
                    url(baseUrl)
                    getToken()?.let {
                        header(HttpHeaders.Authorization, it)
                    }
                }
            }

    }
}
