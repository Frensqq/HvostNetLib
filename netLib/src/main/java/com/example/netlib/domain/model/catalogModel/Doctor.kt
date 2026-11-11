package com.example.netlib.domain.model.catalogModel



data class DoctorExpand(
    val clinic : Clinic
)

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
    val expand: DoctorExpand? = null
)

data class DoctorList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Doctor>
)