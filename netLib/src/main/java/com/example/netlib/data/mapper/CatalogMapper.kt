package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.ClinicListResponse
import com.example.netlib.data.remote.DTO.ClinicResponse
import com.example.netlib.data.remote.DTO.DoctorExpandResponse
import com.example.netlib.data.remote.DTO.DoctorListResponse
import com.example.netlib.data.remote.DTO.DoctorResponse
import com.example.netlib.data.remote.DTO.ServiceListResponse
import com.example.netlib.data.remote.DTO.ServiceResponse
import com.example.netlib.domain.model.catalogModel.Clinic
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.Doctor
import com.example.netlib.domain.model.catalogModel.DoctorExpand
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.Service
import com.example.netlib.domain.model.catalogModel.ServiceList

internal fun ServiceResponse.toDomain(): Service =
    Service(
        id = id,
        code = code,
        name = name,
        price = price,
        durationMin = durationMin,
        sort = sort
    )


internal fun ServiceListResponse.toDomain(): ServiceList =
    ServiceList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items = items.map { it.toDomain() }

    )

internal fun ClinicResponse.toDomain(): Clinic =
    Clinic(id = id,
        name = name,
        address =  address,
        phone = phone,
        lat = lat,
        lng = lng,
        hours = hours,
        sort = sort)


internal fun ClinicListResponse.toDomain(): ClinicList =
    ClinicList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items = items.map { it.toDomain() }

    )

internal fun DoctorExpandResponse.toDomain() : DoctorExpand =
    DoctorExpand(
        clinic = clinic.toDomain()
    )

internal fun DoctorResponse.toDomain(): Doctor =
    Doctor(id = id,
        name = name,
        specialty = specialty,
        experienceYears = experienceYears,
        rating = rating,
        clinic = clinic,
        services = services,
        workDays = workDays,
        photo = photo,
        expand = expand?.toDomain())


internal fun DoctorListResponse.toDomain(): DoctorList =
    DoctorList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items = items.map { it.toDomain() }

    )