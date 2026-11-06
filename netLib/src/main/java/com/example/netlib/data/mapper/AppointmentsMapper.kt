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

fun AppointmentCreate.toRequest(): AppointmentRequest {
    return AppointmentRequest(
        pet = pet,
        doctor = doctor,
        service = service,
        start = start,
        comment = comment
    )
}

fun Cancel.toRequest(): CancelRequest {
    return CancelRequest(
        reason = reason
    )
}

fun AppointmentListResponse.toDomain(): AppointmentList{
    return AppointmentList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items =  items.map { it.toDomain() }
    )
}
fun AppointmentResponse.toDomain(): Appointment {
    return Appointment(
        id = id,
        number = number,
        user = user,
        pet = pet,
        doctor = doctor,
        service = service,
        clinic = clinic,
        status = status,
        comment = comment,
        cancelReason = cancelReason,
        created = created,
        updated = updated,
        //expand = expand.toDomain()
    )
}

fun SlotResponse.toDomain(): Slot {
    return Slot(
        free = free,
        time = time,
        start = start
    )
}

fun SlotListResponse.toDomain(): Slots{
    return Slots(
        doctor = doctor,
        date= date,
        weekday = weekday,
        working = working,
        slots = slots.map { it.toDomain() }
    )
}

//fun AppointmentExpandResponse.toDomain(): AppointmentExpand{
//    return AppointmentExpand(
//        pet =pet,
//
//    )
//}