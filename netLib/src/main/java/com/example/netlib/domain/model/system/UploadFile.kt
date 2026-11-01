package com.example.netlib.domain.model.system

data class UploadFile(
    val bytes: ByteArray,
    val name: String,
    val mimeType: String
)
