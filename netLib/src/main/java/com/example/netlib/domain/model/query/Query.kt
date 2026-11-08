package com.example.netlib.domain.model.query

data class ServiceQuery(
    val sort: String? = null
)

data class ClinicQuery(
    val sort: String? = null
)

data class DoctorQuery(
    val serviceId: String? = null,
    val clinicId: String? = null,
    val sort: String? = null
)

data class PetQuery(
    val page: Int? = null,
    val perPage: Int? = null,
    val sort: String? = null
)

data class AppointmentQuery(
    val statuses: List<String>? = null,
    val sort: String? = null,
    val page: Int? = null,
    val perPage: Int? = null
)