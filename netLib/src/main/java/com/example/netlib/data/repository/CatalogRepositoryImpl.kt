package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.catalogModel.ClinicList
import com.example.netlib.domain.model.catalogModel.DoctorList
import com.example.netlib.domain.model.catalogModel.ServiceList
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.CatalogRepository

class CatalogRepositoryImpl (
    private val api: PBApi,
    networkMonitor: Network
) : PBRepositoryBase(networkMonitor), CatalogRepository {

    override suspend fun getServices(
        filter: String?
    ): NetworkResult<ServiceList> {
        return safeApiCall {
            api.getServices(filter).toDomain()
        }
    }

    override suspend fun getClinics(
        filter: String?
    ): NetworkResult<ClinicList> {
        return safeApiCall {
            api.getClinics(filter).toDomain()
        }
    }

    override suspend fun getDoctors(
        filter: String?
    ): NetworkResult<DoctorList> {
        return safeApiCall {
            api.getDoctors(filter).toDomain()
        }
    }
}