package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ssafy.cheket.AppContainer
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

private const val COLLECTION_BASE_URL = "http://10.0.2.2:3000/collection/"

@Composable
fun CollectionScreen(
    appContainer: AppContainer,
    onTicketClick: (String) -> Unit = {},
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTicketId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Collection grid ──
        Scaffold(
            topBar = { AppHeader(title = "컬렉션") }
        ) { innerPadding ->
            if (uiState.usedTickets.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    "아직 컬렉션이 없어요", "공연을 관람하면 티켓이 여기에 모입니다",
                    Modifier.fillMaxSize().padding(innerPadding)
                )
            } else {
                Column(Modifier.fillMaxSize().background(Background).padding(innerPadding)) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Muted,
                        ) {
                            Text(
                                "${uiState.usedTickets.size}장",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedForeground,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "관람한 공연의 소장 티켓을 모아보세요. 카드를 탭하면 상세 정보를 볼 수 있어요.",
                            fontSize = 12.sp, color = MutedForeground, lineHeight = 18.sp,
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(uiState.usedTickets) { index, ticket ->
                            CompactTicketCard(
                                ticket = ticket,
                                isGold = index == 0,
                                onClick = { selectedTicketId = ticket.id },
                            )
                        }
                    }
                }
            }
        }

        // ── Overlay modal with WebView ──
        TicketOverlay(
            ticketId = selectedTicketId,
            onDismiss = { selectedTicketId = null },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Full-screen transparent WebView overlay
// WebView 배경 투명 → 웹 페이지가 자체적으로 반투명 배경 + 3D 티켓 렌더링
// ─────────────────────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TicketOverlay(
    ticketId: String?,
    onDismiss: () -> Unit,
) {
    val visible = ticketId != null

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(250)),
    ) {
        var isLoading by remember { mutableStateOf(true) }
        val url = ticketId?.let { "$COLLECTION_BASE_URL$it" } ?: ""

        Box(modifier = Modifier.fillMaxSize()) {
            // 반투명 배경 (WebView 로드 전에도 보임)
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )

            // 전체화면 투명 WebView
            if (ticketId != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            // WebView 자체 배경 투명
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, u: String?) {
                                    // 웹 페이지 html/body 배경도 투명으로
                                    view?.evaluateJavascript(
                                        """
                                        document.documentElement.style.background='transparent';
                                        document.body.style.background='transparent';
                                        """.trimIndent(),
                                        null,
                                    )
                                    isLoading = false
                                }
                            }
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // 로딩 스피너
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(tween(400)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp),
                )
            }

            // 닫기 버튼 (우상단)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.15f)),
            ) {
                Icon(Icons.Default.Close, "닫기", tint = White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Compact ticket card for the grid
// ─────────────────────────────────────────────────────────────────────
@Composable
private fun CompactTicketCard(ticket: Ticket, isGold: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.59f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .then(if (isGold) Modifier.border(1.5.dp, GoldColor, RoundedCornerShape(10.dp)) else Modifier)
    ) {
        AsyncImage(
            ticket.poster, ticket.eventName,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isGold) Color(0x30D5B45A) else Color.Transparent,
                            Color(0xDD0B0F1A)
                        ),
                        startY = 60f,
                    )
                )
        )
        if (isGold) {
            Box(Modifier.align(Alignment.TopEnd).padding(6.dp)) {
                Icon(Icons.Outlined.Star, "Gold", tint = GoldColor, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            "CONCERT TICKET", fontSize = 7.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold,
            color = White.copy(alpha = 0.8f), modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        )
        Column(Modifier.align(Alignment.BottomStart).padding(8.dp)) {
            Text(
                "CHEKET", fontSize = if (isGold) 18.sp else 14.sp, fontWeight = FontWeight.Black,
                color = if (isGold) GoldColor else White, letterSpacing = (-0.5).sp
            )
            Text(
                ticket.eventName, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                color = White.copy(alpha = 0.9f),
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 11.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "No.${ticket.id.takeLast(4).padStart(4, '0')}", fontSize = 7.sp,
                color = White.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace
            )
        }
    }
}
