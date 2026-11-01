package com.example.netlib.domain.model.system

data class ErrorResponse(
    val status : Int,
    val message: String,
    val data: Map<String, Any> = emptyMap()
)
