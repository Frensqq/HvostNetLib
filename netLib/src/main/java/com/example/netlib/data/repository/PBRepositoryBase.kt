package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.remote.DTO.ErrorResponse
import com.example.netlib.domain.model.ApiError
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.network.Network
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

abstract class PBRepositoryBase(
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

            val text = try {
                e.response.bodyAsText()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ""
            }

            val error = runCatching {
                Json { ignoreUnknownKeys = true }
                    .decodeFromString<ErrorResponse>(text)
                    .toDomain()
            }.getOrElse {
                ApiError(
                    status = e.response.status.value,
                    message = e.message.orEmpty()
                )
            }

            NetworkResult.Error(error)

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