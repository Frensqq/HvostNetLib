package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.DTO.ErrorResponse
import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.appointmentsModel.Appointment
import com.example.netlib.domain.model.appointmentsModel.AppointmentCreate
import com.example.netlib.domain.model.appointmentsModel.AppointmentList
import com.example.netlib.domain.model.appointmentsModel.Cancel
import com.example.netlib.domain.model.appointmentsModel.Slots
import com.example.netlib.domain.model.authModel.Auth
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.model.authModel.Registration
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.ServiceList
import com.example.netlib.domain.model.petModel.Pet
import com.example.netlib.domain.model.petModel.PetData
import com.example.netlib.domain.model.petModel.PetList
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.AppointmentsRepository
import com.example.netlib.domain.repository.AuthRepository
import com.example.netlib.domain.repository.CatalogRepository
import com.example.netlib.domain.repository.PetRepository
import com.example.netlib.domain.repository.ProfileRepository
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json

class PBRepositoryImpl(
   private val  api: PBApi,
    private val networkMonitor: Network
): AppointmentsRepository, AuthRepository, CatalogRepository, PetRepository, ProfileRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        if (!networkMonitor.isConnected()) {
            return NetworkResult.NoInternet
        }

        return try {
            NetworkResult.Success(apiCall())
        } catch (e: HttpRequestTimeoutException) {
            NetworkResult.Timeout
        } catch (e: IOException) {
            NetworkResult.NoInternet
        } catch (e: ResponseException) {
            val text = runCatching { e.response.bodyAsText() }.getOrDefault("")
            val error = runCatching {
                Json { ignoreUnknownKeys = true }.decodeFromString<ErrorResponse>(text)
            }.getOrElse {
                ErrorResponse(status = e.response.status.value, message = e.message.orEmpty())
            }
            NetworkResult.Error(error)
        } catch (e: Exception) {
            NetworkResult.Error(
                ErrorResponse(status = -1, message = e.message ?: "Unknown error")
            )
        }
    }

    override suspend fun getAppointment(filter: String?): NetworkResult<AppointmentList> {
        return safeApiCall {  api.getAppointment(filter).toDomain()}
    }

    override suspend fun postAppointment(data: AppointmentCreate): NetworkResult<Appointment> {
        return  safeApiCall { api.postAppointment(data.toRequest()).toDomain() }
    }

    override suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): NetworkResult<Appointment> {
        return safeApiCall { api.cancelAppointment(id, data.toRequest()).toDomain() }
    }

    override suspend fun getSlots(
        doctorId: String,
        date: String
    ): NetworkResult<Slots> {
        return safeApiCall {  api.getSlots(doctorId,date).toDomain()}
    }

    override suspend fun registration(data: Registration): NetworkResult<User> {
        return safeApiCall { api.registration(data.toRequest()).toDomain() }
    }

    override suspend fun authorization(data: AuthData): NetworkResult<Auth> {
        return safeApiCall { api.authorization(data.toRequest()).toDomain()}
    }

    override suspend fun refreshToken(): NetworkResult<Auth> {
        return  safeApiCall { api.refreshToken().toDomain() }
    }

    override fun saveToken(token: String?) {
        TODO("Not yet implemented")
    }

    override fun getToken(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun getServices(filter: String?): NetworkResult<ServiceList> {
        return safeApiCall { api.getServices(filter).toDomain() }
    }

    override suspend fun getClinics(filter: String?): NetworkResult<ClinicList> {
        return safeApiCall { api.getClinics(filter).toDomain() }
    }

    override suspend fun getDoctors(filter: String?): NetworkResult<DoctorList> {
        return  safeApiCall { api.getDoctors(filter).toDomain() }
    }

    override suspend fun getPet(filter: String?): NetworkResult<PetList> {
      return safeApiCall {   api.getPet(filter).toDomain() }
    }

    override suspend fun postPet(
        data: PetData,
        photo: UploadFile?
    ): NetworkResult<Pet> {
        return safeApiCall { api.postPet(data.toRequest(), photo).toDomain() }
    }

    override suspend fun patchPet(
        idPet: String,
        data: PetData,
        photo: UploadFile?
    ): NetworkResult<Pet> {
        return safeApiCall { api.patchPet(
            idPet,
            data.toRequest(),
            photo
        ).toDomain() }
    }

    override suspend fun deletePet(idPet: String): NetworkResult<Unit> {
        return safeApiCall { api.deletePet(idPet) }
    }

    override suspend fun getProfile(userId: String): NetworkResult<User> {
        return safeApiCall { api.getProfile(userId = userId).toDomain() }
    }

    override suspend fun patchProfile(
        userId: String,
        data: User,
        avatar: UploadFile?
    ): NetworkResult<User> {
        return safeApiCall {
            api.patchProfile(
                userId = userId,
                data = data.toRequest(),
                avatar = avatar
            ).toDomain()
        }
    }
}