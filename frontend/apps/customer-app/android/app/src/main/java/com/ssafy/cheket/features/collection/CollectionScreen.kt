package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.*

private const val COLLECTION_BASE_URL = "https://j14d108.p.ssafy.io/collection/index.html"

@Composable
fun CollectionScreen(
    appContainer: AppContainer,
    onTicketClick: (String) -> Unit = {},
    viewModel: CollectionViewModel = viewModel(factory = CollectionViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Collection grid ──
        Scaffold(
            topBar = { AppHeader(title = "컬렉션") }
        ) { innerPadding ->
            if (uiState.usedTickets.isEmpty() && !uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmptyState(
                        "아직 컬렉션이 없어요", "공연을 관람하면 티켓이 여기에 모입니다",
                        Modifier.weight(1f)
                    )
                    // ── 테스트 버튼: 더미 데이터로 WebView 확인 ──
                    Button(
                        onClick = {
                            selectedTicket = Ticket(
                                id = "9999",
                                eventId = "1",
                                eventName = "CHEKET 테스트 콘서트",
                                eventDate = "2026-03-15",
                                venue = "SSAFY 서울캠퍼스",
                                poster = "https://picsum.photos/400/600",
                                seatId = "A-12",
                                seatLabel = "A구역 12번",
                                grade = "VIP",
                                originalPrice = 99000,
                                status = TicketStatus.USED,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.padding(bottom = 32.dp),
                    ) {
                        Text("🎫 WebView 테스트", color = White)
                    }
                }
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
                                onClick = { selectedTicket = ticket },
                            )
                        }
                    }
                }
            }
        }

        // ── Overlay modal with WebView ──
        TicketOverlay(
            ticket = selectedTicket,
            onDismiss = { selectedTicket = null },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// Full-screen WebView overlay with transparent background
// ─────────────────────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TicketOverlay(
    ticket: Ticket?,
    onDismiss: () -> Unit,
) {
    val visible = ticket != null

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(250)),
    ) {
        var isLoading by remember { mutableStateOf(true) }
        val url = ticket?.let {
            Uri.parse(COLLECTION_BASE_URL)
                .buildUpon()
                .appendQueryParameter("name", it.eventName)
                .appendQueryParameter("date", it.eventDate)
                .appendQueryParameter("venue", it.venue)
                .appendQueryParameter("seat", it.seatLabel)
                .appendQueryParameter("grade", it.grade)
                .appendQueryParameter("poster", it.poster)
                .appendQueryParameter("id", it.id)
                .appendQueryParameter("price", it.originalPrice.toString())
                .build()
                .toString()
        } ?: ""

        Box(modifier = Modifier.fillMaxSize()) {
            // WebView (HTML 자체가 반투명 배경 + 센터링 처리)
            if (ticket != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            setBackgroundColor(AndroidColor.BLACK)

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                    Log.d("CollectionWV", "[JS] ${cm?.message()} (${cm?.sourceId()}:${cm?.lineNumber()})")
                                    return true
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    Log.d("CollectionWV", "▶ onPageStarted: $url")
                                }

                                override fun onPageFinished(view: WebView?, u: String?) {
                                    Log.d("CollectionWV", "✅ onPageFinished: $u")

                                    // 서버 HTML의 body height 0 문제 + 센터링 런타임 수정
                                    view?.evaluateJavascript(
                                        """
                                        document.documentElement.style.height='100vh';
                                        document.body.style.height='100vh';
                                        document.body.style.overflow='visible';
                                        var s=document.querySelector('.scene');
                                        if(s){
                                          s.style.position='fixed';
                                          s.style.top='50%';
                                          s.style.left='50%';
                                          s.style.transform='translate(-50%,-50%)';
                                          s.style.opacity='1';
                                        }
                                        """.trimIndent(),
                                        null,
                                    )

                                    // DOM 디버그
                                    view?.evaluateJavascript(
                                        """
                                        (function(){
                                          var s=document.querySelector('.scene');
                                          var cf=document.querySelector('.card-flip');
                                          var b=document.body;
                                          var poster=document.querySelector('.poster');
                                          var holo=document.querySelector('.holo-layer');
                                          return 'scene='+(!s?'null':(s.offsetWidth+'x'+s.offsetHeight))
                                            +' cardFlip='+(!cf?'null':(cf.offsetWidth+'x'+cf.offsetHeight))
                                            +' bodyW='+b.offsetWidth+' bodyH='+b.offsetHeight
                                            +' vpH='+window.innerHeight
                                            +' holo='+(!holo?'null':(holo.offsetWidth+'x'+holo.offsetHeight))
                                            +' poster='+(!poster?'null':(poster.naturalWidth+'x'+poster.naturalHeight+' loaded='+poster.complete))
                                            +' title='+document.title;
                                        })()
                                        """.trimIndent(),
                                    ) { result ->
                                        Log.d("CollectionWV", "🔍 DOM: $result")
                                    }

                                    isLoading = false
                                }

                                override fun onReceivedError(
                                    view: WebView?, request: WebResourceRequest?,
                                    error: WebResourceError?,
                                ) {
                                    Log.e("CollectionWV", "❌ Error: ${error?.description} (${error?.errorCode}) url=${request?.url}")
                                }
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                useWideViewPort = true
                                loadWithOverviewMode = true
                            }
                            Log.d("CollectionWV", "🚀 loadUrl: $url")
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
