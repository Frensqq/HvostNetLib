package com.example.netlib

import android.content.Context
import com.example.netlib.data.network.NetworkImpl
import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.KtorWsConnection
import com.example.netlib.data.remote.PBApi
import com.example.netlib.data.remote.PBApiService
import com.example.netlib.data.repository.AppointmentRepositoryImpl
import com.example.netlib.data.repository.AuthRepositoryImpl
import com.example.netlib.data.repository.CatalogRepositoryImpl
import com.example.netlib.data.repository.PetRepositoryImpl
import com.example.netlib.data.repository.ProfileRepositoryImpl
import com.example.netlib.data.storage.TokenStorage
import com.example.netlib.domain.repository.AppointmentsRepository
import com.example.netlib.domain.repository.AuthRepository
import com.example.netlib.domain.repository.CatalogRepository
import com.example.netlib.domain.repository.PetRepository
import com.example.netlib.domain.repository.ProfileRepository
import com.example.netlib.domain.websocket.HvostRealtimeClient

class HvostNetLib(
    context: Context,
    baseUrl: String,
    wsUrl: String
) {
    private val tokenStorage = TokenStorage()

    private val clientFactory = PBApiService(
        baseUrl = baseUrl,
        getToken = { tokenStorage.getToken() }
    )

    private val httpClient = clientFactory.create()

    private val api = PBApi(httpClient)

    private val networkMonitor = NetworkImpl(context.applicationContext)

    val authRepository: AuthRepository =
        AuthRepositoryImpl(api, networkMonitor, tokenStorage)

    val profileRepository: ProfileRepository =
        ProfileRepositoryImpl(api, networkMonitor)

    val catalogRepository: CatalogRepository =
        CatalogRepositoryImpl(api, networkMonitor)

    val petRepository: PetRepository =
        PetRepositoryImpl(api, networkMonitor)

    val appointmentsRepository: AppointmentsRepository =
        AppointmentRepositoryImpl(api, networkMonitor)

    val realtimeClient: HvostRealtimeClient = KtorRealtimeClient(
        connection = KtorWsConnection(
            httpClient = httpClient,
            wsUrl = wsUrl
        ),
        json = clientFactory.json
    )

    suspend fun close() {
        realtimeClient.disconnect()
        httpClient.close()
    }
}