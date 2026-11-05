package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val hours: String,
    val sort: Int
)