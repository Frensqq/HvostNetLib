package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val identity: String,
    val password: String
)

@Serializable
data class RegistrationRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
)




@Serializable
data class AuthResponse(
    val token: String,
    val record: UserResponse
)

