package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
internal data class ServiceResponse(
    val id: String,
    val code: String,
    val name: String,
    val price: Int,
    val durationMin: Int,
    val sort: Int,
)

@Serializable
internal data class ServiceListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<ServiceResponse>
)

@Serializable
internal data class ClinicResponse(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val hours: String,
    val sort: Int
)


@Serializable
internal data class ClinicListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<ClinicResponse>
)




@Serializable
internal data class DoctorExpandResponse(
    val clinic : ClinicResponse
)

@Serializable
internal data class DoctorResponse(
    val id: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val rating: Double,
    val clinic: String,
    val services : List<String>,
    val workDays: List<Int>,
    val photo: String,
    val expand: DoctorExpandResponse? = null
)

@Serializable
internal data class DoctorListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<DoctorResponse>
)
