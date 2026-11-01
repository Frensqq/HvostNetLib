package com.example.netlib.domain.model

data class RequestPet(
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Number,
    val photo: String,
)