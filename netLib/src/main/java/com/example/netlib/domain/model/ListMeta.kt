package com.example.netlib.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ListMeta(
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
)