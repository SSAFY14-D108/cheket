package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ssafy.cheket.ui.theme.*

/**
 * CHEKET 공통 다이얼로그 — 그라데이션 테두리 스타일.
 *
 * 확인 버튼에 gradientBorder 적용, 취소 버튼은 Muted 스타일.
 * 앱 전체에서 AlertDialog 대신 이 컴포넌트를 사용.
 */
@Composable
fun CheketDialog(
    title: String,
    message: String,
    confirmText: String = "확인",
    dismissText: String? = "취소",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColor: Color = Primary,
    isDanger: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )

            // Message
            Text(
                text = message,
                fontSize = 14.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(Modifier.height(4.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Confirm button — gradient border
                Text(
                    text = confirmText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDanger) Danger else confirmColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .gradientBorder(shape = RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 14.dp),
                )

                // Dismiss button — muted style
                if (dismissText != null) {
                    Text(
                        text = dismissText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Muted)
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

/**
 * 단일 버튼 다이얼로그 (알림/안내용)
 */
@Composable
fun CheketAlertDialog(
    title: String,
    message: String,
    confirmText: String = "확인",
    onConfirm: () -> Unit,
) {
    CheketDialog(
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = null,
        onConfirm = onConfirm,
        onDismiss = onConfirm,
    )
}
