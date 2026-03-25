package com.ssafy.cheket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ssafy.cheket.ui.theme.CheketTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    /**
     * FCM 딥링크: 알림 탭 시 Intent로 전달된 notification_type을 StateFlow로 관리.
     * AppNavGraph에서 이를 구독하여 해당 화면으로 네비게이션.
     */
    private val _pendingNotification = MutableStateFlow<NotificationDeepLink?>(null)
    val pendingNotification: StateFlow<NotificationDeepLink?> = _pendingNotification.asStateFlow()

    fun consumeNotification() {
        _pendingNotification.value = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        enableEdgeToEdge()
        val app = application as CheketApplication
        val appContainer = app.appContainer
        val isAlreadyLoggedIn = app.authDataStore.isLoggedIn()
        Log.d(TAG, "onCreate() — appContainer=${appContainer::class.simpleName}, isLoggedIn=$isAlreadyLoggedIn")

        // 앱이 꺼져 있을 때 알림 탭으로 시작된 경우
        handleNotificationIntent(intent)

        setContent {
            CheketTheme {
                AppNavGraph(
                    appContainer = appContainer,
                    startLoggedIn = isAlreadyLoggedIn,
                )
            }
        }
    }

    // 앱이 이미 실행 중일 때 알림 탭
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent()")
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val type = intent?.getStringExtra("notification_type") ?: return
        val notificationId = intent.getStringExtra("notification_id")
        val showId = intent.getStringExtra("show_id")
        Log.d(TAG, "handleNotificationIntent: type=$type, notificationId=$notificationId, showId=$showId")

        _pendingNotification.value = NotificationDeepLink(
            type = type,
            notificationId = notificationId?.toLongOrNull(),
            showId = showId?.toLongOrNull(),
        )

        // Intent extras 초기화 (중복 처리 방지)
        intent.removeExtra("notification_type")
        intent.removeExtra("notification_id")
        intent.removeExtra("show_id")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

data class NotificationDeepLink(
    val type: String,
    val notificationId: Long? = null,
    val showId: Long? = null,
)
