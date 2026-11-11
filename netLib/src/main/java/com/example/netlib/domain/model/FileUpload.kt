package com.example.netlib.domain.model

data class FileUpload(
    val bytes: ByteArray,
    val name: String,
    val mimeType: String = "image/jpeg"
)