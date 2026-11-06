package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentRequest(
    val pet: String,
    val doctor: String,
    val service: String,
    val start: String,
    val comment: String,
)

@Serializable
data class AppointmentExpandResponse(
    val pet: PetResponse? = null,
    val doctor: DoctorResponse? = null,
    val service: ServiceResponse? = null,
    val clinic: ClinicResponse? = null,
)

@Serializable
data class AppointmentResponse(
    val id: String,
    val number: Int,
    val user: String,
    val pet: String,
    val doctor: String,
    val service: String,
    val clinic: String,
    val status: String,
    val comment: String,
    val cancelReason: String,
    val created: String,
    val updated: String,
    val expand: AppointmentExpandResponse? = null
)


@Serializable
data class AppointmentListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<AppointmentResponse>
)

@Serializable
data class SlotResponse (
    val time: String,
    val start: String,
    val free: Boolean
)

@Serializable
data class SlotListResponse(
    val doctor: String,
    val date: String,
    val weekday: Int,
    val working: Boolean,
    val slots: List<SlotResponse>
)

@Serializable
data class CancelRequest(
    val reason: String? = null
)