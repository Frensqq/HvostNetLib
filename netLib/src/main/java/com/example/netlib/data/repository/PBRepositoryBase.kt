package com.example.netlib.data.repository

import com.example.netlib.data.remote.DTO.ErrorResponse
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.network.Network
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json

abstract class PBRepositoryBase (
    protected val networkMonitor: Network
){
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        if (!networkMonitor.isConnected()) {
            return NetworkResult.NoInternet
        }

        return try {
            NetworkResult.Success(apiCall())
        } catch (e: HttpRequestTimeoutException) {
            NetworkResult.Timeout
        } catch (e: IOException) {
            NetworkResult.NoInternet
        } catch (e: ResponseException) {
            val text = runCatching { e.response.bodyAsText() }.getOrDefault("")
            val error = runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString<ErrorResponse>(text)
            }.getOrElse {
                ErrorResponse(status = e.response.status.value, message = e.message.orEmpty())
            }
            NetworkResult.Error(error)
        } catch (e: Exception) {
            NetworkResult.Error(
                ErrorResponse(status = -1, message = e.message ?: "Unknown error")
            )
        }
    }
}