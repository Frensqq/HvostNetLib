package com.example.netlib.domain.model.catalogModel


data class Service(
    val id: String,
    val code: String,
    val name: String,
    val price: Int,
    val duration: Int,
    val sort: Int,
)

data class ServiceList(
    val page:Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val items: List<Service>
)