package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Slots(
    val doctor: String,
    val date: String,
    val weekday: Int,
    val working: Boolean,
    val slots: List<Slot>
)