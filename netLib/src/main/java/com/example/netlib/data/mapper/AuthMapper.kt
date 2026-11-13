package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.AuthRequest
import com.example.netlib.data.remote.DTO.AuthResponse
import com.example.netlib.data.remote.DTO.RegistrationRequest
import com.example.netlib.data.remote.DTO.UserResponse
import com.example.netlib.data.remote.DTO.UserUpdateRequest
import com.example.netlib.domain.model.authModel.Auth
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.model.authModel.Registration
import com.example.netlib.domain.model.authModel.User
import com.example.netlib.domain.model.authModel.UserUpdateData

fun Registration.toRequest(): RegistrationRequest {
    return RegistrationRequest(
        email = email,
        name = name,
        password =  password,
        passwordConfirm = passwordConfirm,
        phone = phone
    )
}

fun UserResponse.toDomain(): User{
    return User(
       id =  id,
       email =  email,
       name =  name,
       phone =  phone,
       avatar =  avatar,
       verified =  verified,
       created =  created,
       updated =  updated
    )
}

fun AuthData.toRequest(): AuthRequest =
    AuthRequest(
        identity = identity,
        password = password
    )

fun AuthResponse.toDomain(): Auth =
    Auth(
        token = token,
        record = record.toDomain()
    )


fun UserUpdateData.toRequest(): UserUpdateRequest =
    UserUpdateRequest(
        name = name,
        phone = phone
    )