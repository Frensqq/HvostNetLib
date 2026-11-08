package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toFilter
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.ServiceList
import com.example.netlib.domain.model.query.ClinicQuery
import com.example.netlib.domain.model.query.DoctorQuery
import com.example.netlib.domain.model.query.ServiceQuery
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.CatalogRepository

class CatalogRepositoryImpl(
    private val api: PBApi,
    networkMonitor: Network
) : PBRepositoryBase(networkMonitor), CatalogRepository {

    override suspend fun getServices(
        query: ServiceQuery?
    ): NetworkResult<ServiceList> {
        return safeApiCall {
            api.getServices(
                sort = query?.sort
            ).toDomain()
        }
    }

    override suspend fun getClinics(
        query: ClinicQuery?
    ): NetworkResult<ClinicList> {
        return safeApiCall {
            api.getClinics(
                sort = query?.sort
            ).toDomain()
        }
    }

    override suspend fun getDoctors(
        query: DoctorQuery?
    ): NetworkResult<DoctorList> {
        return safeApiCall {
            api.getDoctors(
                filter = query?.toFilter(),
                sort = query?.sort
            ).toDomain()
        }
    }
}