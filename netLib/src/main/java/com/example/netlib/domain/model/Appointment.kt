package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentExpand(
    val pet: Pet,
    val doctor: Doctor,
    val service: Service,
    val clinic: Clinic
)

@Serializable
data class Appointment(
    val id: String,
    val number: Int,
    val user: String,
    val pet: String,
    val doctor: String,
    val service: String,
    val clinic: String,
    val status: String,
    val comment: String,
    val cancelReason: String,
    val created: String,
    val updated: String,
    val expand: AppointmentExpand
)