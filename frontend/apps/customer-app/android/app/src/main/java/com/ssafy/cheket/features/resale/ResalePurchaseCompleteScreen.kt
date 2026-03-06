package com.ssafy.cheket.features.resale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*

@Composable
fun ResalePurchaseCompleteScreen(
    purchasedTicketId: String,
    onGoToTicketDetail: (String) -> Unit,
    onGoToTickets: () -> Unit,
) {
    val ticket = remember {
        MockDataSource.mockTickets.find { it.id == purchasedTicketId }
    }
    val resaleItem = remember {
        MockDataSource.mockResaleItems.find { it.ticketId == purchasedTicketId }
    }

    Scaffold(
        topBar = { AppHeader(title = "구매 완료") },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            // Success Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLight),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Text(
                        "구매가 완료되었습니다!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                    )
                    Text(
                        "티켓이 지갑으로 전송되었습니다",
                        fontSize = 13.sp,
                        color = MutedForeground,
                    )
                }
            }

            // Ticket Card
            val eventName = ticket?.eventName ?: resaleItem?.eventName ?: "알 수 없음"
            val eventDate = ticket?.eventDate ?: resaleItem?.eventDate ?: ""
            val venue = ticket?.venue ?: resaleItem?.venue ?: ""
            val poster = ticket?.poster ?: resaleItem?.poster ?: ""
            val seatLabel = ticket?.seatLabel ?: resaleItem?.seatLabel ?: ""
            val grade = ticket?.grade ?: resaleItem?.grade ?: ""

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = poster,
                            contentDescription = eventName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 70.dp, height = 94.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Muted),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                eventName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            DetailRow(Icons.Outlined.CalendarMonth, eventDate)
                            DetailRow(Icons.Outlined.LocationOn, venue)
                            DetailRow(Icons.Outlined.EventSeat, "$seatLabel ($grade)")
                        }
                    }
                }
            }

            // Payment Summary
            val originalPrice = resaleItem?.originalPrice ?: ticket?.originalPrice ?: 0
            val resalePrice = resaleItem?.resalePrice ?: ticket?.resalePrice ?: originalPrice
            val discount = originalPrice - resalePrice

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("결제 내역", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                    HorizontalDivider(color = BorderColor)

                    PaymentRow("정가", "%,d CTK".format(originalPrice))
                    if (discount != 0) {
                        PaymentRow(
                            "할인",
                            "${if (discount > 0) "-" else "+"}%,d CTK".format(kotlin.math.abs(discount)),
                            valueColor = if (discount > 0) Success else Danger,
                        )
                    }

                    HorizontalDivider(color = BorderColor)

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("실 결제 금액", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                        Text(
                            "%,d CTK".format(resalePrice),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                        )
                    }
                }
            }

            // Blockchain Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Muted),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Link, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Text("블록체인 정보", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                    }
                    BlockchainRow("Transaction ID", "0x${purchasedTicketId.hashCode().toUInt().toString(16).padStart(8, '0')}...abcd")
                    BlockchainRow("Token ID", "#${purchasedTicketId.takeLast(3).padStart(4, '0')}")
                    BlockchainRow("Network", "Cheket Chain (Testnet)")
                    BlockchainRow("Status", "Confirmed")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Buttons
            Button(
                onClick = { onGoToTicketDetail(purchasedTicketId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
            ) {
                Text("티켓 확인하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
            }

            OutlinedButton(
                onClick = onGoToTickets,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            ) {
                Text("내 티켓 목록", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MutedForeground, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = MutedForeground)
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = OnBackground,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = MutedForeground)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun BlockchainRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = SubText)
        Text(value, fontSize = 12.sp, color = OnBackground, fontFamily = FontFamily.Monospace)
    }
}
