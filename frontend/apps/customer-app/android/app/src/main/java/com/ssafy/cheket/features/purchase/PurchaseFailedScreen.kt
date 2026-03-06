package com.ssafy.cheket.features.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

private data class FailureConfig(
    val title: String,
    val description: String,
    val errorCode: String,
)

private fun getFailureConfig(reason: String): FailureConfig {
    return when (reason) {
        "SOLD_OUT" -> FailureConfig(
            title = "좌석이 이미 판매되었습니다",
            description = "선택하신 좌석이 다른 사용자에 의해 이미 구매되었습니다.\n다른 좌석을 선택해 주세요.",
            errorCode = "ERR_SOLD_OUT",
        )
        "LOCK_FAILED" -> FailureConfig(
            title = "좌석 잠금에 실패했습니다",
            description = "좌석 선점 과정에서 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.",
            errorCode = "ERR_LOCK_FAIL",
        )
        "LIMIT_EXCEEDED" -> FailureConfig(
            title = "구매 한도를 초과했습니다",
            description = "1인당 최대 구매 가능 매수를 초과하였습니다.\n구매 수량을 확인해 주세요.",
            errorCode = "ERR_LIMIT",
        )
        "INSUFFICIENT_BALANCE" -> FailureConfig(
            title = "잔액이 부족합니다",
            description = "CTK 잔액이 결제 금액보다 부족합니다.\n충전 후 다시 시도해 주세요.",
            errorCode = "ERR_BALANCE",
        )
        "NETWORK" -> FailureConfig(
            title = "네트워크 오류가 발생했습니다",
            description = "서버와의 통신 중 오류가 발생했습니다.\n인터넷 연결을 확인하고 다시 시도해 주세요.",
            errorCode = "ERR_NETWORK",
        )
        else -> FailureConfig(
            title = "결제에 실패했습니다",
            description = "알 수 없는 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.",
            errorCode = "ERR_UNKNOWN",
        )
    }
}

@Composable
fun PurchaseFailedScreen(
    eventId: String,
    reason: String,
    onRetry: (eventId: String) -> Unit,
    onGoHome: () -> Unit,
) {
    val config = remember(reason) { getFailureConfig(reason) }

    Scaffold(
        topBar = {
            AppHeader(title = "결제 실패")
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))

            // Error icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Danger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = "오류",
                    tint = Danger,
                    modifier = Modifier.size(48.dp),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Title
            Text(
                text = config.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // Description
            Text(
                text = config.description,
                fontSize = 14.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Error code badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Muted,
            ) {
                Text(
                    text = config.errorCode,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedForeground,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Retry button
                Button(
                    onClick = { onRetry(eventId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = White,
                    ),
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "좌석 재선택",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Go home button
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MutedForeground,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(BorderColor),
                    ),
                ) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "홈으로 가기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
