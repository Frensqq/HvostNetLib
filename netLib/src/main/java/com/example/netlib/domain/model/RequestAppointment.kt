package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestAppointment(
    val pet: String,
    val doctor: String,
    val service: String,
    val start: String,
    val comment: String,
)