// NOTE: 미사용 화면. ArchiveScreen에서만 네비게이션하나, ArchiveScreen 자체가 도달 불가.
// 컬렉션은 CollectionListWebViewScreen(customer-collection 웹앱 WebView)으로 대체됨.
package com.ssafy.cheket.features.collection

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*

// ─────────────────────────────────────────────────────────────────────
// CollectibleTicketDetailScreen — Full detail with large flip card
// ─────────────────────────────────────────────────────────────────────
@Composable
fun CollectibleTicketDetailScreen(
    ticketId: String,
    onBack: () -> Unit,
) {
    val ticket = remember { MockDataSource.mockTickets.find { it.id == ticketId } }
    val user = remember { MockDataSource.mockUser }

    if (ticket == null) {
        Scaffold(
            topBar = {
                AppHeader(
                    title = "소장 티켓",
                    onBack = onBack,
                    helpTutorialId = TutorialId.COLLECTIBLE_TICKET_DETAIL,
                )
            },
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("티켓을 찾을 수 없습니다.", color = MutedForeground, fontSize = 14.sp)
            }
        }
        return
    }

    val holoVariant = remember(ticketId) { getTicketHoloVariant(ticketId) }
    val numberAura = remember(ticketId) { getNumberAura(ticketId) }
    val tiltState by rememberTiltState(maxDegrees = 10f)
    val tokenId = "#${ticket.id.hashCode().toUInt().toString(16).take(8).uppercase()}"
    val contractAddress = "0xCheket${ticket.showId.hashCode().toUInt().toString(16).padStart(8, '0')}...NFT"

    Scaffold(
        topBar = {
            AppHeader(
                title = "소장 티켓",
                onBack = onBack,
                helpTutorialId = TutorialId.COLLECTIBLE_TICKET_DETAIL,
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, shadowElevation = 8.dp, color = Surface) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Large flippable card
            DetailFlipCard(
                ticket = ticket,
                holoVariant = holoVariant,
                numberAura = numberAura,
                sensorTiltX = tiltState.x,
                sensorTiltY = tiltState.y,
            )

            // Tap hint
            Text(
                "카드를 탭하면 뒤집을 수 있어요",
                fontSize = 12.sp,
                color = MutedForeground,
            )

            // Ticket details card
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
                        DetailInfoRow("관람일", com.ssafy.cheket.core.util.DateTimeUtils.formatShowDateTime(ticket.attendedDate ?: ticket.showDate))
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

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// DetailFlipCard — Large centered card with flip, holo, sparkles, tilt
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun DetailFlipCard(
    ticket: com.ssafy.cheket.core.model.Ticket,
    holoVariant: HoloVariant,
    numberAura: NumberAuraType,
    sensorTiltX: Float,
    sensorTiltY: Float,
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "detailFlip",
    )

    // Touch tilt
    var touchTiltX by remember { mutableFloatStateOf(0f) }
    var touchTiltY by remember { mutableFloatStateOf(0f) }
    val animatedTouchTiltX by animateFloatAsState(touchTiltX, tween(200), label = "dTiltX")
    val animatedTouchTiltY by animateFloatAsState(touchTiltY, tween(200), label = "dTiltY")
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    val finalTiltX = sensorTiltX * 0.4f + animatedTouchTiltX
    val finalTiltY = sensorTiltY * 0.4f + animatedTouchTiltY

    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .padding(horizontal = 48.dp)
            .fillMaxWidth()
            .aspectRatio(0.51f)
            .then(
                if (numberAura != NumberAuraType.NONE) Modifier.numberAura(numberAura) else Modifier
            )
            .onSizeChanged { cardSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (cardSize.width > 0 && cardSize.height > 0) {
                            val nx = (offset.x / cardSize.width - 0.5f) * 2f
                            val ny = (offset.y / cardSize.height - 0.5f) * 2f
                            touchTiltX = -ny * 10f
                            touchTiltY = nx * 10f
                        }
                        tryAwaitRelease()
                        touchTiltX = 0f
                        touchTiltY = 0f
                    },
                    onTap = { flipped = !flipped },
                )
            }
            .graphicsLayer {
                rotationX = finalTiltX
                rotationY = finalTiltY + rotation
                cameraDistance = 12f * density
            },
    ) {
        if (rotation <= 90f) {
            DetailCardFront(
                ticket = ticket,
                holoVariant = holoVariant,
                numberAura = numberAura,
                tiltX = finalTiltX,
                tiltY = finalTiltY,
            )
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                DetailCardBack(ticket = ticket)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Detail card front: poster + holo + sparkles
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun DetailCardFront(
    ticket: com.ssafy.cheket.core.model.Ticket,
    holoVariant: HoloVariant,
    numberAura: NumberAuraType,
    tiltX: Float,
    tiltY: Float,
) {
    val holoColors = remember(holoVariant) { getHoloColors(holoVariant) }
    val infiniteTransition = rememberInfiniteTransition(label = "detailHolo")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "detailShimmer",
    )

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B0F1A)),
    ) {
        // Poster
        AsyncImage(
            model = ticket.poster,
            contentDescription = ticket.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Bottom gradient
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x060B0F1A),
                            Color(0x1E0B0F1A),
                            Color(0x940B0F1A),
                        ),
                    )
                )
        )

        // Top gradient
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x1F000000), Color.Transparent),
                        endY = 240f,
                    )
                )
        )

        // Holo overlay
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawHolographicOverlay(holoColors, shimmerPhase, tiltX, tiltY)
                }
        )

        // Glare
        Box(Modifier.fillMaxSize().glareEffect(tiltX, tiltY))

        // Sparkles
        SparkleParticles(particleCount = 14, modifier = Modifier.fillMaxSize())

        // Label
        Text(
            "COLLECTIBLE TICKET",
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
        )

        // Bottom info
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                ticket.showName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                com.ssafy.cheket.core.util.DateTimeUtils.formatShowDate(ticket.showDate),
                fontSize = 11.sp,
                color = White.copy(alpha = 0.7f),
            )
            Text(
                ticket.venue,
                fontSize = 11.sp,
                color = White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                ticket.numbering.ifBlank { "No.${ticket.id.takeLast(4).padStart(4, '0')}" },
                fontSize = 10.sp,
                color = White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Detail card back: full ticket info
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun DetailCardBack(ticket: com.ssafy.cheket.core.model.Ticket) {
    val gradeColor = remember(ticket.grade) { getGradeColor(ticket.grade) }

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B0F1A)),
    ) {
        // Dimmed poster
        AsyncImage(
            model = ticket.poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.22f,
        )
        // Dark overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE6090D18), Color(0xED090D18))
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(18.dp),
        ) {
            Text(
                "COLLECTIBLE TICKET",
                fontSize = 10.sp,
                letterSpacing = 2.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(14.dp))

            Text(
                ticket.showName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = White.copy(alpha = 0.92f),
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White.copy(alpha = 0.22f))
            )

            Spacer(Modifier.height(14.dp))

            BackInfoRowDetail(label = "DATE", value = com.ssafy.cheket.core.util.DateTimeUtils.formatShowDate(ticket.showDate))
            Spacer(Modifier.height(8.dp))
            BackInfoRowDetail(label = "VENUE", value = ticket.venue)

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White.copy(alpha = 0.18f))
            )

            Spacer(Modifier.height(14.dp))

            // Grade & Seat
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DetailSeatBox(
                    label = "GRADE",
                    value = ticket.grade,
                    accentColor = gradeColor.bg,
                    modifier = Modifier.weight(1f),
                )
                DetailSeatBox(
                    label = "SEAT",
                    value = ticket.seatLabel,
                    accentColor = Color(0xFFF8E28A),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.weight(1f))

            // NFT numbering at bottom
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height(1.dp)
                        .background(White.copy(alpha = 0.1f))
                )
                Text(
                    ticket.numbering.ifBlank { "#${ticket.id.takeLast(4).padStart(4, '0')}" },
                    fontSize = 11.sp,
                    color = White.copy(alpha = 0.35f),
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun BackInfoRowDetail(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.3f),
            modifier = Modifier.widthIn(min = 42.dp),
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = White.copy(alpha = 0.75f),
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailSeatBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            label,
            fontSize = 8.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.SemiBold,
            color = White.copy(alpha = 0.3f),
        )
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
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
