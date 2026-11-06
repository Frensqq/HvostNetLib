package com.example.netlib.domain.model.petModel

import com.example.netlib.data.remote.DTO.PetResponse

data class PetList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<PetResponse>
)