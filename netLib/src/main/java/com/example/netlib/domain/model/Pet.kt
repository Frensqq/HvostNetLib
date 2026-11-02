package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
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