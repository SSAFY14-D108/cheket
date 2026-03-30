package com.ssafy.cheket.features.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.NotificationDto
import com.ssafy.cheket.core.network.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class NotificationViewModel(
    private val userService: UserService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = userService.getNotifications()
                val notifications = response.data ?: emptyList()
                Log.d(TAG, "loadNotifications() got ${notifications.size} items")
                _uiState.value = _uiState.value.copy(
                    notifications = notifications,
                    isLoading = false,
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadNotifications() failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알림을 불러오지 못했습니다.",
                )
            }
        }
    }

    fun markAsRead(notificationId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                userService.markNotificationRead(notificationId)
                Log.d(TAG, "markAsRead($notificationId) success")
                // 로컬 상태 즉시 업데이트
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                )
                onDone()
            } catch (e: Exception) {
                Log.w(TAG, "markAsRead($notificationId) failed", e)
                // 실패해도 네비게이션은 진행
                onDone()
            }
        }
    }

    companion object {
        private const val TAG = "NotificationVM"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CheketApplication
                NotificationViewModel(app.appContainer.userService)
            }
        }
    }
}
