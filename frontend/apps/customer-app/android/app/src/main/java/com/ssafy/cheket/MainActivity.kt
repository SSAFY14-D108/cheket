package com.ssafy.cheket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.cheket.core.network.dto.FcmTokenRequest
import com.ssafy.cheket.ui.theme.CheketTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    // Android 13+ 알림 권한 요청
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "POST_NOTIFICATIONS permission: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        enableEdgeToEdge()
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"))
        requestNotificationPermission()

        val app = application as CheketApplication
        val appContainer = app.appContainer
        val hasToken = app.authDataStore.getAccessToken() != null
        val isLoggingOut = app.authDataStore.isLoggingOut
        val isAlreadyLoggedIn = app.authDataStore.isLoggedIn()
        Log.d(TAG, "onCreate() — isLoggedIn=$isAlreadyLoggedIn, hasToken=$hasToken, isLoggingOut=$isLoggingOut")

        // 앱이 꺼져 있을 때 알림 탭으로 시작된 경우
        handleNotificationIntent(intent)

        // 로그인 상태면 FCM 토큰 동기화 (프로세스 죽어있을 때 토큰 교체 대비)
        if (isAlreadyLoggedIn) {
            syncFcmToken(app)
        }

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

    private fun syncFcmToken(app: CheketApplication) {
        lifecycleScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                app.appContainer.userService.saveFcmToken(FcmTokenRequest(fcmToken = fcmToken))
                Log.d(TAG, "syncFcmToken() sent: ${fcmToken.take(10)}...")
            } catch (e: Exception) {
                Log.w(TAG, "syncFcmToken() failed (non-critical)", e)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
