package com.example.netlib.data.repository

import com.example.netlib.data.mapper.toDomain
import com.example.netlib.data.mapper.toRequest
import com.example.netlib.data.remote.PBApi
import com.example.netlib.domain.model.FileUpload
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.petModel.Pet
import com.example.netlib.domain.model.petModel.PetData
import com.example.netlib.domain.model.petModel.PetList
import com.example.netlib.domain.model.query.PetQuery
import com.example.netlib.domain.network.Network
import com.example.netlib.domain.repository.PetRepository

class PetRepositoryImpl(
    private val api: PBApi,
    networkMonitor: Network
) : PBRepositoryBase(networkMonitor), PetRepository {

    override suspend fun getPet(
        query: PetQuery?
    ): NetworkResult<PetList> {
        return safeApiCall {
            api.getPet(
                page = query?.page,
                perPage = query?.perPage,
                sort = query?.sort
            ).toDomain()
        }
    }

    override suspend fun postPet(
        data: PetData,
        photo: FileUpload?
    ): NetworkResult<Pet> {
        return safeApiCall {
            api.postPet(data.toRequest(), photo?.toRequest()).toDomain()
        }
    }

    override suspend fun patchPet(
        idPet: String,
        data: PetData,
        photo: FileUpload?
    ): NetworkResult<Pet> {
        return safeApiCall {
            api.patchPet(idPet, data.toRequest(), photo?.toRequest()).toDomain()
        }
    }

    override suspend fun deletePet(
        idPet: String
    ): NetworkResult<Unit> {
        return safeApiCall {
            api.deletePet(idPet)
        }
    }
}