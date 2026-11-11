package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
data class PetInputRequest(
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Double,
    val photo: String?= null
)

@Serializable
data class PetRequest(
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Double,
    val photo: String? = null,
)

@Serializable
data class PetResponse(
    val id: String,
    val owner: String,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Double,
    val photo: String,
    val created: String,
    val updated: String
)


@Serializable
data class PetListResponse(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<PetResponse>
)



