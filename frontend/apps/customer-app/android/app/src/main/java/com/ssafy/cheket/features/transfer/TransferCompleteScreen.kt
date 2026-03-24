package com.ssafy.cheket.features.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.navigation.NavParams
import com.ssafy.cheket.ui.theme.*

@Composable
fun TransferCompleteScreen(
    ticketId: String,
    onGoToTickets: () -> Unit,
    onGoHome: () -> Unit,
) {
    val ticket = remember(ticketId) {
        NavParams.selectedTicket?.takeIf { it.id == ticketId }
            ?: MockDataSource.mockTickets.find { it.id == ticketId }
    }
    val recipientLabel = NavParams.recipientName.ifEmpty {
        NavParams.recipientPhone.ifEmpty { "-" }
    }
    val transferTransactionId = NavParams.transferTransactionId

    Scaffold(containerColor = Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(24.dp))

            // --- Success Icon with Decorative Dots ---
            Box(contentAlignment = Alignment.Center) {
                // Decorative dots
                val dotPositions = listOf(
                    -40f to -35f, 35f to -40f, -35f to 30f, 40f to 35f,
                    -20f to -45f, 20f to -42f, -42f to 5f, 45f to 0f,
                )
                dotPositions.forEach { (dx, dy) ->
                    Box(
                        Modifier
                            .offset(x = dx.dp, y = dy.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.3f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Success.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "양도 완료",
                        tint = Success,
                        modifier = Modifier.size(52.dp),
                    )
                }
            }

            Text(
                "양도 요청이 접수되었습니다!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                "블록체인 처리 후 상대방 티켓으로 반영됩니다.\n내 티켓 목록에서 상태를 다시 확인해주세요.",
                fontSize = 14.sp,
                color = MutedForeground,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            // --- Ticket Info Card with Poster Strip ---
            if (ticket != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        // Poster strip
                        AsyncImage(
                            model = ticket.poster,
                            contentDescription = ticket.showName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(72.dp)
                                .fillMaxHeight()
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        bottomStart = 12.dp,
                                    )
                                ),
                        )

                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                ticket.showName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                ticket.showDate,
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                            Text(
                                ticket.venue,
                                fontSize = 12.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }

            // --- Detail Rows ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "좌석", value = ticket?.seatLabel ?: "-")
                    HorizontalDivider(color = BorderColor)
                    DetailRow(label = "수신 연락처", value = recipientLabel)
                    HorizontalDivider(color = BorderColor)
                    DetailRow(label = "양도 금액", value = "무료", valueColor = Primary)
                }
            }

            // --- Transaction Status ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "처리 정보",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    if (transferTransactionId > 0L) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("TX ID", fontSize = 11.sp, color = MutedForeground)
                            Text(
                                "#$transferTransactionId",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnBackground,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Muted)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("처리 상태", fontSize = 13.sp, color = MutedForeground)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("요청 접수", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Success)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // --- Action Buttons ---
            Button(
                onClick = onGoToTickets,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("내 티켓 확인하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
            ) {
                Text("홈으로 돌아가기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = OnBackground,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = MutedForeground)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
