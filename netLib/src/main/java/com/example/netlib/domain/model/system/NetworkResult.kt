package com.example.netlib.domain.model.system

import kotlinx.serialization.Serializable

@Serializable
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T): NetworkResult<T>()
    data class Error(val errorResponse: ErrorResponse) : NetworkResult<Nothing>()
    object NoInternet: NetworkResult<Nothing>()
}