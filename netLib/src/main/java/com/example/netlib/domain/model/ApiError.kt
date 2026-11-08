package com.example.netlib.domain.model

data class ApiError(
    val status: Int = 0,
    val message: String = "",
    val data: Map<String, ApiFieldError> = emptyMap()
)

data class ApiFieldError(
    val code: String? = null,
    val message: String? = null
)