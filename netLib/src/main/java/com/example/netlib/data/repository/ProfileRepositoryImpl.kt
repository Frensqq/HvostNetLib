package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.data.remote.DTO.UserUpdateRequest
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.model.authModel.UserUpdateData
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
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