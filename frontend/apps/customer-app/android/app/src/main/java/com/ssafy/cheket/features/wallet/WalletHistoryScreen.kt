package com.ssafy.cheket.features.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.WalletTx
import com.ssafy.cheket.core.model.WalletTxType
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletHistoryScreen(
    onBack: () -> Unit,
) {
    val walletTxs = remember { MockDataSource.mockWalletTxs }
    val grouped = remember {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        walletTxs
            .sortedByDescending { it.createdAt }
            .groupBy { dateFormat.format(Date(it.createdAt)) }
    }

    Scaffold(
        topBar = { AppHeader(title = "거래 내역", onBack = onBack) },
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            grouped.forEach { (dateLabel, transactions) ->
                item(key = "header_$dateLabel") {
                    Text(
                        text = dateLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SubText,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }

                items(transactions, key = { it.id }) { tx ->
                    HistoryTxItem(tx = tx)
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun HistoryTxItem(tx: WalletTx) {
    val isIncome = tx.amount > 0
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.KOREA) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    val typeLabel = when (tx.type) {
        WalletTxType.CHARGE -> "충전"
        WalletTxType.PURCHASE -> "구매"
        WalletTxType.RESALE_BUY -> "리세일 구매"
        WalletTxType.RESALE_SELL -> "리세일 판매"
        WalletTxType.TRANSFER_SEND -> "양도 전송"
        WalletTxType.TRANSFER_RECEIVE -> "양도 수신"
    }

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
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) PrimaryLight else Danger.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isIncome) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = if (isIncome) Primary else Danger,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    tx.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(typeLabel, fontSize = 11.sp, color = MutedForeground)
                    Text(
                        timeFormat.format(Date(tx.createdAt)),
                        fontSize = 11.sp,
                        color = SubText,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isIncome) "+" else ""}${numberFormat.format(tx.amount)} CTK",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Primary else Danger,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "잔액 ${numberFormat.format(tx.balance)} CTK",
                    fontSize = 11.sp,
                    color = SubText,
                )
            }
        }
    }
}
