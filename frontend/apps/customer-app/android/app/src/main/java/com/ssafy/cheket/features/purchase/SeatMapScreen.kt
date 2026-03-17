package com.ssafy.cheket.features.purchase

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ssafy.cheket.core.datasource.mock.MockDataSource
import com.ssafy.cheket.core.model.SeatMapSection
import com.ssafy.cheket.core.model.SeatPosition
import com.ssafy.cheket.core.model.SectionBounds
import com.ssafy.cheket.core.model.SectionSeat
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch
import com.ssafy.cheket.core.datasource.mock.MockDataSource.VenueInfo
import java.text.NumberFormat
import java.util.Locale

/* ── 논리 캔버스 상수 ── */
private const val CANVAS_W = 1000f
private const val CANVAS_H = 1200f
private const val STAGE_LEFT = 150f
private const val STAGE_TOP = 40f
private const val STAGE_W = 700f
private const val STAGE_H = 80f

private const val SEAT_RADIUS = 10f
private const val SEAT_GAP = 4f

/* ── LOD 줌 임계치 ── */
private const val ZOOM_SHOW_SEATS = 2.0f
private const val ZOOM_SHOW_LABELS = 4.0f

/* ── 미니 프리뷰 높이 ── */
private val MINI_CANVAS_HEIGHT = 350.dp

/* ── 디자인 색상 ── */
private val CanvasBg = Color(0xFFF1F5F9)
private val StageDark = Color(0xFF0F172A)
private val StageAccent = Color(0xFF1E293B)
private val SeatSold = Color(0xFFCBD5E1)
private val SeatLocked = Color(0xFFFEF3C7)
private val SeatLockedBorder = Color(0xFFF59E0B)
private val SheetBg = Color(0xFFFAFAFC)
private val ChipBg = Color(0xFF0F172A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatMapScreen(
    showId: String,
    onBack: () -> Unit = {},
    onPurchase: () -> Unit = {},
    viewModel: SeatMapViewModel = viewModel(factory = SeatMapViewModel.factory(showId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isExpanded by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selectedDetails = remember(state.selectedSeatIds, state.sections) {
        viewModel.getSelectedSeatDetails()
    }

    // ── Toast 메시지 콜백 ──
    val showMessage: (String) -> Unit = remember(snackbarHostState) {
        { msg: String ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            }
        }
    }

    // ── 좌석 탭 핸들러 (조건 체크 + 토스트) ──
    val handleSeatTap: (SectionSeat) -> Unit = remember(state.selectedSeatIds, state.maxSeats) {
        { seat: SectionSeat ->
            when {
                seat.status == "SOLD" -> showMessage("이미 판매된 좌석입니다")
                seat.status == "LOCKED" -> showMessage("현재 잠금된 좌석입니다")
                seat.status != "AVAILABLE" -> showMessage("선택할 수 없는 좌석입니다")
                seat.sessionSeatId !in state.selectedSeatIds
                    && state.selectedSeatIds.size >= state.maxSeats ->
                    showMessage("최대 ${state.maxSeats}석까지 선택 가능합니다")
                else -> viewModel.toggleSeat(seat)
            }
        }
    }

    // ── BottomSheet (전체화면 모드에서 플로팅 칩 터치 시) ──
    if (showBottomSheet && selectedDetails.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = SheetBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFD1D5DB))
                )
            },
        ) {
            BottomSheetContent(
                selectedSeats = selectedDetails,
                totalPrice = viewModel.totalPrice,
                maxSeats = state.maxSeats,
                onRemove = { info ->
                    val seat = state.sections
                        .flatMap { it.seats }
                        .find { it.sessionSeatId == info.sessionSeatId }
                    seat?.let { viewModel.toggleSeat(it) }
                },
                onPurchase = {
                    showBottomSheet = false
                    onPurchase()
                },
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            AppHeader(
                title = state.show?.name ?: "좌석 배치도",
                onBack = {
                    if (isExpanded) isExpanded = false
                    else onBack()
                },
            )

            // ── 공연장 선택 칩 ──
            if (!isExpanded) {
                VenueSelector(
                    venues = MockDataSource.venuePresets,
                    selectedIndex = state.venueIndex,
                    onSelect = { viewModel.switchVenue(it) },
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val fullHeight = maxHeight

                    val canvasHeight by animateDpAsState(
                        targetValue = if (isExpanded) fullHeight else MINI_CANVAS_HEIGHT,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        label = "canvas_height",
                    )
                    val cornerRadius by animateDpAsState(
                        targetValue = if (isExpanded) 0.dp else 20.dp,
                        animationSpec = tween(400, easing = FastOutSlowInEasing),
                        label = "corner_radius",
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                        // ── 좌석 배치도 캔버스 영역 ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(canvasHeight)
                                .then(
                                    if (!isExpanded) Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                    else Modifier
                                )
                                .clip(RoundedCornerShape(cornerRadius))
                                .then(
                                    if (!isExpanded) Modifier.border(
                                        1.dp,
                                        BorderColor.copy(alpha = 0.5f),
                                        RoundedCornerShape(cornerRadius)
                                    )
                                    else Modifier
                                )
                        ) {
                            key(state.venueIndex) {
                                ZoomableSeatCanvas(
                                    sections = state.sections,
                                    sectionBounds = state.sectionBounds,
                                    seatPositions = state.seatPositions,
                                    selectedSeatIds = state.selectedSeatIds,
                                    onSeatTap = handleSeatTap,
                                )
                            }

                            // 확대/축소 토글 버튼
                            Surface(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier
                                    .align(
                                        if (isExpanded) Alignment.TopEnd
                                        else Alignment.BottomEnd
                                    )
                                    .padding(12.dp)
                                    .size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isExpanded) ChipBg.copy(alpha = 0.7f)
                                else Primary,
                                shadowElevation = 4.dp,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.FullscreenExit
                                        else Icons.Outlined.ZoomOutMap,
                                        contentDescription = if (isExpanded) "축소" else "전체화면",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White,
                                    )
                                }
                            }

                            // ── 전체화면 모드: 하단 플로팅 선택 요약 칩 ──
                            if (isExpanded && selectedDetails.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                                ) {
                                    FloatingSelectionChip(
                                        count = selectedDetails.size,
                                        maxSeats = state.maxSeats,
                                        totalPrice = viewModel.totalPrice,
                                        onClick = { showBottomSheet = true },
                                    )
                                }
                            }
                        }

                        // ── 미니 모드 하단 콘텐츠 ──
                        if (!isExpanded) {
                            LegendBar()

                            // 선택 안내 or 좌석 수 카운터
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selectedDetails.isEmpty()) {
                                    Text(
                                        "구역을 터치하여 좌석을 확대하세요",
                                        fontSize = 13.sp,
                                        color = MutedForeground,
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Primary.copy(alpha = 0.1f),
                                        ) {
                                            Text(
                                                "${selectedDetails.size}/${state.maxSeats}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 2.dp
                                                ),
                                            )
                                        }
                                        Text(
                                            "좌석 선택됨",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OnBackground,
                                        )
                                    }
                                }
                            }

                            // 선택 패널
                            if (selectedDetails.isNotEmpty()) {
                                Spacer(Modifier.weight(1f))
                                BottomSelectionPanel(
                                    selectedSeats = selectedDetails,
                                    totalPrice = viewModel.totalPrice,
                                    maxSeats = state.maxSeats,
                                    onRemove = { info ->
                                        val seat = state.sections
                                            .flatMap { it.seats }
                                            .find { it.sessionSeatId == info.sessionSeatId }
                                        seat?.let { viewModel.toggleSeat(it) }
                                    },
                                    onPurchase = onPurchase,
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Snackbar Host (화면 상단에 오버레이) ──
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            snackbar = { data ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ChipBg.copy(alpha = 0.92f),
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        data.visuals.message,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        )
    }
}

/* ══════════════════════════════════════════
   전체화면 하단 플로팅 선택 요약 칩
   ══════════════════════════════════════════ */

@Composable
private fun FloatingSelectionChip(
    count: Int,
    maxSeats: Int,
    totalPrice: Int,
    onClick: () -> Unit,
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ChipBg,
                            Color(0xFF1E293B),
                        )
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 좌석 수 배지
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Primary, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$count",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "${count}석 선택됨",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        )
                        Text(
                            "${fmt.format(totalPrice)}원",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "상세보기",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/* ══════════════════════════════════════════
   BottomSheet 내용 (전체화면에서 칩 터치 시)
   ══════════════════════════════════════════ */

@Composable
private fun BottomSheetContent(
    selectedSeats: List<SeatMapViewModel.SelectedSeatInfo>,
    totalPrice: Int,
    maxSeats: Int,
    onRemove: (SeatMapViewModel.SelectedSeatInfo) -> Unit,
    onPurchase: () -> Unit,
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "선택한 좌석",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.1f),
            ) {
                Text(
                    "${selectedSeats.size} / $maxSeats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 좌석 목록
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            selectedSeats.forEach { info ->
                SheetSeatRow(info = info, onRemove = onRemove)
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // 합계 + 결제
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "총 ${selectedSeats.size}석",
                    fontSize = 13.sp,
                    color = MutedForeground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${fmt.format(totalPrice)}원",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                )
            }
            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(54.dp),
                contentPadding = PaddingValues(horizontal = 32.dp),
            ) {
                Text("결제하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SheetSeatRow(
    info: SeatMapViewModel.SelectedSeatInfo,
    onRemove: (SeatMapViewModel.SelectedSeatInfo) -> Unit,
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier.border(1.dp, BorderColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 구역 색상 인디케이터
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "${info.sectionName} · ${info.seatNo}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnBackground,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${info.gradeName} · ${fmt.format(info.price)}원",
                        fontSize = 13.sp,
                        color = MutedForeground,
                    )
                }
            }
            Surface(
                onClick = { onRemove(info) },
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "제거",
                        tint = MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/* ══════════════════════════════════════════
   Zoomable Canvas
   ══════════════════════════════════════════ */

private const val DEFAULT_ZOOM = 1.5f
private const val DEFAULT_FOCUS_X = CANVAS_W / 2
private const val DEFAULT_FOCUS_Y = CANVAS_H * 0.35f

@Composable
private fun ZoomableSeatCanvas(
    sections: List<SeatMapSection>,
    sectionBounds: Map<Long, SectionBounds>,
    seatPositions: Map<Long, SeatPosition>,
    selectedSeatIds: Set<Long>,
    onSeatTap: (SectionSeat) -> Unit,
) {
    var zoom by remember { mutableFloatStateOf(DEFAULT_ZOOM) }
    var offsetX by remember { mutableFloatStateOf(Float.NaN) }
    var offsetY by remember { mutableFloatStateOf(Float.NaN) }
    var isAnimating by remember { mutableStateOf(false) }
    val animScope = rememberCoroutineScope()

    val sectionColors = remember(sections) {
        sections.associate { it.sectionId to parseColor(it.colorCode) }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(CanvasBg)) {
        val viewW = with(LocalDensity.current) { maxWidth.toPx() }
        val viewH = with(LocalDensity.current) { maxHeight.toPx() }

        // 너비 기준 baseScale — 전환 시 높이만 변하므로 baseScale 불변 → 진동 없음
        val baseScale = remember(viewW) { viewW / CANVAS_W }

        var prevViewH by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(viewW, viewH) {
            if (offsetX.isNaN() || offsetY.isNaN()) {
                // 최초 진입: 기본 포커스 위치로 초기화
                val scale = baseScale * DEFAULT_ZOOM
                offsetX = viewW / 2 - DEFAULT_FOCUS_X * scale
                offsetY = viewH / 2 - DEFAULT_FOCUS_Y * scale
            } else if (prevViewH > 0f) {
                // 전체화면↔미니 전환: baseScale·zoom 불변, 세로 중심만 보정
                offsetY += (viewH - prevViewH) / 2f
            }
            prevViewH = viewH
        }

        if (offsetX.isNaN() || offsetY.isNaN()) return@BoxWithConstraints

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, gestureZoom, _ ->
                        if (isAnimating) return@detectTransformGestures
                        val newZoom = (zoom * gestureZoom).coerceIn(0.5f, 12f)
                        val zoomDelta = newZoom / zoom
                        offsetX = (offsetX - centroid.x) * zoomDelta + centroid.x + pan.x
                        offsetY = (offsetY - centroid.y) * zoomDelta + centroid.y + pan.y
                        zoom = newZoom
                    }
                }
                .pointerInput(sections, sectionBounds) {
                    detectTapGestures { tapOffset ->
                        if (isAnimating) return@detectTapGestures
                        // 너비 기준 baseScale (높이 변경 시에도 불변 → 전환 진동 방지)
                        val currentBaseScale = size.width.toFloat() / CANVAS_W
                        val scale = currentBaseScale * zoom
                        val logicalX = (tapOffset.x - offsetX) / scale
                        val logicalY = (tapOffset.y - offsetY) / scale

                        if (zoom < ZOOM_SHOW_SEATS) {
                            // ── 줌 아웃 상태: 구역 블록 탭 → 해당 구역으로 줌 ──
                            val tappedSection = sectionBounds.entries.find { (_, b) ->
                                if (b.polygon.isNotEmpty()) {
                                    pointInPolygon(logicalX, logicalY, b.polygon)
                                } else {
                                    logicalX in b.left..(b.left + b.width) &&
                                        logicalY in b.top..(b.top + b.height)
                                }
                            }
                            tappedSection?.let { (_, b) ->
                                val targetZoom = 3.5f
                                val bCx = b.left + b.width / 2
                                val bCy = b.top + b.height / 2
                                val newScale = currentBaseScale * targetZoom
                                val targetOffsetX = size.width / 2f - bCx * newScale
                                val targetOffsetY = size.height / 2f - bCy * newScale
                                animScope.launch {
                                    isAnimating = true
                                    val fromZoom = zoom
                                    val fromOffsetX = offsetX
                                    val fromOffsetY = offsetY
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 450,
                                            easing = FastOutSlowInEasing
                                        ),
                                    ) { progress, _ ->
                                        zoom = fromZoom + (targetZoom - fromZoom) * progress
                                        offsetX = fromOffsetX + (targetOffsetX - fromOffsetX) * progress
                                        offsetY = fromOffsetY + (targetOffsetY - fromOffsetY) * progress
                                    }
                                    isAnimating = false
                                }
                            }
                        } else {
                            // ── 좌석 보이는 상태: 좌석 탭 → 선택/해제 ──
                            // 중간 줌(좌석 보이지만 라벨 없음)에서는 빈 공간 탭 무시
                            val seat = findSeatAtPosition(
                                logicalX, logicalY, sections, seatPositions
                            )
                            if (seat != null) {
                                onSeatTap(seat)
                            }
                        }
                    }
                }
        ) {
            // 너비 기준 baseScale (state와 일관성 유지)
            val drawBaseScale = size.width / CANVAS_W
            val scale = drawBaseScale * zoom

            withTransform({
                translate(left = offsetX, top = offsetY)
                scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
            }) {
                drawStage()

                val showSeats = zoom >= ZOOM_SHOW_SEATS
                val showLabels = zoom >= ZOOM_SHOW_LABELS

                sections.forEach { section ->
                    val bounds = sectionBounds[section.sectionId] ?: return@forEach
                    val sectionColor = sectionColors[section.sectionId] ?: Color.Gray

                    if (!showSeats) {
                        drawSectionBlock(section, bounds, sectionColor)
                    } else {
                        drawSectionOutline(section, bounds, sectionColor)
                        drawSeats(
                            section, sectionColor,
                            selectedSeatIds, showLabels, seatPositions
                        )
                    }
                }
            }
        }
    }
}

/* ── Canvas 드로잉 함수들 ── */

private fun DrawScope.drawStage() {
    // 스테이지 본체
    val stageGradient = Brush.verticalGradient(
        colors = listOf(StageDark, StageAccent),
        startY = STAGE_TOP,
        endY = STAGE_TOP + STAGE_H,
    )
    drawRoundRect(
        brush = stageGradient,
        topLeft = Offset(STAGE_LEFT, STAGE_TOP),
        size = Size(STAGE_W, STAGE_H),
        cornerRadius = CornerRadius(16f, 16f),
    )
    // 하단 하이라이트
    drawRoundRect(
        color = Color.White.copy(alpha = 0.08f),
        topLeft = Offset(STAGE_LEFT + 2f, STAGE_TOP + STAGE_H - 20f),
        size = Size(STAGE_W - 4f, 18f),
        cornerRadius = CornerRadius(8f, 8f),
    )
    // 텍스트
    drawContext.canvas.nativeCanvas.drawText(
        "STAGE",
        STAGE_LEFT + STAGE_W / 2,
        STAGE_TOP + STAGE_H / 2 + 7f,
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 22f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
            letterSpacing = 0.15f
        }
    )
    // 스테이지 아래 반원형 데코
    val arcPath = Path().apply {
        moveTo(STAGE_LEFT + 100f, STAGE_TOP + STAGE_H)
        quadraticTo(
            STAGE_LEFT + STAGE_W / 2, STAGE_TOP + STAGE_H + 30f,
            STAGE_LEFT + STAGE_W - 100f, STAGE_TOP + STAGE_H,
        )
    }
    drawPath(arcPath, color = Color(0xFFE2E8F0).copy(alpha = 0.4f), style = Stroke(width = 1.5f))
}

private fun DrawScope.drawSectionBlock(
    section: SeatMapSection,
    bounds: SectionBounds,
    color: Color,
) {
    if (bounds.polygon.isNotEmpty()) {
        // ── 다각형 구역 ──
        val path = polygonToPath(bounds.polygon)
        drawPath(path, color = color.copy(alpha = 0.12f))
        drawPath(path, color = color.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
    } else {
        // ── 사각형 구역 (폴백) ──
        drawRoundRect(
            color = color.copy(alpha = 0.12f),
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(bounds.width, bounds.height),
            cornerRadius = CornerRadius(12f, 12f),
        )
        drawRoundRect(
            color = color.copy(alpha = 0.4f),
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(bounds.width, bounds.height),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 1.5f),
        )
    }

    val available = section.seats.count { it.status == "AVAILABLE" }
    val cx = bounds.left + bounds.width / 2
    val cy = bounds.top + bounds.height / 2

    val namePaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.parseColor(colorToHex(color))
        textSize = 18f
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD
        )
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(section.sectionName, cx, cy - 8f, namePaint)

    val countPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.parseColor("#6B7280")
        textSize = 13f
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText("잔여 ${available}석", cx, cy + 12f, countPaint)
}

private fun DrawScope.drawSectionOutline(
    section: SeatMapSection,
    bounds: SectionBounds,
    color: Color,
) {
    if (bounds.polygon.isNotEmpty()) {
        val path = polygonToPath(bounds.polygon)
        drawPath(path, color = color.copy(alpha = 0.05f))
        drawPath(path, color = color.copy(alpha = 0.2f), style = Stroke(width = 1f))
    } else {
        drawRoundRect(
            color = color.copy(alpha = 0.05f),
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(bounds.width, bounds.height),
            cornerRadius = CornerRadius(10f, 10f),
        )
        drawRoundRect(
            color = color.copy(alpha = 0.2f),
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(bounds.width, bounds.height),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 1f),
        )
    }

    // 구역 이름 라벨 — polygon의 경우 중심 상단에 표시
    val labelX = if (bounds.polygon.isNotEmpty()) {
        bounds.polygon.map { it.first }.average().toFloat()
    } else {
        bounds.left + 6f
    }
    val labelY = bounds.top + 13f

    val paint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.parseColor(colorToHex(color.copy(alpha = 0.7f)))
        textSize = 10f
        textAlign = if (bounds.polygon.isNotEmpty()) android.graphics.Paint.Align.CENTER
                    else android.graphics.Paint.Align.LEFT
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD
        )
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(
        section.sectionName,
        labelX, labelY, paint
    )
}

private fun DrawScope.drawSeats(
    section: SeatMapSection,
    sectionColor: Color,
    selectedSeatIds: Set<Long>,
    showLabels: Boolean,
    seatPositions: Map<Long, SeatPosition>,
) {
    section.seats.forEach { seat ->
        val pos = seatPositions[seat.sessionSeatId] ?: return@forEach
        val cx = pos.cx
        val cy = pos.cy
        val isSelected = seat.sessionSeatId in selectedSeatIds

        // 선택된 좌석 글로우 링
        if (isSelected) {
            drawCircle(
                color = Primary.copy(alpha = 0.25f),
                radius = SEAT_RADIUS + 4f,
                center = Offset(cx, cy),
            )
        }

        val fillColor = when {
            isSelected -> Primary
            seat.status == "AVAILABLE" -> Color.White
            seat.status == "SOLD" -> SeatSold
            seat.status == "LOCKED" -> SeatLocked
            else -> Color.LightGray
        }
        val strokeColor = when {
            isSelected -> Primary
            seat.status == "AVAILABLE" -> sectionColor.copy(alpha = 0.4f)
            seat.status == "LOCKED" -> SeatLockedBorder.copy(alpha = 0.5f)
            else -> Color(0xFFD1D5DB)
        }
        val strokeWidth = if (isSelected) 1.8f else 1f

        drawCircle(color = fillColor, radius = SEAT_RADIUS, center = Offset(cx, cy))
        drawCircle(
            color = strokeColor,
            radius = SEAT_RADIUS,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth),
        )

        // 선택된 좌석 체크 마크
        if (isSelected && !showLabels) {
            val checkPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 9f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("✓", cx, cy + 3.5f, checkPaint)
        }

        if (showLabels) {
            val paint = android.graphics.Paint().apply {
                this.color = if (isSelected) android.graphics.Color.WHITE
                else android.graphics.Color.parseColor("#4B5563")
                textSize = 6f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(seat.seatNo, cx, cy + 2.5f, paint)
        }
    }
}

/* ── 좌석 탭 hit-test ── */

private fun findSeatAtPosition(
    logicalX: Float,
    logicalY: Float,
    sections: List<SeatMapSection>,
    seatPositions: Map<Long, SeatPosition>,
): SectionSeat? {
    val hitRadiusSq = SEAT_RADIUS * SEAT_RADIUS * 3f  // ~1.7배 반지름
    for (section in sections) {
        for (seat in section.seats) {
            val pos = seatPositions[seat.sessionSeatId] ?: continue
            val dx = logicalX - pos.cx
            val dy = logicalY - pos.cy
            if (dx * dx + dy * dy <= hitRadiusSq) {
                return seat
            }
        }
    }
    return null
}

/* ── Legend Bar ── */

@Composable
private fun LegendBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendPill(color = Primary, label = "선택")
            LegendPill(color = Color.White, borderColor = MutedForeground, label = "가능")
            LegendPill(color = SeatSold, label = "판매됨")
            LegendPill(color = SeatLocked, borderColor = SeatLockedBorder, label = "잠김")
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String, borderColor: Color? = null) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF1F5F9),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
                    .then(
                        if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape)
                        else Modifier
                    )
            )
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/* ── Bottom Selection Panel (미니 모드용) ── */

@Composable
private fun BottomSelectionPanel(
    selectedSeats: List<SeatMapViewModel.SelectedSeatInfo>,
    totalPrice: Int,
    maxSeats: Int,
    onRemove: (SeatMapViewModel.SelectedSeatInfo) -> Unit,
    onPurchase: () -> Unit,
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.KOREA) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        color = Surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 선택된 좌석 가로 스크롤
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selectedSeats.forEach { info ->
                    SurfaceChip(info, onRemove)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 합계 + 결제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "총 ${selectedSeats.size}/${maxSeats}석",
                        fontSize = 12.sp,
                        color = MutedForeground,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${fmt.format(totalPrice)}원",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )
                }
                Button(
                    onClick = onPurchase,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(50.dp),
                    contentPadding = PaddingValues(horizontal = 28.dp),
                ) {
                    Text("결제하기", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SurfaceChip(
    info: SeatMapViewModel.SelectedSeatInfo,
    onRemove: (SeatMapViewModel.SelectedSeatInfo) -> Unit,
) {
    val fmt = remember { NumberFormat.getNumberInstance(Locale.KOREA) }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0FDF9),
        modifier = Modifier
            .animateContentSize()
            .border(1.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "${info.gradeName} ${info.seatNo}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground,
                )
                Text(
                    "${fmt.format(info.price)}원",
                    fontSize = 11.sp,
                    color = MutedForeground,
                )
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                onClick = { onRemove(info) },
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE2E8F0),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "제거",
                        tint = MutedForeground,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

/* ── 공연장 선택 칩 ── */

@Composable
private fun VenueSelector(
    venues: List<VenueInfo>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        venues.forEachIndexed { idx, venue ->
            val isSelected = idx == selectedIndex
            Surface(
                onClick = { onSelect(idx) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ChipBg else Color(0xFFF1F5F9),
                border = if (isSelected) null
                else androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            ) {
                Text(
                    "${venue.icon} ${venue.name}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MutedForeground,
                )
            }
        }
    }
}

/* ── Polygon 유틸리티 ── */

/** 꼭짓점 리스트 → Compose Path */
private fun polygonToPath(polygon: List<Pair<Float, Float>>): Path {
    return Path().apply {
        if (polygon.isEmpty()) return@apply
        moveTo(polygon[0].first, polygon[0].second)
        for (i in 1 until polygon.size) {
            lineTo(polygon[i].first, polygon[i].second)
        }
        close()
    }
}

/** Ray-casting point-in-polygon 테스트 */
private fun pointInPolygon(x: Float, y: Float, polygon: List<Pair<Float, Float>>): Boolean {
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].first; val yi = polygon[i].second
        val xj = polygon[j].first; val yj = polygon[j].second
        if ((yi > y) != (yj > y) &&
            x < (xj - xi) * (y - yi) / (yj - yi) + xi
        ) {
            inside = !inside
        }
        j = i
    }
    return inside
}

/* ── Utilities ── */

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color.Gray
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}
