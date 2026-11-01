package com.example.netlib.domain.model

data class DoctorList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Doctor>
)