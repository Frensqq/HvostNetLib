package com.example.netlib.domain.model.appointmentsModel

import com.example.netlib.domain.model.catalogModel.Clinic
import com.example.netlib.domain.model.catalogModel.Doctor
import com.example.netlib.domain.model.catalogModel.Service
import com.example.netlib.domain.model.petModel.Pet


data class AppointmentExpand(
    val pet: Pet? = null,
    val doctor: Doctor? = null,
    val service: Service? = null,
    val clinic: Clinic? = null,
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
    val start: String,
    val comment: String,
    val cancelReason: String,
    val created: String,
    val updated: String,
    val expand: AppointmentExpand? = null
)