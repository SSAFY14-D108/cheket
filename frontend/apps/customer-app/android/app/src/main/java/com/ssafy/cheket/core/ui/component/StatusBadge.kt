package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.model.TicketStatus
import com.ssafy.cheket.ui.theme.BadgeExpired
import com.ssafy.cheket.ui.theme.BadgeListed
import com.ssafy.cheket.ui.theme.BadgeSold
import com.ssafy.cheket.ui.theme.BadgeUsed
import com.ssafy.cheket.ui.theme.StatusOnSale
import com.ssafy.cheket.ui.theme.StatusSoldOut

// v0 gradient border brush for badges
private val BadgeGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF5E2DAFF.toInt()),  // rgba(226,218,255,0.96)
        Color(0xFFF0C4F7E0.toInt()),  // rgba(196,247,224,0.92)
        Color(0xFFF5CAE6FF.toInt()),  // rgba(202,230,255,0.96)
    ),
)

private val PillShape = RoundedCornerShape(50)

@Composable
fun TicketStatusBadge(status: TicketStatus) {
    val (textColor, shadowColor, label) = when (status) {
        TicketStatus.AVAILABLE -> Triple(BadgeSold, BadgeSold.copy(alpha = 0.12f), "보유중")
        TicketStatus.LISTED -> Triple(BadgeListed, BadgeListed.copy(alpha = 0.12f), "판매중")
        TicketStatus.SOLD -> Triple(BadgeExpired, BadgeExpired.copy(alpha = 0.12f), "판매완료")
        TicketStatus.USED -> Triple(BadgeUsed, BadgeUsed.copy(alpha = 0.10f), "사용완료")
        TicketStatus.EXPIRED -> Triple(BadgeExpired, BadgeExpired.copy(alpha = 0.12f), "만료")
    }
    StatusLabel(textColor = textColor, shadowColor = shadowColor, label = label)
}

@Composable
fun ShowStatusBadge(status: ShowStatus) {
    val (textColor, shadowColor, label) = when (status) {
        ShowStatus.ON_SALE -> Triple(StatusOnSale, StatusOnSale.copy(alpha = 0.12f), "판매중")
        ShowStatus.SOLD_OUT -> Triple(StatusSoldOut, StatusSoldOut.copy(alpha = 0.12f), "매진")
    }
    StatusLabel(textColor = textColor, shadowColor = shadowColor, label = label)
}

@Composable
private fun StatusLabel(textColor: Color, shadowColor: Color, label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = PillShape,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(PillShape)
            .border(width = 1.dp, brush = BadgeGradientBrush, shape = PillShape)
            .background(Color(0xFFFFFFFB))  // near-white matching v0
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
