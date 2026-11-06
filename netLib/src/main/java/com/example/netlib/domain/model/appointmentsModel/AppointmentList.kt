package com.example.netlib.domain.model.appointmentsModel


data class AppointmentList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Appointment>
)

