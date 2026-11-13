
package com.example.netlib.data.mapper

import com.example.netlib.domain.model.query.AppointmentQuery
import com.example.netlib.domain.model.query.DoctorQuery

private fun String.escapeFilterValue(): String {
    return replace("\\", "\\\\")
        .replace("'", "\\'")
}

internal fun DoctorQuery.toFilter(): String? {
    val filters = mutableListOf<String>()

    serviceId?.takeIf { it.isNotBlank() }?.let {
        filters.add("services~'${it.escapeFilterValue()}'")
    }

    clinicId?.takeIf { it.isNotBlank() }?.let {
        filters.add("clinic='${it.escapeFilterValue()}'")
    }

    return filters.takeIf { it.isNotEmpty() }
        ?.joinToString(" && ")
}

internal fun AppointmentQuery.toFilter(): String? {
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
                ) { "status='${it.escapeFilterValue()}'" }
            )
        }

    petId?.takeIf { it.isNotBlank() }?.let {
        filters.add("pet='${it.escapeFilterValue()}'")
    }

    return filters.takeIf { it.isNotEmpty() }
        ?.joinToString(" && ")
}
