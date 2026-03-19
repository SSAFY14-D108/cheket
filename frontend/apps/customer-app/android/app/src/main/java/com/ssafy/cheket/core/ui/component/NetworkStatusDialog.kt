package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.network.observeNetworkConnectivity
import com.ssafy.cheket.ui.theme.*

/**
 * 앱 전체에서 네트워크 상태를 감시하고,
 * 연결이 끊기면 다이얼로그를 띄운다.
 *
 * 사용법: AppNavGraph 등 최상위 Composable에서
 * ```
 * NetworkStatusObserver()
 * ```
 */
@Composable
fun NetworkStatusObserver() {
    val context = LocalContext.current
    val isConnected by context.observeNetworkConnectivity()
        .collectAsState(initial = true)

    if (!isConnected) {
        NetworkDisconnectedDialog()
    }
}

@Composable
private fun NetworkDisconnectedDialog() {
    AlertDialog(
        onDismissRequest = { /* 닫기 불가 — 네트워크 복구되면 자동 사라짐 */ },
        shape = RoundedCornerShape(20.dp),
        containerColor = CardBg,
        icon = {
            Icon(
                Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = Danger,
                modifier = Modifier.size(48.dp),
            )
        },
        title = {
            Text(
                "네트워크 연결 없음",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Text(
                "인터넷에 연결되어 있지 않습니다.\nWi-Fi 또는 모바일 데이터를 확인해 주세요.",
                fontSize = 14.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            // 확인 버튼 없음 — 네트워크 복구 시 자동 닫힘
            // 대신 안내 텍스트
            Text(
                "연결되면 자동으로 닫힙니다",
                fontSize = 12.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        },
    )
}
