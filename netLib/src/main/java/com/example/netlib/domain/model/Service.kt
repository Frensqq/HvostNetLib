package com.example.netlib.domain.model

import kotlinx.serialization.Serializable
@Serializable
data class Service(
    val id: String,
    val code: String,
    val name: String,
    val price: Int,
    val duration: Int,
    val sort: Int,
)