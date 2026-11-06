package com.example.netlib.domain.repository

import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.petModel.Pet
import com.example.netlib.domain.model.petModel.PetData
import com.example.netlib.domain.model.petModel.PetList


interface PetRepository {
    suspend fun getPet(filter: String? = null): NetworkResult<PetList>

    suspend fun postPet(
        data: PetData,
        photo: UploadFile? = null
    ): NetworkResult<Pet>

    suspend fun patchPet(
        idPet: String,
        data: PetData,
        photo: UploadFile? = null
    ): NetworkResult<Pet>
    suspend fun deletePet(idPet: String): NetworkResult< Unit>
}