package com.example.netlib.data.storage

import android.content.Context
import com.example.netlib.domain.storage.TokenStorage

internal class TokenStorageImpl(context: Context): TokenStorage {

    private val preferences = context.applicationContext
        .getSharedPreferences("hvost_net_lib", Context.MODE_PRIVATE)

    override fun saveToken(token: String?) {
        preferences.edit().apply {
            if (token == null) {
                remove("auth_token")
            } else {
                putString("auth_token", token)
            }
            apply()
        }
    }

    override fun getToken(): String? {
        return preferences.getString("auth_token", null)
    }
}