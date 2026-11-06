package com.example.netlib.domain.repository

import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.ServiceList


interface CatalogRepository {
    suspend fun getServices(filter: String? = null): NetworkResult<ServiceList>

    suspend fun getClinics(filter: String? = null): NetworkResult<ClinicList>

    suspend fun getDoctors(filter: String? = null): NetworkResult<DoctorList>

}