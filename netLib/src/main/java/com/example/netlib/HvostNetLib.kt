package com.example.netlib

import android.content.Context
import com.example.netlib.data.network.NetworkImpl
import com.example.netlib.data.remote.KtorRealtimeClient
import com.example.netlib.data.remote.KtorWsConnection
import com.example.netlib.data.remote.PBApi
import com.example.netlib.data.remote.PBApiService
import com.example.netlib.data.repository.PBRepositoryImpl
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
    private var token: String? = null

    private val clientFactory = PBApiService(
        baseUrl = baseUrl,
        getToken = { token }
    )

    private val httpClient = clientFactory.create()

    private val api = PBApi(httpClient)

    private val repository = PBRepositoryImpl(
        api = api,
        networkMonitor = NetworkImpl(context.applicationContext)
    )

    val authRepository: AuthRepository = repository

    val profileRepository: ProfileRepository = repository

    val catalogRepository: CatalogRepository = repository

    val petRepository: PetRepository = repository

    val appointmentsRepository: AppointmentsRepository = repository

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