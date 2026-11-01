package com.example.netlib.domain.model.system

data class AuthRequest(
    val identity: String,
    val password: String
)