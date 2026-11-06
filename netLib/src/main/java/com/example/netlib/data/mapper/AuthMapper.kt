package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.AppointmentListResponse
import com.example.netlib.data.remote.DTO.AppointmentRequest
import com.example.netlib.data.remote.DTO.AppointmentResponse
import com.example.netlib.data.remote.DTO.AuthRequest
import com.example.netlib.data.remote.DTO.AuthResponse
import com.example.netlib.data.remote.DTO.CancelRequest
import com.example.netlib.data.remote.DTO.RegistrationRequest
import com.example.netlib.data.remote.DTO.UserResponse
import com.example.netlib.domain.model.appointmentsModel.Appointment
import com.example.netlib.domain.model.appointmentsModel.AppointmentCreate
import com.example.netlib.domain.model.appointmentsModel.AppointmentList
import com.example.netlib.domain.model.appointmentsModel.Cancel
import com.example.netlib.domain.model.authModel.Auth
import com.example.netlib.domain.model.authModel.AuthData
import com.example.netlib.domain.model.authModel.Registration
import com.example.netlib.domain.model.authModel.User

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
        id,
        email,
        name,
        phone,
        avatar,
        verified,
        created,
        updated
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