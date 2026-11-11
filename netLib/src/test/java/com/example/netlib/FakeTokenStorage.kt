package com.example.netlib

import com.example.netlib.domain.storage.TokenStorage

class FakeTokenStorage : TokenStorage {
    private var token: String? = null

    override fun saveToken(token: String?) {
        this.token = token
    }

    override fun getToken(): String? = token
}