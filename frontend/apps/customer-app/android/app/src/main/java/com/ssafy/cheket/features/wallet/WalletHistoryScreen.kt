package com.ssafy.cheket.features.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

private val IncomeColor = Color(0xFF10B981)    // 초록 — 수입
private val ExpenseColor = Color(0xFF2563EB)    // 파랑 — 지출
private val NeutralColor = Color(0xFF6366F1)    // 보라 — 양도 등
private val PendingColor = Color(0xFFF59E0B)    // 노랑 — 대기/처리중
private val FailedTextColor = Color(0xFF9CA3AF) // 회색 — 실패

@Composable
fun WalletHistoryScreen(
    onBack: () -> Unit,
    viewModel: WalletHistoryViewModel = viewModel(factory = WalletHistoryViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppHeader(
                title = "트랜잭션 내역",
                onBack = onBack,
                helpTutorialId = TutorialId.WALLET_HISTORY,
            )
        },
        containerColor = Background,
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // 필터 드롭다운
            FilterDropdown(
                selected = selectedFilter,
                onSelect = viewModel::onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when (val state = uiState) {
                is WalletHistoryUiState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is WalletHistoryUiState.Error -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = MutedForeground, fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.load() },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("다시 시도")
                            }
                        }
                    }
                }

                is WalletHistoryUiState.Success -> {
                    if (state.grouped.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("트랜잭션 내역이 없습니다.", color = MutedForeground, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.grouped.forEach { (dateLabel, transactions) ->
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
            }
        }
    }
}

// ── 필터 드롭다운 ──

@Composable
private fun FilterDropdown(
    selected: TxTypeFilter,
    onSelect: (TxTypeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Muted)
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = selected.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MutedForeground,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(White),
        ) {
            TxTypeFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (filter == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (filter == selected) Primary else OnBackground,
                        )
                    },
                    onClick = {
                        onSelect(filter)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ── 트랜잭션 아이템 ──

@Composable
private fun HistoryTxItem(tx: TxUiItem) {
    val isFailed = tx.txStatus == "FAILED"
    val isPending = tx.txStatus == "PENDING" || tx.txStatus == "SUBMITTED"
    val isIncome = (tx.displayAmount ?: 0L) > 0
    val isExpense = (tx.displayAmount ?: 0L) < 0
    val isNeutral = tx.displayAmount == 0L
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    var expanded by remember { mutableStateOf(false) }

    val (icon, iconBg, iconTint) = resolveIcon(tx.type, isFailed, isIncome, isNeutral || isFailed)

    val isResaleCreate = tx.type == "RESALE_CREATE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 아이콘
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(12.dp))

                // typeLabel + 실패 텍스트 + 시간
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            tx.typeLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFailed) FailedTextColor else OnBackground,
                        )
                        if (isFailed) {
                            Text(
                                "실패",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FailedTextColor,
                                textDecoration = TextDecoration.LineThrough,
                            )
                        }
                        if (isPending) {
                            Text(
                                if (tx.txStatus == "PENDING") "대기중" else "처리중",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PendingColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PendingColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))

                    // 양도: seller → buyer 이름 표시
                    if (tx.type == "TRANSFER" && (tx.sellerName != null || tx.buyerName != null)) {
                        Text(
                            "${tx.sellerName ?: "?"} → ${tx.buyerName ?: "?"}",
                            fontSize = 11.sp,
                            color = NeutralColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(1.dp))
                    }

                    // description 한 줄 미리보기
                    Text(
                        tx.description,
                        fontSize = 11.sp,
                        color = if (isFailed) MutedForeground.copy(alpha = 0.6f) else MutedForeground,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                        lineHeight = 16.sp,
                    )

                    Spacer(Modifier.height(2.dp))
                    Text(tx.timeLabel, fontSize = 10.sp, color = SubText)
                }

                Spacer(Modifier.width(8.dp))

                // 금액
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        isFailed -> {
                            // 실패 시 금액 표시하지 않음
                        }
                        isResaleCreate && tx.amount != 0L -> {
                            Text(
                                "등록가",
                                fontSize = 10.sp,
                                color = SubText,
                            )
                            Text(
                                "${numberFormat.format(tx.amount)} SSF",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedForeground,
                            )
                        }
                        isNeutral || tx.displayAmount == null -> {
                            Text(
                                "-",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedForeground,
                            )
                        }
                        else -> {
                            val amountAlpha = if (isPending) 0.5f else 1f
                            Text(
                                "${if (isIncome) "+" else ""}${numberFormat.format(tx.displayAmount)} SSF",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = (if (isIncome) IncomeColor else ExpenseColor).copy(alpha = amountAlpha),
                            )
                        }
                    }
                }
            }

            // 확장 영역 — txHash + blockNumber
            if (expanded) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(10.dp))

                tx.txHash?.let { hash ->
                    DetailRow(label = "TX", value = "${hash.take(10)}...${hash.takeLast(8)}", mono = true)
                    Spacer(Modifier.height(4.dp))
                }

                tx.blockNumber?.let { block ->
                    DetailRow(label = "Block", value = "#${numberFormat.format(block)}", mono = true)
                    Spacer(Modifier.height(4.dp))
                }

                if (tx.txHash == null && tx.blockNumber == null) {
                    Text(
                        "블록체인 정보 없음",
                        fontSize = 11.sp,
                        color = SubText,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, mono: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Muted)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SubText)
        Text(
            value,
            fontSize = 11.sp,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            color = MutedForeground,
        )
    }
}

private data class IconStyle(val icon: ImageVector, val bg: Color, val tint: Color)

private fun resolveIcon(type: String, isFailed: Boolean, isIncome: Boolean, isNeutral: Boolean): IconStyle {
    if (isFailed) return IconStyle(Icons.Filled.Close, FailedTextColor.copy(alpha = 0.1f), FailedTextColor)

    return when {
        type == "TRANSFER" -> IconStyle(Icons.AutoMirrored.Filled.CompareArrows, NeutralColor.copy(alpha = 0.1f), NeutralColor)
        type == "RESALE_CREATE" || type == "RESALE_CANCEL" || type == "STAKEHOLDER_MINT" ->
            IconStyle(Icons.Filled.HorizontalRule, MutedForeground.copy(alpha = 0.1f), MutedForeground)
        isNeutral -> IconStyle(Icons.Filled.HorizontalRule, MutedForeground.copy(alpha = 0.1f), MutedForeground)
        isIncome -> IconStyle(Icons.Filled.ArrowDownward, IncomeColor.copy(alpha = 0.1f), IncomeColor)
        else -> IconStyle(Icons.Filled.ArrowUpward, ExpenseColor.copy(alpha = 0.1f), ExpenseColor)
    }
}
