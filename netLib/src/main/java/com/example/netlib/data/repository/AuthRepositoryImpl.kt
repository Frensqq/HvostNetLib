package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.Auth
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.model.authModel.Registration
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.AuthRepository
import com.example.netlib.domain.storage.TokenStorage

class AuthRepositoryImpl(
    private val api: PBApi,
    networkMonitor: Network,
    private val tokenStorageImpl: TokenStorage
) : PBRepositoryBase(networkMonitor), AuthRepository {

    override suspend fun registration(
        data: Registration
    ): NetworkResult<User> {
        return safeApiCall {
            api.registration(data.toRequest()).toDomain()
        }
    }

    override suspend fun authorization(
        data: AuthData
    ): NetworkResult<Auth> {
        return safeApiCall {
            val response = api.authorization(data.toRequest())

            tokenStorageImpl.saveToken(response.token)

            response.toDomain()
        }
    }

    override suspend fun refreshToken(): NetworkResult<Auth> {
        return safeApiCall {
            val response = api.refreshToken()

            tokenStorageImpl.saveToken(response.token)

            response.toDomain()
        }
    }

    override fun saveToken(token: String?) {
        tokenStorageImpl.saveToken(token)
    }

    override fun getToken(): String? {
        return tokenStorageImpl.getToken()
    }
}