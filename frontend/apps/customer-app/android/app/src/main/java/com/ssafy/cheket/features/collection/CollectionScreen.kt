// NOTE: 미사용 화면. 네이티브 Compose 컬렉션 뷰어였으나,
// CollectionListWebViewScreen(customer-collection 웹앱 WebView)으로 대체됨.
package com.ssafy.cheket.features.collection

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.ssafy.cheket.ui.theme.*
import kotlin.math.*

// ─── Effect Types ────────────────────────────────────────────────
enum class EffectType(val label: String) {
    NONE("None"),
    GOLD_FOIL("Gold Foil"),
    SILVER_FOIL("Silver Foil"),
    ROSE_FOIL("Rose Foil"),
    HOLO_RAINBOW("Rainbow"),
    HOLO_AURORA("Aurora"),
    HOLO_PRISM("Prism"),
    HOLO_COSMOS("Cosmos"),
    HOLO_SUNSET("Sunset"),
    HOLO_NEON("Neon"),
}

private val HOLO_EFFECTS = listOf(
    EffectType.HOLO_RAINBOW, EffectType.HOLO_AURORA, EffectType.HOLO_PRISM,
    EffectType.HOLO_COSMOS, EffectType.HOLO_SUNSET, EffectType.HOLO_NEON,
)

private fun getDefaultEffect(ticketId: String): EffectType {
    val num = ticketId.replace(Regex("\\D"), "").toIntOrNull() ?: 0
    return HOLO_EFFECTS[num % HOLO_EFFECTS.size]
}

private fun isMetalEffect(effect: EffectType) =
    effect == EffectType.GOLD_FOIL || effect == EffectType.SILVER_FOIL || effect == EffectType.ROSE_FOIL

// Metal base colors
private fun metalBaseColor(effect: EffectType): Color = when (effect) {
    EffectType.GOLD_FOIL -> Color(0xFFD9AD2A)
    EffectType.SILVER_FOIL -> Color(0xFFC7D0DC)
    EffectType.ROSE_FOIL -> Color(0xFFC79080)
    else -> Color(0xFF0B0F1A)
}

// Metal poster tint (grayscale + hue shift via overlay)
private fun metalOverlayColor(effect: EffectType): Color = when (effect) {
    EffectType.GOLD_FOIL -> Color(0x40D9AD2A)
    EffectType.SILVER_FOIL -> Color(0x30A0B0C0)
    EffectType.ROSE_FOIL -> Color(0x38C79080)
    else -> Color.Transparent
}

// ─── CollectionScreen ────────────────────────────────────────────
@Composable
fun CollectionScreen(
    appContainer: AppContainer,
    onTicketClick: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tiltState by rememberTiltState(maxDegrees = 10f)

    var activeIndex by remember { mutableIntStateOf(0) }
    var effectOverrides by remember { mutableStateOf(mapOf<String, EffectType>()) }

    // Sync index to bounds
    LaunchedEffect(uiState.usedTickets.size) {
        if (uiState.usedTickets.isNotEmpty()) {
            activeIndex = activeIndex.mod(uiState.usedTickets.size)
        }
    }

    fun getEffect(ticketId: String) = effectOverrides[ticketId] ?: getDefaultEffect(ticketId)

    val tickets = uiState.usedTickets
    val activeTicket = tickets.getOrNull(activeIndex.mod(tickets.size.coerceAtLeast(1)))

    Scaffold(
        topBar = {
            AppHeader(title = "티켓 컬렉션", onBack = onBack)
        },
    ) { innerPadding ->
        if (tickets.isEmpty() && !uiState.isLoading) {
            EmptyState(
                title = "아직 컬렉션에 담긴 티켓이 없어요",
                description = "관람 완료된 티켓이 생기면 이곳에서 특별한 컬렉션으로 확인할 수 있어요.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else if (tickets.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0A0E18))
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Title area ──
                Spacer(Modifier.height(16.dp))
                if (activeTicket != null) {
                    Text(
                        activeTicket.showName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 40.dp)
                            .clickable { onTicketClick(activeTicket.id) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activeTicket.numbering.ifBlank { "#${activeTicket.id.takeLast(4).padStart(4, '0')}" },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                    )
                }

                // ── CoverFlow Carousel ──
                Spacer(Modifier.height(20.dp))
                CoverFlowCarousel(
                    tickets = tickets,
                    activeIndex = activeIndex,
                    onActiveIndexChange = { activeIndex = it },
                    getEffect = ::getEffect,
                    tiltX = tiltState.x,
                    tiltY = tiltState.y,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                // ── Navigation arrows ──
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { activeIndex-- }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "이전",
                            tint = Color.White.copy(alpha = 0.6f),
                        )
                    }
                    Text(
                        "${activeIndex.mod(tickets.size) + 1} / ${tickets.size}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    IconButton(onClick = { activeIndex++ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "다음",
                            tint = Color.White.copy(alpha = 0.6f),
                        )
                    }
                }

                // ── Effect selector ──
                EffectSelector(
                    currentEffect = activeTicket?.let { getEffect(it.id) } ?: EffectType.NONE,
                    onEffectChange = { effect ->
                        activeTicket?.let { t ->
                            effectOverrides = effectOverrides + (t.id to effect)
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}

// ─── CoverFlow Carousel ─────────────────────────────────────────
@Composable
private fun CoverFlowCarousel(
    tickets: List<Ticket>,
    activeIndex: Int,
    onActiveIndexChange: (Int) -> Unit,
    getEffect: (String) -> EffectType,
    tiltX: Float,
    tiltY: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val normalizedIndex = activeIndex.mod(tickets.size)

    // Card dimensions
    val cardWidth = 220.dp
    val cardHeight = 430.dp
    val compactScale = 0.55f

    // Swipe detection
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(tickets.size) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAccumulator > 80f) onActiveIndexChange(activeIndex - 1)
                        else if (dragAccumulator < -80f) onActiveIndexChange(activeIndex + 1)
                        dragAccumulator = 0f
                    },
                    onDragCancel = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragAccumulator += dragAmount },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // Render cards: -2..+2 around active
        for (offsetRaw in -2..2) {
            val idx = (normalizedIndex + offsetRaw).mod(tickets.size)
            val ticket = tickets[idx]
            val absOffset = abs(offsetRaw)
            val isFocus = absOffset == 0

            val scale by animateFloatAsState(
                targetValue = if (isFocus) 1f else (0.7f - absOffset * 0.08f).coerceAtLeast(0.5f),
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                label = "scale_$idx",
            )
            val alpha by animateFloatAsState(
                targetValue = if (isFocus) 1f else (0.65f - absOffset * 0.2f).coerceAtLeast(0.15f),
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                label = "alpha_$idx",
            )
            val translationX by animateFloatAsState(
                targetValue = offsetRaw * 160f * density,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 180f),
                label = "tx_$idx",
            )
            val translationY by animateFloatAsState(
                targetValue = if (isFocus) -6f else absOffset * 8f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f),
                label = "ty_$idx",
            )

            val effect = getEffect(ticket.id)
            val numberAura = getNumberAura(ticket.id)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = translationX
                        this.translationY = translationY * density
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        // Focus card: apply tilt
                        if (isFocus) {
                            rotationX = tiltX * 0.3f
                            rotationY = tiltY * 0.3f
                            cameraDistance = 12f * density
                        }
                    }
                    .size(cardWidth, cardHeight)
                    .then(
                        if (!isFocus) Modifier.clickable {
                            onActiveIndexChange(activeIndex + offsetRaw)
                        } else Modifier
                    ),
            ) {
                if (isFocus) {
                    // Focus card: full interactive with flip
                    FocusCard(
                        ticket = ticket,
                        effect = effect,
                        numberAura = numberAura,
                        tiltX = tiltX,
                        tiltY = tiltY,
                    )
                } else {
                    // Non-focus: front only, smaller
                    TicketFrontFace(
                        ticket = ticket,
                        effect = effect,
                        numberAura = numberAura,
                        tiltX = 0f,
                        tiltY = 0f,
                    )
                }
            }
        }
    }
}

// ─── FocusCard (interactive, flippable) ──────────────────────────
@Composable
private fun FocusCard(
    ticket: Ticket,
    effect: EffectType,
    numberAura: NumberAuraType,
    tiltX: Float,
    tiltY: Float,
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "focusFlip",
    )

    var touchTiltX by remember { mutableFloatStateOf(0f) }
    var touchTiltY by remember { mutableFloatStateOf(0f) }
    val animTouchX by animateFloatAsState(touchTiltX, tween(150), label = "ftx")
    val animTouchY by animateFloatAsState(touchTiltY, tween(150), label = "fty")
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    val finalTiltX = tiltX * 0.3f + animTouchX
    val finalTiltY = tiltY * 0.3f + animTouchY
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                            touchTiltX = -ny * 8f
                            touchTiltY = nx * 8f
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
            TicketFrontFace(
                ticket = ticket,
                effect = effect,
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

// ─── Front Face ──────────────────────────────────────────────────
@Composable
private fun TicketFrontFace(
    ticket: Ticket,
    effect: EffectType,
    numberAura: NumberAuraType,
    tiltX: Float,
    tiltY: Float,
) {
    val isMetal = isMetalEffect(effect)
    val isHolo = effect.name.startsWith("HOLO_")
    val holoVariant = when (effect) {
        EffectType.HOLO_RAINBOW -> HoloVariant.RAINBOW
        EffectType.HOLO_AURORA -> HoloVariant.AURORA
        EffectType.HOLO_PRISM -> HoloVariant.PRISM
        EffectType.HOLO_COSMOS -> HoloVariant.COSMOS
        EffectType.HOLO_SUNSET -> HoloVariant.SUNSET
        EffectType.HOLO_NEON -> HoloVariant.NEON
        else -> HoloVariant.RAINBOW
    }
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isMetal) metalBaseColor(effect) else Color(0xFF0B0F1A)),
    ) {
        // Poster
        AsyncImage(
            model = ticket.poster,
            contentDescription = ticket.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = if (isMetal) 0.35f else 1f,
        )

        // Metal tint overlay
        if (isMetal) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(metalOverlayColor(effect))
            )
            // Metal sheen (animated diagonal)
            val sheenPhase by infiniteTransition.animateFloat(
                initialValue = -0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "metalSheen",
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        val sheenColor = when (effect) {
                            EffectType.GOLD_FOIL -> Color(0x30FFFDE6)
                            EffectType.SILVER_FOIL -> Color(0x28F0F4FF)
                            EffectType.ROSE_FOIL -> Color(0x28FFE8E0)
                            else -> Color.Transparent
                        }
                        val cx = size.width * sheenPhase
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(sheenColor, Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(cx, size.height * 0.3f),
                                radius = size.width * 0.6f,
                            ),
                        )
                    }
            )
        }

        // Dark bottom gradient
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x080B0F1A),
                            Color(0x200B0F1A),
                            if (isMetal) Color(0x600B0F1A) else Color(0x940B0F1A),
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
                        listOf(Color(0x1F000000), Color.Transparent),
                        endY = 200f,
                    )
                )
        )

        // Holographic overlay (for holo effects)
        if (isHolo) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawHolographicOverlay(holoColors, shimmerPhase, tiltX, tiltY)
                    }
            )
        }

        // Glare
        Box(Modifier.fillMaxSize().glareEffect(tiltX, tiltY))

        // Sparkle for spark-rare
        if (numberAura == NumberAuraType.SPARK_RARE) {
            SparkleParticles(particleCount = 8, modifier = Modifier.fillMaxSize())
        }

        // "COLLECTIBLE TICKET" label
        Text(
            "COLLECTIBLE TICKET",
            fontSize = 7.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )

        // Bottom info
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                ticket.showName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                ticket.numbering.ifBlank { "No.${ticket.id.takeLast(4).padStart(4, '0')}" },
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

// ─── Back Face ───────────────────────────────────────────────────
@Composable
private fun TicketBackFace(ticket: Ticket) {
    val gradeColor = remember(ticket.grade) { getGradeColor(ticket.grade) }

    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B0F1A)),
    ) {
        AsyncImage(
            model = ticket.poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.26f,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xE6090D18), Color(0xED090D18)))
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                "COLLECTIBLE TICKET",
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                ticket.showName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
            Spacer(Modifier.height(12.dp))

            BackInfoRow(label = "DATE", value = com.ssafy.cheket.core.util.DateTimeUtils.formatShowDate(ticket.showDate))
            Spacer(Modifier.height(8.dp))
            BackInfoRow(label = "VENUE", value = ticket.venue)

            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SeatInfoBox("GRADE", ticket.grade, gradeColor.bg, Modifier.weight(1f))
                SeatInfoBox("SEAT", ticket.seatLabel, Color(0xFFF8E28A), Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))

            Text(
                ticket.numbering.ifBlank { "#${ticket.id.takeLast(4).padStart(4, '0')}" },
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.3f),
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.widthIn(min = 40.dp),
        )
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.75f),
            lineHeight = 14.sp,
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
            letterSpacing = 1.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.3f),
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

// ─── Effect Selector ─────────────────────────────────────────────
@Composable
private fun EffectSelector(
    currentEffect: EffectType,
    onEffectChange: (EffectType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val effects = EffectType.entries

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "EFFECT",
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
        )

        // Scrollable effect chips
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(effects.size) { index ->
                val effect = effects[index]
                val isSelected = effect == currentEffect
                val chipColor = when {
                    effect == EffectType.GOLD_FOIL -> Color(0xFFD9AD2A)
                    effect == EffectType.SILVER_FOIL -> Color(0xFFC7D0DC)
                    effect == EffectType.ROSE_FOIL -> Color(0xFFC79080)
                    effect.name.startsWith("HOLO_") -> Color(0xFF60EFFF)
                    else -> Color.White.copy(alpha = 0.3f)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            if (isSelected) Modifier.background(chipColor.copy(alpha = 0.2f))
                                .border(1.dp, chipColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            else Modifier.background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        )
                        .clickable { onEffectChange(effect) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        effect.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) chipColor else Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}
