package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestPet(
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Double,
    val photo: String,
)