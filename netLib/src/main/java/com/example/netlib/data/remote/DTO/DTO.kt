package com.example.netlib.data.remote.DTO

import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorResponse(
    val status: Int = 0,
    val message: String = "",
    val data: Map<String, FieldError> = emptyMap()
)





@Serializable
internal data class FieldError(
    val code: String? = null,
    val message: String? = null
)


@Serializable
internal data class ListMeta(
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val totalItems: Int,
)


internal data class UploadFile(
    val bytes: ByteArray,
    val name: String,
    val mimeType: String = "image/jpeg"
)