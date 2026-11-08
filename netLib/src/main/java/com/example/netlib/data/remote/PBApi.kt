package com.example.netlib.data.remote

import com.example.netlib.data.remote.DTO.AppointmentListResponse
import com.example.netlib.data.remote.DTO.AppointmentRequest
import com.example.netlib.data.remote.DTO.AppointmentResponse
import com.example.netlib.data.remote.DTO.AuthRequest
import com.example.netlib.data.remote.DTO.AuthResponse
import com.example.netlib.data.remote.DTO.CancelRequest
import com.example.netlib.data.remote.DTO.ClinicListResponse
import com.example.netlib.data.remote.DTO.DoctorListResponse
import com.example.netlib.data.remote.DTO.PetListResponse
import com.example.netlib.data.remote.DTO.PetRequest
import com.example.netlib.data.remote.DTO.PetResponse
import com.example.netlib.data.remote.DTO.RegistrationRequest
import com.example.netlib.data.remote.DTO.ServiceListResponse
import com.example.netlib.data.remote.DTO.SlotListResponse
import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.data.remote.DTO.UserResponse
import com.example.netlib.data.remote.DTO.UserUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.collections.forEach

class PBApi(
    private val client: HttpClient,
) {

    private val json = Json {
        encodeDefaults =  true
        explicitNulls = false
    }

    private suspend inline fun <reified T> get(
        path: String,
        filter: String? = null,
        expand: String? = null,
        sort: String? = null,
        page: Int? = null,
        perPage: Int? = null
    ): T = client.get(path) {
        filter?.let { parameter("filter", it) }
        expand?.let { parameter("expand", it) }
        sort?.let { parameter("sort", it) }
        page?.let { parameter("page", it) }
        perPage?.let { parameter("perPage", it) }
    }.body()

    private suspend inline fun <reified T> post(
        path: String,
        data: Any
    ) : T = client.post(path) {
        contentType(ContentType.Application.Json)
        setBody(data)
    }.body()

    private suspend fun delete(path: String){
        client.delete(path)
    }


    private fun files(
        vararg values: Pair<String, UploadFile?>
    ) = values
        .filter { it.second != null }
        .associate { it.first to listOf(it.second!!) }


    private inline fun <reified D> fields(data: D): Map<String, String> =
        json.encodeToJsonElement(data)
            .jsonObject
            .mapNotNull { (key, value) ->
                if (value == JsonNull) null
                else key to value.let {
                    if (it is JsonPrimitive) it.content else it.toString()
                }
            }
            .toMap()

    private inline fun <reified D> multipartBody(
        data: D,
        files: Map<String, List<UploadFile>>
    ) = MultiPartFormDataContent(
        formData {
            fields(data).forEach { (key, value) ->
                append(key, value)
            }

            files.forEach { (field, list) ->
                list.forEach { file ->
                    append(
                        field,
                        file.bytes,
                        Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${file.name}\""
                            )
                            append(HttpHeaders.ContentType, file.mimeType)
                        }
                    )
                }
            }
        }
    )

    private suspend inline fun <reified T, reified D> postMultipart(
        path: String,
        data: D,
        files: Map<String, List<UploadFile>>
    ): T = client.post(path) {
        setBody(multipartBody(data, files))
    }.body()

    private suspend inline fun <reified T, reified D> patchMultipart(
        path: String,
        data: D,
        files: Map<String, List<UploadFile>>
    ): T = client.patch(path) {
        setBody(multipartBody(data, files))
    }.body()

    suspend fun registration(data: RegistrationRequest): UserResponse =
        post("collections/users/records", data)

    suspend fun authorization(data: AuthRequest): AuthResponse =
        post("collections/users/auth-with-password", data)

    suspend fun refreshToken(): AuthResponse =
        post("collections/users/auth-refresh", buildJsonObject{})

    suspend fun getProfile(userId: String): UserResponse =
        get("collections/users/records/${userId}")



    suspend fun patchProfile(
        userId: String,
        data: UserUpdateRequest,
        avatar: UploadFile? = null
    ): UserResponse =
        patchMultipart(
            path = "collections/users/records/${userId}",
            data = data,
            files = files("avatar" to avatar),
        )


    suspend fun getServices(sort: String? = null): ServiceListResponse =
        get(
            path = "collections/services/records",
            sort = sort
        )

    suspend fun getClinics(sort: String? = null): ClinicListResponse =
        get(
            path = "collections/clinics/records",
            sort = sort
        )

    suspend fun getDoctors(
        filter: String? = null,
        sort: String? = null
    ): DoctorListResponse =
        get(
            path = "collections/doctors/records",
            filter = filter,
            expand = "clinic",
            sort = sort
        )


    suspend fun getPet(
        page: Int? = null,
        perPage: Int? = null,
        sort: String? = null
    ): PetListResponse =
        get(
            path = "collections/pets/records",
            page = page,
            perPage = perPage,
            sort = sort
        )

    suspend fun postPet(
        data: PetRequest,
        photo: UploadFile? = null
    ): PetResponse =
        postMultipart(
            path = "collections/pets/records",
            data = data,
            files = files("photo" to photo),
        )

    suspend fun patchPet(
        idPet: String,
        data: PetRequest,
        photo: UploadFile? = null
    ): PetResponse = patchMultipart(
        path = "collections/pets/records/${idPet}",
        data = data,
        files = files("photo" to photo)
    )

    suspend fun deletePet(idPet: String): Unit {
        delete("collections/pets/records/${idPet}")
    }

    suspend fun getSlots(doctorId: String, date: String): SlotListResponse =
        client.get("hvost/slots") {
            parameter("doctor", doctorId)
            parameter("date", date)
        }.body()

    suspend fun getAppointment(
        filter: String? = null,
        sort: String? = null,
        page: Int? = null,
        perPage: Int? = null
    ): AppointmentListResponse =
        get(
            path = "collections/appointments/records",
            filter = filter,
            expand = "pet,doctor,service,clinic",
            sort = sort,
            page = page,
            perPage = perPage
        )

    suspend fun postAppointment(
        data: AppointmentRequest
    ): AppointmentResponse =
        post("collections/appointments/records", data)


    suspend fun cancelAppointment(
        id: String,
        data: CancelRequest
    ): AppointmentResponse =
        post(
            "hvost/appointments/${id}/cancel",
            data
        )
}