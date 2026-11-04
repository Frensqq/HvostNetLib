package com.example.netlib.data.webSoket

import kotlin.math.min

object ReconnectPolicy {
    private val STEPS_SEC = longArrayOf(1, 2, 4, 8, 16, 30)

    fun backoffMs(attempt: Int): Long {
        val idx = (attempt - 1).coerceIn(0, STEPS_SEC.lastIndex)
        return STEPS_SEC[idx] * 1000L
    }
}