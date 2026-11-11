package com.example.netlib.domain.model.query

data class ServiceQuery(
    val page: Int? = null,
    val perPage: Int? = null,
    val sort: String? = null
)

data class ClinicQuery(
    val page: Int? = null,
    val perPage: Int? = null,
    val sort: String? = null
)

data class DoctorQuery(
    val serviceId: String? = null,
    val clinicId: String? = null,
    val page: Int? = null,
    val perPage: Int? = null,
    val sort: String? = null
)

data class PetQuery(
    val page: Int? = null,
    val perPage: Int? = null,
    val sort: String? = "name"
)

data class AppointmentQuery(
    val statuses: List<String>? = null,
    val petId: String? = null,
    val sort: String? = "-start",
    val page: Int? = null,
    val perPage: Int? = null
)