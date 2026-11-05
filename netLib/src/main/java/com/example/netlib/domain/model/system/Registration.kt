package com.example.netlib.domain.model.system

import kotlinx.serialization.Serializable

@Serializable
data class Registration(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
)