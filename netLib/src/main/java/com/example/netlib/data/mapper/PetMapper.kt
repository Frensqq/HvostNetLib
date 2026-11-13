package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.PetListResponse
import com.example.netlib.data.remote.DTO.PetRequest
import com.example.netlib.data.remote.DTO.PetResponse
import com.example.netlib.domain.model.petModel.Pet
import com.example.netlib.domain.model.petModel.PetData
import com.example.netlib.domain.model.petModel.PetList

internal fun PetResponse.toDomain(): Pet {
    return Pet(
        id = id,
        owner = owner,
        name = name,
        species = species,
        breed = breed,
        birthDate = birthDate,
        weightKg = weightKg,
        photo = photo,
        created = created,
        updated = updated
    )
}

internal fun PetListResponse.toDomain(): PetList {
    return PetList(
        page = page,
        perPage = perPage,
        totalPages = totalPages,
        totalItems = totalItems,
        items = items.map { it.toDomain() }
    )
}

internal fun PetData.toRequest(): PetRequest {
    return PetRequest(
        name = name,
        species = species,
        breed = breed,
        birthDate = birthDate,
        weightKg = weightKg
    )
}