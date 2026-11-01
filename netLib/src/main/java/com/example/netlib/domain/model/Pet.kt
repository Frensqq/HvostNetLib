package com.example.netlib.domain.model

data class Pet(
    val id: String,
    val owner: String,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: String,
    val weightKg: Number,
    val photo: String,
    val created: String,
    val updated: String
)