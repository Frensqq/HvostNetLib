package com.example.netlib.domain.model.system

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val identity: String,
    val password: String
)