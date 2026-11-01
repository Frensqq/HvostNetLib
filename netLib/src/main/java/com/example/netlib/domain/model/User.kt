package com.example.netlib.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val avatar: String,
    val verified: Boolean,
    val created: String,
    val updated: String
)