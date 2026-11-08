package com.example.netlib.data.storage

class TokenStorage {

    @Volatile
    private var token: String? = null

    fun saveToken(token: String?) {
        this.token = token
    }

    fun getToken(): String? {
        return token
    }
}