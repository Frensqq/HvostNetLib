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

fun ServiceResponse.toDomain(): Service =
    Service(id,
        code,
        name,
        price,
        duration,
        sort = sort)


fun ServiceListResponse.toDomain(): ServiceList =
    ServiceList(
        page,
        perPage,
        totalPages,
        totalItems,
        items = items.map { it.toDomain() }

    )

fun ClinicResponse.toDomain(): Clinic =
    Clinic(id,
        name,
        address,
        phone,
        lat,
        lng,
        hours,
        sort)


fun ClinicListResponse.toDomain(): ClinicList =
    ClinicList(
        page,
        perPage,
        totalPages,
        totalItems,
        items = items.map { it.toDomain() }

    )

fun DoctorExpandResponse.toDomain() : DoctorExpand =
    DoctorExpand(
        clinic = clinic.toDomain()
    )

fun DoctorResponse.toDomain(): Doctor =
    Doctor(id,
        name,
        specialty,
        experienceYears,
        rating,
        clinic,
        services,
        workDays,
        photo,
        expand = expand.toDomain())


fun DoctorListResponse.toDomain(): DoctorList =
    DoctorList(
        page,
        perPage,
        totalPages,
        totalItems,
        items = items.map { it.toDomain() }

    )