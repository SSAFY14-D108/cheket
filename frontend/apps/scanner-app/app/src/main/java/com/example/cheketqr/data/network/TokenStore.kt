package com.example.cheketqr.data.network

import android.content.Context
import android.content.SharedPreferences

/**
 * 호스트 JWT 토큰 저장소.
 * SharedPreferences에 accessToken/refreshToken 저장.
 */
object TokenStore {
    private const val PREF_NAME = "cheket_scanner_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var accessToken: String
        get() = prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String
        get() = prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    val isLoggedIn: Boolean
        get() = accessToken.isNotBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
