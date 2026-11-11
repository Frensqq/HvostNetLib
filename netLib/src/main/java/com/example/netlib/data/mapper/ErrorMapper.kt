package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.ErrorResponse
import com.example.netlib.domain.model.ApiError
import com.example.netlib.domain.model.ApiFieldError

fun ErrorResponse.toDomain(): ApiError {
    return ApiError(
        status = status,
        message = message,
        data = data.mapValues { (_, error) ->
            ApiFieldError(
                code = error.code,
                message = error.message
            )
        }
    )
}