package com.ssafy.cheket.core.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FCM 토큰 갱신 및 푸시 수신 처리.
 *
 * - onNewToken: Firebase SDK가 토큰 갱신 시 호출 → 서버에 즉시 전송
 * - onMessageReceived: 포그라운드 푸시 수신 처리
 */
class CheketFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: ${token.take(10)}...")

        // 로그인 상태일 때만 서버에 전송
        val app = application as? CheketApplication ?: return
        val authDataStore = app.authDataStore
        if (!authDataStore.isLoggedIn()) {
            Log.d(TAG, "onNewToken — not logged in, skipping server registration")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userService = app.appContainer.userService
                userService.saveFcmToken(FcmTokenRequest(fcmToken = token))
                Log.d(TAG, "onNewToken — sent to server")
            } catch (e: Exception) {
                Log.w(TAG, "onNewToken — failed to send to server", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: from=${message.from}, title=${message.notification?.title}")
        // TODO: 포그라운드 알림 표시 (NotificationManager)
    }

    companion object {
        private const val TAG = "CheketFCM"
    }
}
