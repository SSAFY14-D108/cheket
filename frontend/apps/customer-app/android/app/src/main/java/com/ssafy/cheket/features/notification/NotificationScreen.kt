package com.ssafy.cheket.features.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.network.dto.NotificationDto
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

/**
 * 알림 타입별 아이콘 + 색상 매핑
 */
private fun notificationIcon(type: String): Pair<ImageVector, Color> = when (type) {
    "SHOW_START" -> Icons.Outlined.Event to Color(0xFF3B82F6)           // 파랑
    "SETTLEMENT" -> Icons.Outlined.AccountBalanceWallet to Primary
    "APPROVED" -> Icons.Outlined.CheckCircle to Primary
    "REJECTED" -> Icons.Outlined.Cancel to Color(0xFFEF4444)             // 빨강
    "RESALE" -> Icons.Outlined.Sell to Color(0xFFF59E0B)                 // 노랑
    "RQ_CREATE" -> Icons.Outlined.Description to Color(0xFF8B5CF6)       // 보라
    "RQ_UPDATE" -> Icons.Outlined.EditNote to Color(0xFF8B5CF6)          // 보라
    else -> Icons.Outlined.Notifications to MutedForeground
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNotificationClick: (NotificationDto) -> Unit,
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) isRefreshing = false
    }

    Scaffold(
        topBar = { AppHeader(title = "알림", onBack = onBack) },
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.notifications.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            uiState.error != null && uiState.notifications.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = "오류가 발생했습니다",
                        description = uiState.error ?: "",
                    )
                }
            }
            uiState.notifications.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = "아직 알림이 없습니다",
                        description = "새로운 알림이 도착하면 이곳에서 바로 확인할 수 있어요.",
                    )
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        viewModel.loadNotifications()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                        .padding(innerPadding),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    viewModel.markAsRead(notification.id) {
                                        onNotificationClick(notification)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit,
) {
    val (icon, iconColor) = notificationIcon(notification.type)
    val bgColor = if (notification.isRead) Color.Transparent else PrimaryLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }

        // 메시지
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.message,
                fontSize = 14.sp,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                color = OnBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = notificationTypeLabel(notification.type),
                fontSize = 12.sp,
                color = MutedForeground,
            )
        }

        // 읽지 않음 표시
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary),
            )
        }
    }
    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
}

private fun notificationTypeLabel(type: String): String = when (type) {
    "SHOW_START" -> "공연 알림"
    "SETTLEMENT" -> "정산 완료"
    "APPROVED" -> "계약 확인"
    "REJECTED" -> "계약 거절"
    "RESALE" -> "리세일 알림"
    "RQ_CREATE" -> "공연 등록 확인 요청"
    "RQ_UPDATE" -> "공연 수정 확인 요청"
    else -> "알림"
}
