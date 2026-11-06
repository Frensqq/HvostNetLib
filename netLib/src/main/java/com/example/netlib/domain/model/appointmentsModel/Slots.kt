package com.example.netlib.domain.model.appointmentsModel

data class Slots(
    val doctor: String,
    val date: String,
    val weekday: Int,
    val working: Boolean,
    val slots: List<Slot>
)