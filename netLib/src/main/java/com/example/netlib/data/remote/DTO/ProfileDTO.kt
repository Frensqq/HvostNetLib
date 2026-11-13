package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
internal data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val avatar: String,
    val verified: Boolean,
    val created: String,
    val updated: String
)



@Serializable
internal data class UserUpdateRequest(
    val name: String? = null,
    val phone: String? = null
)
