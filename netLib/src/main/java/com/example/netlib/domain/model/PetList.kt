package com.example.netlib.domain.model

data class PetList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Pet>
)