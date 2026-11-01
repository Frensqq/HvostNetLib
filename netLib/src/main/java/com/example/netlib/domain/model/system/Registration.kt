package com.example.netlib.domain.model.system

data class Registration(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String,
    val phone: String,
)