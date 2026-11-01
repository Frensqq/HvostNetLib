package com.example.netlib.domain.model

data class AuthResponse(
    val token: String,
    val record: User
)