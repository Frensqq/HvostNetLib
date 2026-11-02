package com.example.netlib.domain.repository

import com.example.netlib.domain.model.Appointment
import com.example.netlib.domain.model.AppointmentList
import com.example.netlib.domain.model.Cancel
import com.example.netlib.domain.model.ClinicList
import com.example.netlib.domain.model.DoctorList
import com.example.netlib.domain.model.Pet
import com.example.netlib.domain.model.PetList
import com.example.netlib.domain.model.RequestAppointment
import com.example.netlib.domain.model.RequestPet
import com.example.netlib.domain.model.ServiceList
import com.example.netlib.domain.model.Slots
import com.example.netlib.domain.model.User
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.system.AuthResponse
import com.example.netlib.domain.model.system.NetworkResult
import com.example.netlib.domain.model.system.Registration
import com.example.netlib.domain.model.system.UploadFile

interface PBRepository {


    suspend fun registration(data: Registration): NetworkResult< User>
    suspend fun authorization(data: AuthRequest): NetworkResult< AuthResponse>

    suspend fun refreshToken(): NetworkResult< AuthResponse>

    suspend fun getProfile(userId: String): NetworkResult< User>
    suspend fun patchProfile(
        userId: String,
        data: User,
        avatar: UploadFile? = null
    ): NetworkResult< User>

    suspend fun getServices(filter: String? = null): NetworkResult<ServiceList>

    suspend fun getClinics(filter: String? = null): NetworkResult< ClinicList>

    suspend fun getDoctors(filter: String? = null): NetworkResult< DoctorList>



    suspend fun getPet(filter: String? = null): NetworkResult< PetList>

    suspend fun postPet(
        data: RequestPet,
        photo: UploadFile? = null
    ): NetworkResult< PetList>

    suspend fun patchPet(
        idPet: String,
        data: RequestPet,
        photo: UploadFile? = null
    ): NetworkResult< Pet>
    suspend fun deletePet(idPet: String): NetworkResult< Unit>

    suspend fun getSlots(doctorId: String, date: String): NetworkResult<Slots>

    suspend fun getAppointment(
        filter: String? = null,
    ): NetworkResult<AppointmentList>

    suspend fun postAppointment(
        data: RequestAppointment
    ): NetworkResult<Appointment>

    suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): NetworkResult< Appointment>


    fun saveToken(
        token: String? = null
    )
     fun getToken() : String?
}