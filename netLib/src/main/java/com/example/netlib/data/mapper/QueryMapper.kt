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
    val filters = mutableListOf<String>()

    statuses
        ?.filter { it.isNotBlank() }
        ?.distinct()
        ?.takeIf { it.isNotEmpty() }
        ?.let { values ->
            filters.add(
                values.joinToString(
                    separator = " || ",
                    prefix = "(",
                    postfix = ")"
                ) { "status='$it'" }
            )
        }

    petId?.takeIf { it.isNotBlank() }?.let {
        filters.add("pet='$it'")
    }

    return filters.takeIf { it.isNotEmpty() }
        ?.joinToString(" && ")
}