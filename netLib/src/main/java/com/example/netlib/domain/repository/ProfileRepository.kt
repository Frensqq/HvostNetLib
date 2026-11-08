package com.example.netlib.domain.repository

import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.model.authModel.UserUpdateData


interface ProfileRepository {
    suspend fun getProfile(userId: String): NetworkResult<User>
    suspend fun patchProfile(
        userId: String,
        data: UserUpdateData,
        avatar: UploadFile? = null
    ): NetworkResult< User>

}