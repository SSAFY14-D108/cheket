package com.ssafy.cheket.features.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun SettingsScreen(
    onPasswordChange: () -> Unit,
    onBack: () -> Unit,
) {
    var allowNotifications by remember { mutableStateOf(true) }

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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
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
                            "마케팅/이벤트 등 푸시 알림 수신 여부를 설정합니다.",
                            fontSize = 12.sp,
                            color = MutedForeground,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = allowNotifications,
                        onCheckedChange = { allowNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = Primary,
                        ),
                    )
                }
            }

            // Password change
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
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
}
