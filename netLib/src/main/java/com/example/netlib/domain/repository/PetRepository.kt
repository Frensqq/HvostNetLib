package com.example.netlib.domain.repository

import com.example.netlib.domain.model.FileUpload
import com.example.netlib.domain.model.NetworkResult
import com.example.netlib.domain.model.petModel.Pet
import com.example.netlib.domain.model.petModel.PetData
import com.example.netlib.domain.model.petModel.PetList
import com.example.netlib.domain.model.query.PetQuery


interface PetRepository {
    suspend fun getPet(
        query: PetQuery?
    ): NetworkResult<PetList>

    suspend fun postPet(
        data: PetData,
        photo: FileUpload? = null
    ): NetworkResult<Pet>


    suspend fun patchPet(
        idPet: String,
        data: PetData,
        photo: FileUpload? = null
    ): NetworkResult<Pet>
    suspend fun deletePet(idPet: String): NetworkResult< Unit>
}