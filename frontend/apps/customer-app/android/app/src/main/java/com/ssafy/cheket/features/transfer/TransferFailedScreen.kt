package com.ssafy.cheket.features.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun TransferFailedScreen(
    ticketId: String,
    onRetry: (String) -> Unit,
    onGoToTickets: () -> Unit,
) {
    val ticket = remember(ticketId) {
        MockDataSource.mockTickets.find { it.id == ticketId }
    }
    val recipientName = NavParams.recipientName
    val recipientPhone = NavParams.recipientPhone
    val failureReason = NavParams.transferFailureReason.ifEmpty {
        "알 수 없는 오류가 발생했습니다."
    }

    Scaffold(
        topBar = { AppHeader(title = "양도 실패") },
        containerColor = Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // --- Failure Icon ---
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Danger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "양도 실패",
                    tint = Danger,
                    modifier = Modifier.size(52.dp),
                )
            }

            Text(
                "양도에 실패했습니다",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )

            Text(
                "양도 처리 중 문제가 발생했습니다.\n아래 내용을 확인해주세요.",
                fontSize = 14.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            // --- Failure Reason Card ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Danger.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "실패 사유",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Danger,
                        )
                        Text(
                            failureReason,
                            fontSize = 13.sp,
                            color = OnBackground,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            // --- Transfer Detail Card ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "양도 상세",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )

                    HorizontalDivider(color = BorderColor)

                    FailedDetailRow(
                        label = "공연",
                        value = ticket?.eventName ?: "-",
                    )
                    FailedDetailRow(
                        label = "좌석",
                        value = ticket?.seatLabel ?: "-",
                    )
                    FailedDetailRow(
                        label = "양도 대상",
                        value = recipientName.ifEmpty { "-" },
                    )
                    FailedDetailRow(
                        label = "연락처",
                        value = recipientPhone.ifEmpty { "-" },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Action Buttons ---
            Button(
                onClick = { onRetry(ticketId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("다시 시도하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = onGoToTickets,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedForeground),
            ) {
                Text("내 티켓으로 돌아가기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FailedDetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = MutedForeground,
            modifier = Modifier.width(72.dp),
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}
