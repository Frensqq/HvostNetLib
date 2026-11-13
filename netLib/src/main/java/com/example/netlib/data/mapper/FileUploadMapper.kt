package com.example.netlib.data.mapper

import com.example.netlib.data.remote.DTO.UploadFile
import com.example.netlib.domain.model.FileUpload

fun FileUpload.toRequest(): UploadFile {
    return UploadFile(
        bytes = bytes,
        name = name,
        mimeType = mimeType
    )
}