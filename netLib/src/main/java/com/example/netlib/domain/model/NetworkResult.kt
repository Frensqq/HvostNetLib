package com.example.netlib.domain.model

import com.example.netlib.data.remote.DTO.ErrorResponse

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T): NetworkResult<T>()
    data class Error(val errorResponse: ErrorResponse) : NetworkResult<Nothing>()
    object NoInternet: NetworkResult<Nothing>()

    object Timeout : NetworkResult<Nothing>()
}