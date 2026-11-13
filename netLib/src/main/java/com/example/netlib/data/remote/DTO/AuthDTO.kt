package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthRequest(
    val identity: String,
    val password: String
)

@Serializable
internal data class RegistrationRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
)




@Serializable
internal data class AuthResponse(
    val token: String,
    val record: UserResponse
)

