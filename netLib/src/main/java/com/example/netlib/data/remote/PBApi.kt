package com.example.netlib.data.remote

import com.example.netlib.domain.model.Appointment
import com.example.netlib.domain.model.AppointmentList
import com.example.netlib.domain.model.Cancel
import com.example.netlib.domain.model.ClinicList
import com.example.netlib.domain.model.DoctorList
import com.example.netlib.domain.model.PetList
import com.example.netlib.domain.model.RequestAppointment
import com.example.netlib.domain.model.RequestPet
import com.example.netlib.domain.model.ServiceList
import com.example.netlib.domain.model.Slots
import com.example.netlib.domain.model.User
import com.example.netlib.domain.model.system.AuthRequest
import com.example.netlib.domain.model.system.AuthResponse
import com.example.netlib.domain.model.system.Registration
import com.example.netlib.domain.model.system.UploadFile
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
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.collections.forEach

class PBApi(
    private val client: HttpClient
) {

    private val json = Json {
        encodeDefaults =  true
        explicitNulls = false
    }

    private suspend inline fun <reified T> get(
        path: String,
        filter: String? = null,
        expand: String? = null
    ) : T = client.get(path){
        filter?.let { parameter("filter", it) }
        expand?.let { parameter("expand", it) }

    }.body()

    private suspend inline fun <reified T> post(
        path: String,
        data: Any
    ) : T = client.post(path) {
        contentType(ContentType.Application.Json)
        setBody(data)
    }.body()

    private suspend inline fun <reified T> patch(
        path: String,
        data: Any
    ): T = client.patch(path) {
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


    private inline fun <reified D> fields(data: D): Map<String, String> {
        val objectData = json.encodeToJsonElement(data).jsonObject

        return objectData.mapNotNull { (key, value) ->
            if (value == JsonNull) {
                null
            } else {
                key to if (value is JsonPrimitive) value.content else value.toString()
            }
        }.toMap()
    }

    private suspend inline fun <reified T, reified D> multipart(
        path: String,
        data: D,
        files: Map<String, List<UploadFile>>,
        patch: Boolean = false
    ): T {

        val body = MultiPartFormDataContent(
            formData {
                fields(data).forEach { (k, v) -> append(k, v) }

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
        return if (patch)
            client.patch(path) { setBody(body) }.body()
        else
            client.post(path) { setBody(body) }.body()
    }

    suspend fun registration(data: Registration): User =
        post("collections/users/records", data)

    suspend fun authorization(data: AuthRequest): AuthResponse =
        post("collections/users/auth-with-password", data)

    suspend fun refreshToken(): AuthResponse =
        post("collections/users/auth-refresh", {})

    suspend fun getProfile(userId: String, filter: String? = null): User =
        get("collections/users/records/${userId}", filter)

    suspend fun patchProfile(userId: String, data: User): User =
        patch("collections/users/records/${userId}", data)


    suspend fun getServices(filter: String? = null): ServiceList =
        get("collections/services/records", filter)

    suspend fun getClinics(filter: String? = null): ClinicList =
        get("collections/clinics/records", filter)

    suspend fun getDoctors(filter: String? = null): DoctorList =
        get("collections/doctors/records",
            filter,
            "clinic"
        )


    suspend fun getPet(filter: String? = null): PetList =
        get("collections/pets/records", filter)

    suspend fun postPet(data: RequestPet): PetList =
        post("collections/pets/records", data)

    suspend fun patchPet(idPet: String, data: RequestPet): PetList =
        patch("collections/pets/records/${idPet}", data)

    suspend fun deletePet(idPet: String): Unit {
        delete("collections/pets/records/${idPet}")
    }

    suspend fun getSlots(doctorId: String, date: String): Slots =
        client.get("hvost/slots") {
            parameter("doctor", doctorId)
            parameter("date", date)
        }.body()

    suspend fun getAppointment(
        filter: String? = null,
    ): AppointmentList =
        get("collections/appointments/records",
            filter, "pet,doctor,service,clinic")

    suspend fun postAppointment(
        data: RequestAppointment
    ): Appointment =
        post("collections/appointments/records", data)


    suspend fun cancelAppointment(
        id: String,
        data: Cancel
    ): Appointment =
        post(
            "hvost/appointments/${id}/cancel",
            data
        )


}