package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
data class ServiceResponse(
    val id: String,
    val code: String,
    val name: String,
    val price: Int,
    val duration: Int,
    val sort: Int,
)

@Serializable
data class ServiceListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<ServiceResponse>
)

@Serializable
data class ClinicResponse(
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
data class ClinicListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<ClinicResponse>
)




@Serializable
data class DoctorExpandResponse(
    val clinic : ClinicResponse
)

@Serializable
data class DoctorResponse(
    val id: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val rating: Double,
    val clinic: String,
    val services : List<String>,
    val workDays: List<Int>,
    val photo: String,
    val expand: DoctorExpandResponse
)

@Serializable
data class DoctorListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<DoctorResponse>
)
