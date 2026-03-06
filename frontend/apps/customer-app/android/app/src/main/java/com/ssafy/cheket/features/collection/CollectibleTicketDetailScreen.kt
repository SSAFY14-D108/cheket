package com.ssafy.cheket.features.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun CollectibleTicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
) {
    val ticket = remember { MockDataSource.mockTickets.find { it.id == ticketId } }
    val user = remember { MockDataSource.mockUser }

    if (ticket == null) {
        Scaffold(topBar = { AppHeader(title = "소장 티켓", onBack = onBack) }) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("티켓을 찾을 수 없습니다.", color = MutedForeground, fontSize = 14.sp)
            }
        }
        return
    }

    val tokenId = "#${ticket.id.hashCode().toUInt().toString(16).take(8).uppercase()}"
    val contractAddress = "0xCheket${ticket.eventId.hashCode().toUInt().toString(16).padStart(8, '0')}...NFT"

    Scaffold(
        topBar = { AppHeader(title = "소장 티켓", onBack = onBack) },
        bottomBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 8.dp, color = Surface) {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { /* placeholder - share */ },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("소장 티켓 공유하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Full Poster with overlay
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            ) {
                AsyncImage(
                    model = ticket.poster,
                    contentDescription = ticket.eventName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Gradient overlay
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Transparent, Color(0xDD0B0F1A)),
                                startY = 200f,
                            )
                        )
                )

                // NFT Badge
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldColor.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Verified, contentDescription = null, tint = White, modifier = Modifier.size(14.dp))
                        Text("NFT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }

                // Event info overlaid on poster bottom
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        ticket.eventName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        Text(ticket.eventDate, fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        Text(ticket.venue, fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                    }
                }
            }

            // Detail Card
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("티켓 상세", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)

                        HorizontalDivider(color = BorderColor)

                        DetailInfoRow("좌석", ticket.seatLabel)
                        DetailInfoRow("등급", ticket.grade)
                        DetailInfoRow("관람일", ticket.attendedDate ?: ticket.eventDate)
                        DetailInfoRow("상태", "관람 완료")
                    }
                }

                // NFT Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Muted),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Link, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text("NFT 정보", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                        }

                        HorizontalDivider(color = BorderColor)

                        NftInfoRow("Token ID", tokenId)
                        NftInfoRow("Owner", user.walletAddress.take(10) + "..." + user.walletAddress.takeLast(4))
                        NftInfoRow("Contract", contractAddress)
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = MutedForeground)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
    }
}

@Composable
private fun NftInfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, color = SubText)
        Text(
            value,
            fontSize = 12.sp,
            color = OnBackground,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp),
        )
    }
}
