package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.remote.DTO.ErrorResponse
import com.example.netlib.domain.model.ApiError
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.network.Network
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

internal abstract class PBRepositoryBase(
    protected val networkMonitor: Network
) {
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {

        if (!networkMonitor.isConnected()) {
            return NetworkResult.NoInternet
        }

        return try {
            NetworkResult.Success(apiCall())

        } catch (e: CancellationException) {
            throw e

        } catch (e: HttpRequestTimeoutException) {
            NetworkResult.Timeout

        } catch (e: IOException) {
            NetworkResult.NoInternet

        } catch (e: ResponseException) {

            val status = e.response.status.value

            val text = try {
                e.response.bodyAsText()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return NetworkResult.Error(
                    ApiError(
                        status = status,
                        message = "Не удалось прочитать тело ответа: ${e.message}"
                    )
                )
            }

            val error = try {
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<ErrorResponse>(text)
                    .toDomain()

            } catch (e: CancellationException) {
                throw e

            } catch (e: SerializationException) {
                return NetworkResult.SerializationError(
                    message = "Ошибка разбора JSON ответа сервера: ${e.message}",
                    status = status
                )

            } catch (e: Exception) {
                return NetworkResult.SerializationError(
                    message = "Некорректный формат ошибки сервера: ${e.message}",
                    status = status
                )
            }

            NetworkResult.Error(error)

        } catch (e: JsonConvertException) {
            NetworkResult.SerializationError(
                message = e.cause?.message ?: e.message ?: "JSON parsing error"
            )

        } catch (e: SerializationException) {
            NetworkResult.SerializationError(
                message = e.message ?: "JSON parsing error"
            )

        } catch (e: Exception) {
            NetworkResult.Error(
                ApiError(
                    status = -1,
                    message = e.message ?: "Unknown error"
                )
            )
        }
    }
}