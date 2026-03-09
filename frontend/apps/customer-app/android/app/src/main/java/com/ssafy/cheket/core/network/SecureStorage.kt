package com.ssafy.cheket.core.network

interface SecureStorage {
    fun saveTokens(tokens: AuthTokens)
    fun getTokens(): AuthTokens?
    fun clearTokens()
}
