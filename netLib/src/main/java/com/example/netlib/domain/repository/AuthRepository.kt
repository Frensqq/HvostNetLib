package com.example.netlib.domain.repository

import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.Auth
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.model.authModel.Registration
import com.example.netlib.domain.model.authModel.User

interface AuthRepository {
    suspend fun registration(data: Registration): NetworkResult<User>
    suspend fun authorization(data: AuthData): NetworkResult<Auth>
    suspend fun refreshToken(): NetworkResult<Auth>

    fun saveToken(
        token: String? = null
    )
    fun getToken() : String?
}