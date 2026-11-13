package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.FileUpload
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.model.authModel.UserUpdateData
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val api: PBApi,
    networkMonitor: Network
) : PBRepositoryBase(networkMonitor), ProfileRepository {

    override suspend fun getProfile(
        userId: String
    ): NetworkResult<User> {
        return safeApiCall {
            api.getProfile(userId).toDomain()
        }
    }

    override suspend fun patchProfile(
        userId: String,
        data: UserUpdateData,
        avatar: FileUpload?
    ): NetworkResult<User> {
        return safeApiCall {
            api.patchProfile(
                userId = userId,
                data = data.toRequest(),
                avatar = avatar?.toRequest()
            ).toDomain()
        }
    }

}