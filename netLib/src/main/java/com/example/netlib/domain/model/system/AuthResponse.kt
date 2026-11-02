package com.example.netlib.domain.model.system

import com.example.netlib.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val record: User
)