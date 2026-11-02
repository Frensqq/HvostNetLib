package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Slot (
    val time: String,
    val start: String,
    val free: Boolean
)