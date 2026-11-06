package com.example.netlib.domain.repository

import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.appointmentsModel.Appointment
import com.example.netlib.domain.model.appointmentsModel.AppointmentCreate
import com.example.netlib.domain.model.appointmentsModel.AppointmentList
import com.example.netlib.domain.model.appointmentsModel.Cancel
import com.example.netlib.domain.model.appointmentsModel.Slots

interface AppointmentsRepository {

    suspend fun getAppointment(
        filter: String? = null,
    ): NetworkResult<AppointmentList>

    suspend fun postAppointment(
        data: AppointmentCreate
    ): NetworkResult<Appointment>

    suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): NetworkResult< Appointment>

    suspend fun getSlots(doctorId: String, date: String): NetworkResult<Slots>
}