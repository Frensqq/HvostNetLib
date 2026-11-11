package com.example.netlib.domain.repository

import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.ServiceList
import com.example.netlib.domain.model.query.ClinicQuery
import com.example.netlib.domain.model.query.DoctorQuery
import com.example.netlib.domain.model.query.ServiceQuery


interface CatalogRepository {
    suspend fun getServices(query: ServiceQuery? = null): NetworkResult<ServiceList>

    suspend fun getClinics(query: ClinicQuery? = null): NetworkResult<ClinicList>

    suspend fun getDoctors( query: DoctorQuery? = null): NetworkResult<DoctorList>

}