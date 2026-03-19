package com.ssafy.cheket.features.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.network.dto.NotificationRequest
import com.ssafy.cheket.core.network.service.UserService
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.elevatedSurface
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

private const val TAG = "SettingsScreen"

@Composable
fun SettingsScreen(
    userService: UserService,
    onPasswordChange: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allowNotifications by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppHeader(title = "설정", onBack = onBack) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Push notification toggle
            Row(
                Modifier
                    .fillMaxWidth()
                    .elevatedSurface()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "푸시 알림 받기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "공연 당일/전날 예약한 공연의 알림을 받습니다.",
                        fontSize = 12.sp,
                        color = MutedForeground,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = allowNotifications,
                    onCheckedChange = { newValue ->
                        if (isUpdating) return@Switch
                        val previousValue = allowNotifications
                        allowNotifications = newValue
                        isUpdating = true
                        scope.launch {
                            try {
                                val response = userService.updateNotification(
                                    NotificationRequest(notificationEnable = newValue)
                                )
                                Log.d(TAG, "updateNotification($newValue) statusCode=${response.httpStatusCode}")
                                isUpdating = false
                                if (response.httpStatusCode !in 200..299) {
                                    allowNotifications = previousValue
                                    Toast.makeText(context, "알림 설정 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "updateNotification() error", e)
                                isUpdating = false
                                allowNotifications = previousValue
                                Toast.makeText(context, "알림 설정 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isUpdating,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = White,
                        checkedTrackColor = Primary,
                    ),
                )
            }

            // Password change
            Row(
                Modifier
                    .fillMaxWidth()
                    .elevatedSurface()
                    .clickable(onClick = onPasswordChange)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Lock, null, tint = MutedForeground, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("비밀번호 변경", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnBackground, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, null, tint = SubText, modifier = Modifier.size(20.dp))
            }
        }
    }
}
