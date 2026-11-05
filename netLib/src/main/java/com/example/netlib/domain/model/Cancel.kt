package com.example.netlib.domain.model

import io.ktor.websocket.CloseReason
import kotlinx.serialization.Serializable

@Serializable
data class Cancel(
    val reason: String? = null
)