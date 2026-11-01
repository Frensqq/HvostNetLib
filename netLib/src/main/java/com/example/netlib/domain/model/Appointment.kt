package com.example.netlib.domain.model


data class AppointmentExpand(
    val pet: Pet,
    val doctor: Doctor,
    val service: Service,
    val clinic: Clinic
)

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