package com.example.netlib.data.remote

import com.example.netlib.data.remote.DTO.AppointmentRequest
import com.example.netlib.data.remote.DTO.AuthRequest
import com.example.netlib.data.remote.DTO.CancelRequest
import com.example.netlib.data.remote.DTO.PetRequest
import com.example.netlib.data.remote.DTO.RegistrationRequest
import com.example.netlib.data.remote.DTO.UserUpdateRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.Assume.assumeTrue
import java.time.LocalDate
import java.time.ZoneId


class PBApiRealServerTest {

    companion object {
        private const val BASE_URL = "http://frens-project.tech:8090/api/"
        private const val EMAIL = "demo@hvost.local"
        private const val PASSWORD = "Demo2027!"
        private const val TEST_PET_NAME = "PBApiIntegrationPet"
    }

    private var token: String? = null
    private lateinit var httpClient: io.ktor.client.HttpClient
    private lateinit var api: PBApi

    @Before
    fun setUp() {
        val service = PBApiService(
            baseUrl = BASE_URL,
            getToken = { token }
        )
        httpClient = service.create()
        api = PBApi(httpClient)
    }

    @After
    fun tearDown() {
        runBlocking {
            if (::httpClient.isInitialized) {
                httpClient.close()
            }
        }
    }

    private suspend fun login() {
        val response = api.authorization(
            AuthRequest(
                identity = EMAIL,
                password = PASSWORD
            )
        )
        assertTrue(response.token.isNotBlank())
        token = response.token
    }

    @Test
    fun authorizationAndRefreshTokenWork() = runBlocking {
        val auth = api.authorization(
            AuthRequest(EMAIL, PASSWORD)
        )

        assertTrue(auth.token.isNotBlank())
        assertEquals(EMAIL, auth.record.email)

        token = auth.token

        val refreshed = api.refreshToken()
        assertTrue(refreshed.token.isNotBlank())
        assertEquals(auth.record.id, refreshed.record.id)

        // Все следующие запросы должны уже использовать обновлённый токен.
        token = refreshed.token
        val profile = api.getProfile(refreshed.record.id)
        assertEquals(EMAIL, profile.email)
    }

    @Test
    fun authorizationWithWrongPasswordReturns400() {
        val error = assertThrows(ClientRequestException::class.java) {
            runBlocking {
                api.authorization(
                    AuthRequest(
                        identity = EMAIL,
                        password = "wrong-password"
                    )
                )
            }
        }

        assertEquals(HttpStatusCode.BadRequest, error.response.status)
    }

    @Test
    fun registrationOfExistingDemoUserReturns400() {
        val error = assertThrows(ClientRequestException::class.java) {
            runBlocking {
                api.registration(
                    RegistrationRequest(
                        email = EMAIL,
                        password = PASSWORD,
                        passwordConfirm = PASSWORD,
                        name = "Тест",
                        phone = "+79001234567"
                    )
                )
            }
        }

        assertEquals(HttpStatusCode.BadRequest, error.response.status)
    }
    @Test
    fun patchProfileChangesOnlyName() = runBlocking {
        login()

        val auth = api.authorization(AuthRequest(EMAIL, PASSWORD))
        val before = api.getProfile(auth.record.id)

        val newName = before.name + "Test"

        try {
            api.patchProfile(
                userId = before.id,
                data = UserUpdateRequest(name = newName)
            )

            val after = api.getProfile(before.id)

            assertEquals(newName, after.name)
            assertEquals(before.phone, after.phone)
            assertEquals(before.email, after.email)
            assertEquals(before.avatar, after.avatar)
        } finally {
            api.patchProfile(
                userId = before.id,
                data = UserUpdateRequest(name = before.name)
            )
        }
    }
    @Test
    fun getAppointmentAfterAuthorization() = runBlocking {
        login()

        val auth = api.authorization(AuthRequest(EMAIL, PASSWORD))
        val before = api.getAppointment()
        val data = api.getAppointment("id='r6yzwye0hfm1oqk'")

        assertTrue(before.totalItems == 16)
        println(data.totalItems)
        assertTrue(data.totalItems == 1)



    }

    @Test
    fun getProfileAndPatchProfileWork() = runBlocking {
        login()

        val profile = api.getProfile(
            api.authorization(AuthRequest(EMAIL, PASSWORD)).record.id
        )

        assertEquals(EMAIL, profile.email)

        // Отправляем те же значения, чтобы не менять демонстрационный профиль.
        val updated = api.patchProfile(
            userId = profile.id,
            data = UserUpdateRequest(
                name = profile.name,
                phone = profile.phone
            )
        )

        assertEquals(profile.id, updated.id)
        assertEquals(profile.name, updated.name)
        assertEquals(profile.phone, updated.phone)
    }

    @Test
    fun catalogRequestsReturnData() = runBlocking {
        val services = api.getServices(sort = "sort")
        assertTrue(services.items.isNotEmpty())
        assertTrue(services.items.all { it.durationMin > 0 })

        val clinics = api.getClinics(sort = "sort")
        assertTrue(clinics.items.isNotEmpty())

        val doctors = api.getDoctors(sort = "-rating")
        assertTrue(doctors.items.isNotEmpty())

        val doctor = doctors.items.first()
        assertTrue(doctor.services.isNotEmpty())

        val byService = api.getDoctors(
            filter = "services~'${doctor.services.first()}'",
            sort = "-rating"
        )
        assertTrue(
            byService.items.any { doctor.services.first() in it.services }
        )

        val byClinic = api.getDoctors(
            filter = "clinic='${doctor.clinic}'"
        )
        assertTrue(byClinic.items.all { it.clinic == doctor.clinic })
    }

    @Test
    fun petListCreateUpdateAndDeleteWork() = runBlocking {
        login()

        val petsBefore = api.getPet(
            page = 1,
            perPage = 50,
            sort = "name"
        )
        assertTrue(petsBefore.page >= 1)

        val created = api.postPet(
            PetRequest(
                name = TEST_PET_NAME,
                species = "cat",
                breed = "Тестовая порода",
                birthDate = "2020-01-01 00:00:00.000Z",
                weightKg = 3.5
            )
        )

        try {
            assertEquals(TEST_PET_NAME, created.name)
            assertNotNull(created.id)

            val updated = api.patchPet(
                idPet = created.id,
                data = PetRequest(
                    name = "${TEST_PET_NAME}Updated",
                    species = "cat",
                    breed = "Тестовая порода",
                    birthDate = "2020-01-01 00:00:00.000Z",
                    weightKg = 3.7
                )
            )

            assertEquals("${TEST_PET_NAME}Updated", updated.name)

            val petsAfter = api.getPet(
                page = 1,
                perPage = 50,
                sort = "name"
            )
            assertTrue(petsAfter.items.any { it.id == created.id })
        } finally {
            api.deletePet(created.id)
        }

        val petsAfterDelete = api.getPet(
            page = 1,
            perPage = 50,
            sort = "name"
        )
        assertFalse(petsAfterDelete.items.any { it.id == created.id })
    }

    @Test
    fun slotsAppointmentListCreateAndCancelWork() = runBlocking {
        login()

        val pet = api.postPet(
            PetRequest(
                name = "${TEST_PET_NAME}Appointment",
                species = "cat",
                breed = "Тестовая порода",
                birthDate = "2020-01-01 00:00:00.000Z",
                weightKg = 3.5
            )
        )

        try {
            val services = api.getServices().items
            val doctors = api.getDoctors().items
            assertTrue(services.isNotEmpty())
            assertTrue(doctors.isNotEmpty())

            var selectedDoctorId: String? = null
            var selectedServiceId: String? = null
            var selectedStart: String? = null

            val moscowToday = LocalDate.now(ZoneId.of("Europe/Moscow"))

            search@ for (dayOffset in 2L..14L) {
                val date = moscowToday.plusDays(dayOffset)
                for (doctor in doctors) {
                    if (date.dayOfWeek.value !in doctor.workDays) continue

                    val serviceId = doctor.services.firstOrNull { doctorServiceId ->
                        services.any { it.id == doctorServiceId }
                    } ?: continue

                    val slots = api.getSlots(
                        doctorId = doctor.id,
                        date = date.toString()
                    )

                    assertEquals(doctor.id, slots.doctor)
                    if (!slots.working) continue

                    val freeSlot = slots.slots.firstOrNull { it.free } ?: continue

                    selectedDoctorId = doctor.id
                    selectedServiceId = serviceId
                    selectedStart = freeSlot.start
                    break@search
                }
            }

            // Если все ближайшие слоты заняты, этот сценарий будет отмечен как skipped.
            assumeTrue(
                "На ближайшие 14 дней нет подходящего свободного слота",
                selectedDoctorId != null &&
                    selectedServiceId != null &&
                    selectedStart != null
            )

            val createdAppointment = api.postAppointment(
                AppointmentRequest(
                    pet = pet.id,
                    doctor = selectedDoctorId!!,
                    service = selectedServiceId!!,
                    start = selectedStart!!,
                    comment = "PBApi integration test"
                )
            )

            assertEquals("pending", createdAppointment.status)

            val appointments = api.getAppointment(
                filter = "pet='${pet.id}'",
                sort = "-start",
                page = 1,
                perPage = 20
            )
            assertTrue(appointments.items.any { it.id == createdAppointment.id })

            val canceled = api.cancelAppointment(
                id = createdAppointment.id,
                data = CancelRequest(reason = "PBApi integration test cleanup")
            )
            assertEquals("cancelled", canceled.status)
        } finally {
            // Удаление питомца также удаляет его записи, если отмена не удалась.
            api.deletePet(pet.id)
        }
    }
}
