package com.example.netlib.domain.model.appointmentsModel

data class Slot(
    val time: String,
    val start: String,
    val free: Boolean
)