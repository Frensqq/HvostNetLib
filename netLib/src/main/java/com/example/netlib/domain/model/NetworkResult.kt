package com.example.netlib.domain.model


sealed class NetworkResult<out T> {

    data class Success<T>(
        val data: T
    ) : NetworkResult<T>()

    data class Error(
        val errorResponse: ApiError
    ) : NetworkResult<Nothing>()

    object NoInternet : NetworkResult<Nothing>()

    object Timeout : NetworkResult<Nothing>()
}