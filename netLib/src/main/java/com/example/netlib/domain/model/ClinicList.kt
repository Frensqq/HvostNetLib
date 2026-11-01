package com.example.netlib.domain.model

data class ClinicList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Clinic>
)