package com.ssafy.cheket.features.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.TxRecord
import com.ssafy.cheket.core.model.TxStatus
import com.ssafy.cheket.core.model.TxType
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TxHistoryScreen(
    onBack: () -> Unit,
) {
    val txRecords = remember { MockDataSource.mockTxRecords.sortedByDescending { it.createdAt } }

    Scaffold(
        topBar = {
            AppHeader(
                title = "티켓 트랜젝션 내역",
                onBack = onBack,
                helpTutorialId = TutorialId.TX_HISTORY,
            )
        },
    ) { innerPadding ->
        if (txRecords.isEmpty()) {
            EmptyState(
                title = "트랜젝션 내역이 없습니다.",
                description = "티켓 구매, 재판매, 환불 내역이 여기에 표시됩니다.",
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(txRecords, key = { it.id }) { record ->
                    TxRecordItem(record = record)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TxRecordItem(record: TxRecord) {
    val dateFormat = remember { SimpleDateFormat("MM.dd HH:mm", Locale.KOREA) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Top row: label + date/status
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    record.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    dateFormat.format(Date(record.createdAt)),
                    fontSize = 12.sp,
                    color = SubText,
                )
            }

            // Status badge with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val (statusLabel, statusColor, statusIcon) = when (record.status) {
                    TxStatus.CONFIRMED -> Triple("완료", Success, Icons.Outlined.CheckCircle)
                    TxStatus.FAILED -> Triple("실패", Danger, Icons.Outlined.ErrorOutline)
                    TxStatus.PENDING -> Triple("대기중", Warning, Icons.Outlined.Schedule)
                    TxStatus.CONFIRMING -> Triple("확인중", Info, Icons.Outlined.AccessTime)
                }
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(14.dp))
                Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = statusColor)
            }

            // TX Type badge
            TxTypeBadge(type = record.type)

            // Detail box (gray background) per v0-version2
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Muted,
            ) {
                Column(
                    Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // TX Hash
                    DetailRow(
                        label = "TX Hash",
                        value = record.txHash.take(10) + "..." + record.txHash.takeLast(8),
                        isMonospace = true,
                    )

                    // Amount
                    if (record.amount != null) {
                        DetailRow(label = "금액", value = "%,d SSF".format(record.amount))
                    }

                    // Confirmations
                    DetailRow(
                        label = "확인",
                        value = "${record.confirmations}/12",
                        valueColor = when {
                            record.confirmations >= 12 -> Success
                            record.confirmations > 0 -> Info
                            else -> Warning
                        },
                    )

                    // Error message
                    if (record.status == TxStatus.FAILED && record.errorMessage != null) {
                        DetailRow(label = "오류", value = record.errorMessage, valueColor = Danger)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MutedForeground,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 11.sp, color = SubText)
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TxTypeBadge(type: TxType) {
    val (label, bg, fg) = when (type) {
        TxType.PURCHASE -> Triple("티켓 구매", Info.copy(alpha = 0.15f), Info)
        TxType.RESALE_LIST -> Triple("재판매 등록", Primary.copy(alpha = 0.15f), Primary)
        TxType.RESALE_BUY -> Triple("재판매 구매", Success.copy(alpha = 0.15f), Success)
        TxType.TRANSFER -> Triple("티켓 양도", Warning.copy(alpha = 0.15f), Warning)
    }
    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
