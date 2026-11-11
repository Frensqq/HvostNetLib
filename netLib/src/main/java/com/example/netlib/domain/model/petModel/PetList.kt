package com.example.netlib.domain.model.petModel


data class PetList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Pet>
)