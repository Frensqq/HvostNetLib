package com.example.netlib.domain.model

import kotlin.time.Duration

data class Service(
    val id: String,
    val code: String,
    val name: String,
    val price: Int,
    val duration: Int,
    val sort: Int,
)