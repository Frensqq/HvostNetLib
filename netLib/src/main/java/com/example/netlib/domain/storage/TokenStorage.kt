package com.example.netlib.domain.storage

interface TokenStorage {
    fun saveToken(token: String?)
    fun getToken(): String?
}