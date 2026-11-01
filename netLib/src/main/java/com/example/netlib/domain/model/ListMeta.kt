package com.example.netlib.domain.model

data class ListMeta(
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
)