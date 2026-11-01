package com.example.netlib.domain.model

data class DoctorExpand(
    val clinic : Clinic
)


data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val rating: Number,
    val clinic: String,
    val services : List<String>,
    val workDays: List<Int>,
    val photo: String,
    val expand: DoctorExpand
)