package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.AppointmentExpandResponse
import com.example.netlib.data.remote.DTO.AppointmentListResponse
import com.example.netlib.data.remote.DTO.AppointmentRequest
import com.example.netlib.data.remote.DTO.AppointmentResponse
import com.example.netlib.data.remote.DTO.CancelRequest
import com.example.netlib.data.remote.DTO.SlotListResponse
import com.example.netlib.data.remote.DTO.SlotResponse
import com.example.netlib.domain.model.appointmentsModel.Appointment
import com.example.netlib.domain.model.appointmentsModel.AppointmentCreate
import com.example.netlib.domain.model.appointmentsModel.AppointmentExpand
import com.example.netlib.domain.model.appointmentsModel.AppointmentList
import com.example.netlib.domain.model.appointmentsModel.Cancel
import com.example.netlib.domain.model.appointmentsModel.Slot
import com.example.netlib.domain.model.appointmentsModel.Slots

internal fun AppointmentCreate.toRequest(): AppointmentRequest {
    return AppointmentRequest(
        pet = pet,
        doctor = doctor,
        service = service,
        start = start,
        comment = comment
    )
}

internal fun Cancel.toRequest(): CancelRequest {
    return CancelRequest(
        reason = reason
    )
}

internal fun AppointmentListResponse.toDomain(): AppointmentList{
    return AppointmentList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items =  items.map { it.toDomain() }
    )
}

internal fun AppointmentExpandResponse.toDomain(): AppointmentExpand{
    return AppointmentExpand(
       pet = pet?.toDomain(),
       doctor = doctor?.toDomain(),
       service =  service?.toDomain(),
       clinic =  clinic?.toDomain()
    )
}


internal fun AppointmentResponse.toDomain(): Appointment {
    return Appointment(
        id = id,
        number = number,
        user = user,
        pet = pet,
        doctor = doctor,
        start = start,
        service = service,
        clinic = clinic,
        status = status,
        comment = comment,
        cancelReason = cancelReason,
        created = created,
        updated = updated,
        expand = expand?.toDomain()
    )
}

internal fun SlotResponse.toDomain(): Slot {
    return Slot(
        free = free,
        time = time,
        start = start
    )
}

internal fun SlotListResponse.toDomain(): Slots{
    return Slots(
        doctor = doctor,
        date= date,
        weekday = weekday,
        working = working,
        slots = slots.map { it.toDomain() }
    )
}
