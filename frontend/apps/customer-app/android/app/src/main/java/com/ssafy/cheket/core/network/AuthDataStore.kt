package com.ssafy.cheket.core.network

class AuthDataStore(private val secureStorage: SecureStorage) {

    fun isLoggedIn(): Boolean = secureStorage.getTokens() != null

    fun getAccessToken(): String? = secureStorage.getTokens()?.accessToken

    fun getTokenType(): String = secureStorage.getTokens()?.tokenType ?: "Bearer"

    fun getRefreshToken(): String? = secureStorage.getTokens()?.refreshToken

    fun saveTokens(tokens: AuthTokens) {
        secureStorage.saveTokens(tokens)
    }

    fun clear() {
        secureStorage.clearTokens()
    }
}
