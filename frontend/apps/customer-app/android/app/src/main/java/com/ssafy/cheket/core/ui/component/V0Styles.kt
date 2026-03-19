package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * v0 `.elevated-surface` — box-shadow: 0 10px 30px rgba(15,23,42,0.045)
 */
fun Modifier.elevatedSurface(
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
): Modifier = this
    .shadow(
        elevation = 6.dp,
        shape = shape,
        ambientColor = Color(0x0B0F172A),
        spotColor = Color(0x0B0F172A),
    )
    .background(Color.White, shape)

/**
 * v0 `.elevated-surface-soft` — box-shadow: 0 6px 20px rgba(15,23,42,0.03)
 */
fun Modifier.elevatedSurfaceSoft(
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
): Modifier = this
    .shadow(
        elevation = 3.dp,
        shape = shape,
        ambientColor = Color(0x080F172A),
        spotColor = Color(0x080F172A),
    )
    .background(Color.White, shape)

/**
 * v0 `gradient-border` 효과 — 이중 선형 그래디언트 테두리
 * 버튼, 카드 등에 사용
 */
fun Modifier.gradientBorder(
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    borderWidth: Dp = 1.5.dp,
): Modifier = this
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFB8E6D8),  // 민트
                Color(0xFFD4C8F0),  // 라벤더
                Color(0xFFB8D8E8),  // 하늘
            ),
        ),
        shape = shape,
    )
