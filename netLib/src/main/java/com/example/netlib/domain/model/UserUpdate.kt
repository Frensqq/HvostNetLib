package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdate(
    val name: String,
    val phone: String,
    val avatar: String
)