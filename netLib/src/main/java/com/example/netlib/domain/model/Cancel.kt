package com.example.netlib.domain.model

import io.ktor.websocket.CloseReason

data class Cancel(
    val reason: String? = null
)