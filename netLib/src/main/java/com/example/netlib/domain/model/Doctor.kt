package com.example.netlib.domain.model

import kotlinx.serialization.Serializable
@Serializable
data class DoctorExpand(
    val clinic : Clinic
)

@Serializable
data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val rating: Double,
    val clinic: String,
    val services : List<String>,
    val workDays: List<Int>,
    val photo: String,
    val expand: DoctorExpand
)