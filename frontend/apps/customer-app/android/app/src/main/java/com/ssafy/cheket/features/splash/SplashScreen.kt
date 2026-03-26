package com.ssafy.cheket.features.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.R
import com.ssafy.cheket.ui.theme.Background
import kotlinx.coroutines.delay

private val SplashBg = Background
private val GlowGreen = Color(0xFF6C7FFF)
private val GlowCyan = Color(0xFFD7E3FF)
private val SubtitleColor = Color(0xFF9CA3AF)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
) {
    // Phase 0: 초기 상태
    // Phase 1: "C" 아이콘 바운스 등장 (0~700ms)
    // Phase 2: "C" 아이콘 축소+회전+페이드아웃 → CHEKET 로고 등장 (700~1200ms)
    // Phase 3: 서브타이틀 + 글로우 바 (1200~2200ms)
    // 완료: onSplashFinished()

    var phase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        phase = 1  // C 아이콘 등장
        delay(800)
        phase = 2  // C → CHEKET 전환
        delay(500)
        phase = 3  // 서브타이틀 등장
        delay(900)
        onSplashFinished()
    }

    // ── "C" 아이콘 애니메이션 ──
    val cScale by animateFloatAsState(
        targetValue = when (phase) {
            0 -> 0.2f
            1 -> 1f
            else -> 0.3f
        },
        animationSpec = when (phase) {
            1 -> spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            else -> tween(350, easing = EaseInBack)
        },
        label = "cScale",
    )

    val cAlpha by animateFloatAsState(
        targetValue = if (phase <= 1) 1f else 0f,
        animationSpec = tween(300),
        label = "cAlpha",
    )

    val cRotation by animateFloatAsState(
        targetValue = if (phase >= 2) 360f else 0f,
        animationSpec = tween(400, easing = EaseInOutCubic),
        label = "cRotation",
    )

    // ── CHEKET 로고 애니메이션 ──
    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "logoScale",
    )

    // 가로 방향 스프링 (0 → 1, 탄성 있게 늘어남)
    val logoScaleX by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "logoScaleX",
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(if (phase >= 2) 400 else 0, delayMillis = if (phase >= 2) 150 else 0),
        label = "logoAlpha",
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(400),
        label = "subtitleAlpha",
    )

    // ── 하단 글로우 바 ──
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart,
        ),
        label = "glowOffset",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center,
    ) {
        // "C" 아이콘 (Phase 1→2에서 사라짐)
        if (cAlpha > 0.01f) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .scale(cScale)
                    .alpha(cAlpha)
                    .rotate(cRotation)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
            )
        }

        // CHEKET 로고 + 서브타이틀 (Phase 2→3에서 등장)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(logoAlpha)
                .scale(logoScale),
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_cheket),
                contentDescription = "Cheket Logo",
                modifier = Modifier
                    .width(260.dp)
                    .graphicsLayer(scaleX = logoScaleX),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "블록체인 기반 티켓팅 플랫폼",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SubtitleColor,
                modifier = Modifier.alpha(subtitleAlpha),
            )
        }

        // 하단 글로우 라인 (Phase 2부터)
        if (phase >= 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .alpha(logoAlpha)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GlowGreen.copy(alpha = 0.5f),
                                GlowCyan.copy(alpha = 0.7f),
                                GlowGreen.copy(alpha = 0.5f),
                                Color.Transparent,
                            ),
                            startX = glowOffset * 1000f,
                            endX = glowOffset * 1000f + 600f,
                        )
                    ),
            )
        }
    }
}
