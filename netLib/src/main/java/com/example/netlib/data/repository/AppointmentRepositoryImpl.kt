package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toFilter
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.appointmentsModel.Appointment
import com.example.netlib.domain.model.appointmentsModel.AppointmentCreate
import com.example.netlib.domain.model.appointmentsModel.AppointmentList
import com.example.netlib.domain.model.appointmentsModel.Cancel
import com.example.netlib.domain.model.appointmentsModel.Slots
import com.example.netlib.domain.model.query.AppointmentQuery
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.AppointmentsRepository

class AppointmentRepositoryImpl(
    private val api: PBApi,
    networkMonitor: Network
) : PBRepositoryBase(networkMonitor), AppointmentsRepository {

    override suspend fun getAppointment(
        query: AppointmentQuery?
    ): NetworkResult<AppointmentList> {
        return safeApiCall {
            api.getAppointment(
                filter = query?.toFilter(),
                sort = query?.sort,
                page = query?.page,
                perPage = query?.perPage
            ).toDomain()
        }
    }

    override suspend fun postAppointment(
        data: AppointmentCreate
    ): NetworkResult<Appointment> {
        return safeApiCall {
            api.postAppointment(data.toRequest()).toDomain()
        }
    }

    override suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): NetworkResult<Appointment> {
        return safeApiCall {
            api.cancelAppointment(id, data.toRequest()).toDomain()
        }
    }

    override suspend fun getSlots(
        doctorId: String,
        date: String
    ): NetworkResult<Slots> {
        return safeApiCall {
            api.getSlots(doctorId, date).toDomain()
        }
    }
}