package com.example.netlib.domain.model.catalogModel

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



data class ClinicList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Clinic>
)