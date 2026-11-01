package com.example.netlib.domain.model

data class Clinic(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val lat: Number,
    val lng: Number,
    val hours: String,
    val sort: Int
)