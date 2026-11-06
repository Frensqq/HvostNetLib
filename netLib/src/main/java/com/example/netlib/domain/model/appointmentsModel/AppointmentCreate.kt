package com.example.netlib.domain.model.appointmentsModel

data class AppointmentCreate (
    val pet: String,
    val doctor: String,
    val service: String,
    val start: String,
    val comment: String,
)