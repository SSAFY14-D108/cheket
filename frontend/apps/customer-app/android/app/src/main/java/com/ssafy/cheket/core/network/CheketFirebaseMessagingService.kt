package com.ssafy.cheket.core.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.MainActivity
import com.ssafy.cheket.R
import com.ssafy.cheket.core.network.dto.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FCM 토큰 갱신 및 푸시 수신 처리.
 *
 * - onNewToken: Firebase SDK가 토큰 갱신 시 호출 → 서버에 즉시 전송
 * - onMessageReceived: 포그라운드/백그라운드 푸시 수신 → 시스템 알림 표시
 *
 * 알림 탭 → MainActivity로 Intent 전달 → deep link로 해당 화면 이동
 */
class CheketFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: ${token.take(10)}...")

        val app = application as? CheketApplication ?: return
        val authDataStore = app.authDataStore
        if (!authDataStore.isLoggedIn()) {
            Log.d(TAG, "onNewToken — not logged in, skipping")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userService = app.appContainer.userService
                userService.saveFcmToken(FcmTokenRequest(fcmToken = token))
                Log.d(TAG, "onNewToken — sent to server")
            } catch (e: Exception) {
                Log.w(TAG, "onNewToken — failed to send", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = message.notification?.title ?: data["title"] ?: "체켓"
        val body = message.notification?.body ?: data["body"] ?: ""
        val type = data["type"] ?: ""
        val notificationId = data["notificationId"]
        val showId = data["showId"]

        Log.d(TAG, "onMessageReceived: type=$type, title=$title, showId=$showId")

        showSystemNotification(title, body, type, notificationId, showId)
    }

    private fun showSystemNotification(
        title: String,
        body: String,
        type: String,
        notificationId: String?,
        showId: String?,
    ) {
        val channelId = "cheket_notifications"
        val notifManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8+ 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "체켓 알림",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "공연 알림, 거래 알림, 계약 요청 등"
                enableVibration(true)
            }
            notifManager.createNotificationChannel(channel)
        }

        // 탭 시 MainActivity로 딥링크 전달
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            putExtra("notification_id", notificationId)
            putExtra("show_id", showId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (notificationId?.toLongOrNull() ?: System.currentTimeMillis()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notifManager.notify(
            (notificationId?.toLongOrNull() ?: System.currentTimeMillis()).toInt(),
            notification,
        )
    }

    companion object {
        private const val TAG = "CheketFCM"
    }
}
