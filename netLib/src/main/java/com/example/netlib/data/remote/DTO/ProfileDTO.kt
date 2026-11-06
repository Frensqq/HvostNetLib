package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
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
data class UserUpdateRequest(
    val name: String,
    val phone: String,
    val avatar: String)
