package com.ssafy.cheket.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class AuthLogoutReason {
    TOKEN_EXPIRED,
    REFRESH_FAILED,
    UNAUTHORIZED,
    USER_LOGOUT,
    ACCOUNT_DELETED
}

sealed class AuthEvent {
    data class ForceLogout(val reason: AuthLogoutReason) : AuthEvent()
}

object AuthEventBus {
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun sendEvent(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
