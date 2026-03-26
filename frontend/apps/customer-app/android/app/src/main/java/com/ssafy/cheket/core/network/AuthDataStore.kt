package com.ssafy.cheket.core.network

class AuthDataStore(private val secureStorage: SecureStorage) {

    @Volatile
    private var cachedUserId: Long? = null

    /** 로그아웃 플래그 — true이면 토큰 갱신 차단 */
    @Volatile
    var isLoggingOut: Boolean = false
        private set

    fun isLoggedIn(): Boolean = !isLoggingOut && secureStorage.getTokens() != null

    fun getAccessToken(): String? = secureStorage.getTokens()?.accessToken

    fun getTokenType(): String = secureStorage.getTokens()?.tokenType ?: "Bearer"

    fun getRefreshToken(): String? = secureStorage.getTokens()?.refreshToken

    fun saveTokens(tokens: AuthTokens) {
        if (isLoggingOut) return // 로그아웃 중에는 토큰 저장 차단
        secureStorage.saveTokens(tokens)
    }

    fun getUserId(): Long? {
        if (cachedUserId != null) return cachedUserId
        // 메모리에 없으면 영구 저장소에서 복원
        cachedUserId = (secureStorage as? EncryptedSharedPrefManager)?.getUserId()
        return cachedUserId
    }

    fun saveUserId(userId: Long) {
        cachedUserId = userId
        (secureStorage as? EncryptedSharedPrefManager)?.saveUserId(userId)
    }

    fun clear() {
        android.util.Log.d("AuthDataStore", "clear() called — before: isLoggedIn=${secureStorage.getTokens() != null}")
        isLoggingOut = true
        secureStorage.clearTokens()
        cachedUserId = null
        android.util.Log.d("AuthDataStore", "clear() done — after: isLoggedIn=${secureStorage.getTokens() != null}")
    }

    /** 로그인 성공 시 로그아웃 플래그 해제 */
    fun onLoginSuccess() {
        isLoggingOut = false
    }
}
