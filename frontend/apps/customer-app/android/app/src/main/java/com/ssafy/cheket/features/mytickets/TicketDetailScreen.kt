package com.ssafy.cheket.features.mytickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TicketStatusBadge
import com.ssafy.cheket.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TicketDetailScreen(
    ticketId: String,
    onQrCheckin: (String) -> Unit,
    onTransfer: (String) -> Unit,
    onResaleCreate: (String) -> Unit,
    onBack: () -> Unit,
) {
    val ticket = remember(ticketId) {
        MockDataSource.mockTickets.find { it.id == ticketId }
    }

    Scaffold(
        topBar = { AppHeader(title = "티켓 상세", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        if (ticket == null) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("티켓을 찾을 수 없습니다", color = MutedForeground, fontSize = 14.sp)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // --- Poster + Status Badge ---
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AsyncImage(
                    model = ticket.poster,
                    contentDescription = ticket.eventName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                ) {
                    TicketStatusBadge(status = ticket.status)
                }
            }

            // --- Info Card ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = ticket.eventName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    HorizontalDivider(color = BorderColor)

                    InfoRow(label = "일시", value = ticket.eventDate)
                    InfoRow(label = "장소", value = ticket.venue)
                    InfoRow(label = "좌석", value = ticket.seatLabel)
                    InfoRow(label = "등급", value = ticket.grade)
                    InfoRow(label = "원가", value = formatPrice(ticket.originalPrice))

                    if (ticket.status == TicketStatus.LISTED && ticket.resalePrice != null) {
                        HorizontalDivider(color = BorderColor)
                        InfoRow(
                            label = "리세일 가격",
                            value = formatPrice(ticket.resalePrice),
                            valueColor = Primary,
                        )
                    }

                    if (ticket.status == TicketStatus.USED && ticket.attendedDate != null) {
                        HorizontalDivider(color = BorderColor)
                        InfoRow(label = "참석일", value = ticket.attendedDate)
                    }
                }
            }

            // --- NFT Info ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "NFT 정보",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground,
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    NftInfoRow(
                        label = "Token ID",
                        value = "CTK-${ticket.id.uppercase().replace("_", "-")}",
                    )
                    NftInfoRow(
                        label = "소유자 주소",
                        value = MockDataSource.mockUser.walletAddress,
                    )
                }
            }

            // --- Action Buttons ---
            when (ticket.status) {
                TicketStatus.SOLD -> {
                    // QR Check-in (primary)
                    Button(
                        onClick = { onQrCheckin(ticketId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("QR 체크인", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Transfer + Resale (side by side)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onTransfer(ticketId) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("양도하기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = { onResaleCreate(ticketId) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("판매하기", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                TicketStatus.LISTED -> {
                    Button(
                        onClick = { /* cancel listing mock */ },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    ) {
                        Text("리스팅 취소", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = White)
                    }
                }

                TicketStatus.USED -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("사용 완료", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                TicketStatus.EXPIRED -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("만료된 티켓", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(
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

@Composable
private fun NftInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 11.sp, color = MutedForeground)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Muted)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun formatPrice(price: Int): String {
    val fmt = NumberFormat.getNumberInstance(Locale.KOREA)
    return "${fmt.format(price)} CTK"
}
