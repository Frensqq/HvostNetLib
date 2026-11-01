package com.example.netlib.domain.model

data class PetInput(
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: String,
    val photo: String
)