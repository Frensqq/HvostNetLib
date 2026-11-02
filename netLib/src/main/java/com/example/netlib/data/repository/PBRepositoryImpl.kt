package com.example.netlib.data.repository

import com.example.netlib.data.remote.PBApi
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
import com.example.netlib.domain.model.system.ErrorResponse
import com.example.netlib.domain.model.system.NetworkResult
import com.example.netlib.domain.model.system.Registration
import com.example.netlib.domain.model.system.UploadFile
import com.example.netlib.domain.nework.Network
import com.example.netlib.domain.repository.PBRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.utils.io.errors.IOException

class PBRepositoryImpl(
   private val  api: PBApi,
    private val networkMonitor: Network
): PBRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        if (!networkMonitor.isConnected()) {
            return NetworkResult.NoInternet
        }

        return try {
            NetworkResult.Success(apiCall())
        } catch (e: IOException) {
            NetworkResult.NoInternet
        } catch (e: ResponseException) {
            NetworkResult.Error(
                errorResponse = ErrorResponse(
                    status = e.response.status.value,
                    message = e.message.toString(),
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(
                errorResponse = ErrorResponse(
                    status = -1,
                    message = e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun registration(data: Registration): NetworkResult<User> = safeApiCall {
        api.registration(data)
    }

    override suspend fun authorization(data: AuthRequest): NetworkResult<AuthResponse> =
        safeApiCall {
            api.authorization(data)
        }

    override suspend fun refreshToken(): NetworkResult<AuthResponse> = safeApiCall {
        api.refreshToken()
    }

    override suspend fun getProfile(userId: String): NetworkResult<User> = safeApiCall {
        api.getProfile(userId)
    }

    override suspend fun patchProfile(
        userId: String,
        data: User,
        avatar: UploadFile?
    ): NetworkResult<User> = safeApiCall {
        api.patchProfile(userId, data, avatar)
    }

    override suspend fun getServices(filter: String?): NetworkResult<ServiceList> = safeApiCall {
        api.getServices(filter)
    }

    override suspend fun getClinics(filter: String?): NetworkResult<ClinicList> = safeApiCall {
        api.getClinics(filter)
    }

    override suspend fun getDoctors(filter: String?): NetworkResult<DoctorList> = safeApiCall {
        api.getDoctors(filter)
    }

    override suspend fun getPet(filter: String?): NetworkResult<PetList> = safeApiCall {
        api.getPet(filter)
    }

    override suspend fun postPet(
        data: RequestPet,
        photo: UploadFile?
    ): NetworkResult<PetList> = safeApiCall {
        api.postPet(data, photo)
    }

    override suspend fun patchPet(
        idPet: String,
        data: RequestPet,
        photo: UploadFile?
    ): NetworkResult<Pet> = safeApiCall {
        api.patchPet(idPet, data, photo)
    }

    override suspend fun deletePet(idPet: String): NetworkResult<Unit> = safeApiCall {
        api.deletePet(idPet)
    }

    override suspend fun getSlots(
        doctorId: String,
        date: String
    ): NetworkResult<Slots> = safeApiCall {
        api.getSlots(
            doctorId,
            date
        )
    }

    override suspend fun getAppointment(filter: String?): NetworkResult<AppointmentList> =
        safeApiCall {
            api.getAppointment(filter)
        }

    override suspend fun postAppointment(data: RequestAppointment): NetworkResult<Appointment> =
        safeApiCall {
            api.postAppointment(data)
        }

    override suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): NetworkResult<Appointment> = safeApiCall {
        api.cancelAppointment(id, data)
    }

}