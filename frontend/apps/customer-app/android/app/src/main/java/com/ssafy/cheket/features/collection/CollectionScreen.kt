package com.ssafy.cheket.features.collection

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.core.ui.component.TutorialHelpButton
import com.ssafy.cheket.core.ui.component.TutorialId
import com.ssafy.cheket.ui.theme.*

// ?????????????????????????????????????????????????????????????????????
// CollectionScreen ??2-column grid with collectible NFT ticket cards
// ?????????????????????????????????????????????????????????????????????
@Composable
fun CollectionScreen(
    appContainer: AppContainer,
    onTicketClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tiltState by rememberTiltState(maxDegrees = 10f)

    Scaffold(
        topBar = {
            AppHeader(
                title = "컬렉션",
                onBack = onBack,
                actions = {
                    TutorialHelpButton(tutorialId = TutorialId.COLLECTION)
                },
            )
        },
    ) { innerPadding ->
        if (uiState.usedTickets.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "아직 컬렉션에 담긴 티켓이 없어요",
                description = "관람 완료된 티켓이 생기면 이곳에서 특별한 컬렉션으로 확인할 수 있어요.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding),
            ) {
                // Subtitle area
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Muted,
                    ) {
                        Text(
                            "${uiState.usedTickets.size}개",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedForeground,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "관람이 끝난 티켓은 컬렉션으로 보관돼요.\n길게 눌러 자세히 보고, 나만의 티켓 아카이브를 모아보세요.",
                        fontSize = 12.sp,
                        color = MutedForeground,
                        lineHeight = 18.sp,
                    )
                }

                // 2-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(uiState.usedTickets, key = { _, t -> t.id }) { _, ticket ->
                        CollectibleTicketCard(
                            ticket = ticket,
                            holoVariant = getTicketHoloVariant(ticket.id),
                            numberAura = getNumberAura(ticket.id),
                            sensorTiltX = tiltState.x,
                            sensorTiltY = tiltState.y,
                            onLongPress = { onTicketClick(ticket.id) },
                        )
                    }
                }
            }
        }
    }
}

// ?????????????????????????????????????????????????????????????????????
// CollectibleTicketCard ??Flip card with holo overlay, tilt, sparkles
// ?????????????????????????????????????????????????????????????????????
@Composable
private fun CollectibleTicketCard(
    ticket: Ticket,
    holoVariant: HoloVariant,
    numberAura: NumberAuraType,
    sensorTiltX: Float,
    sensorTiltY: Float,
    onLongPress: () -> Unit,
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    // Touch-based tilt (on press)
    var touchTiltX by remember { mutableFloatStateOf(0f) }
    var touchTiltY by remember { mutableFloatStateOf(0f) }
    val animatedTouchTiltX by animateFloatAsState(touchTiltX, tween(150), label = "touchTiltX")
    val animatedTouchTiltY by animateFloatAsState(touchTiltY, tween(150), label = "touchTiltY")
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Combine sensor tilt (subtle) with touch tilt
    val finalTiltX = sensorTiltX * 0.3f + animatedTouchTiltX
    val finalTiltY = sensorTiltY * 0.3f + animatedTouchTiltY

    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.51f)
            .then(
                if (numberAura != NumberAuraType.NONE) Modifier.numberAura(numberAura)
                else Modifier
            )
            .onSizeChanged { cardSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (cardSize.width > 0 && cardSize.height > 0) {
                            val nx = (offset.x / cardSize.width - 0.5f) * 2f
                            val ny = (offset.y / cardSize.height - 0.5f) * 2f
                            touchTiltX = -ny * 8f
                            touchTiltY = nx * 8f
                        }
                        tryAwaitRelease()
                        touchTiltX = 0f
                        touchTiltY = 0f
                    },
                    onTap = {
                        flipped = !flipped
                    },
                    onLongPress = {
                        onLongPress()
                    },
                )
            }
            .graphicsLayer {
                rotationX = finalTiltX
                rotationY = finalTiltY + rotation
                cameraDistance = 12f * density
            },
    ) {
        if (rotation <= 90f) {
            TicketFrontFace(
                ticket = ticket,
                holoVariant = holoVariant,
                numberAura = numberAura,
                tiltX = finalTiltX,
                tiltY = finalTiltY,
            )
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                TicketBackFace(ticket = ticket)
            }
        }
    }
}

// ?????????????????????????????????????????????????????????????????????
// Front face: poster + holo overlay + sparkles
// ?????????????????????????????????????????????????????????????????????
@Composable
private fun TicketFrontFace(
    ticket: Ticket,
    holoVariant: HoloVariant,
    numberAura: NumberAuraType,
    tiltX: Float,
    tiltY: Float,
) {
    val holoColors = remember(holoVariant) { getHoloColors(holoVariant) }

    val infiniteTransition = rememberInfiniteTransition(label = "holoShimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerPhase",
    )

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B0F1A)),
    ) {
        // Poster image
        AsyncImage(
            model = ticket.poster,
            contentDescription = ticket.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Dark bottom gradient
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x080B0F1A),
                            Color(0x200B0F1A),
                            Color(0x940B0F1A),
                        ),
                    )
                )
        )

        // Top darkening
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x1F000000),
                            Color.Transparent,
                        ),
                        endY = 200f,
                    )
                )
        )

        // Holographic overlay
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawHolographicOverlay(holoColors, shimmerPhase, tiltX, tiltY)
                }
        )

        // Glare effect following tilt
        Box(
            Modifier
                .fillMaxSize()
                .glareEffect(tiltX, tiltY)
        )

        // Sparkle particles for spark-rare numbers
        if (numberAura == NumberAuraType.SPARK_RARE) {
            SparkleParticles(
                particleCount = 8,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // "COLLECTIBLE TICKET" label
        Text(
            "COLLECTIBLE TICKET",
            fontSize = 7.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
        )

        // Bottom info
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                ticket.showName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                ticket.numbering.ifBlank { "No.${ticket.id.takeLast(4).padStart(4, '0')}" },
                fontSize = 8.sp,
                color = White.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// ?????????????????????????????????????????????????????????????????????
// Back face: ticket details on dark overlay
// ?????????????????????????????????????????????????????????????????????
@Composable
private fun TicketBackFace(ticket: Ticket) {
    val gradeColor = remember(ticket.grade) { getGradeColor(ticket.grade) }

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B0F1A)),
    ) {
        // Dimmed poster
        AsyncImage(
            model = ticket.poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.26f,
        )
        // Dark overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xE6090D18),
                            Color(0xED090D18),
                        )
                    )
                )
        )

        // Info content
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            Text(
                "COLLECTIBLE TICKET",
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                color = White.copy(alpha = 0.5f),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                ticket.showName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = White.copy(alpha = 0.9f),
                lineHeight = 17.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(12.dp))

            // Divider
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White.copy(alpha = 0.2f))
            )

            Spacer(Modifier.height(10.dp))

            // Date
            BackInfoRow(label = "DATE", value = ticket.showDate.split(" ").firstOrNull() ?: ticket.showDate)
            Spacer(Modifier.height(6.dp))
            // Venue
            BackInfoRow(label = "VENUE", value = ticket.venue)

            Spacer(Modifier.height(12.dp))

            // Divider
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(White.copy(alpha = 0.15f))
            )

            Spacer(Modifier.height(10.dp))

            // Grade & Seat boxes
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SeatInfoBox(
                    label = "GRADE",
                    value = ticket.grade,
                    accentColor = gradeColor.bg,
                    modifier = Modifier.weight(1f),
                )
                SeatInfoBox(
                    label = "SEAT",
                    value = ticket.seatLabel,
                    accentColor = Color(0xFFF8E28A),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.weight(1f))

            // NFT ID at bottom
            Text(
                ticket.numbering.ifBlank { "#${ticket.id.takeLast(4).padStart(4, '0')}" },
                fontSize = 9.sp,
                color = White.copy(alpha = 0.3f),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun BackInfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.3f),
            modifier = Modifier.widthIn(min = 36.dp),
        )
        Text(
            value,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = White.copy(alpha = 0.75f),
            lineHeight = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SeatInfoBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            fontSize = 7.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold,
            color = White.copy(alpha = 0.3f),
        )
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
