package com.example.netlib.domain.model.system

import kotlinx.serialization.Serializable

@Serializable
data class FieldError(
    val code: String? = null,
    val message: String? = null
)

@Serializable
data class ErrorResponse(
    val status: Int = 0,
    val message: String = "",
    val data: Map<String, FieldError> = emptyMap()
)