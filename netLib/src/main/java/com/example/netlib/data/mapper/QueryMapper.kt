package com.example.netlib.data.mapper

import com.example.netlib.domain.model.query.AppointmentQuery
import com.example.netlib.domain.model.query.DoctorQuery

fun DoctorQuery.toFilter(): String? {
    val filters = mutableListOf<String>()

    serviceId?.let {
        filters.add("services~'$it'")
    }

    clinicId?.let {
        filters.add("clinic='$it'")
    }

    return filters.takeIf { it.isNotEmpty() }
        ?.joinToString(" && ")
}

fun AppointmentQuery.toFilter(): String? {
    val values = statuses
        ?.filter { it.isNotBlank() }
        ?.distinct()
        ?: return null

    if (values.isEmpty()) return null

    return values.joinToString(
        separator = " || ",
        prefix = "(",
        postfix = ")"
    ) {
        "status='$it'"
    }
}