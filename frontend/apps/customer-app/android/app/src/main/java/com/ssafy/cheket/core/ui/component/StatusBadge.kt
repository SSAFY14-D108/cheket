package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun TicketStatusBadge(status: TicketStatus) {
    val (bg, text, label) = when (status) {
        TicketStatus.AVAILABLE -> Triple(BadgeSold.copy(alpha = 0.15f), BadgeSold, "보유중")
        TicketStatus.LISTED -> Triple(BadgeListed.copy(alpha = 0.15f), BadgeListed, "판매중")
        TicketStatus.SOLD -> Triple(BadgeExpired.copy(alpha = 0.15f), BadgeExpired, "판매완료")
        TicketStatus.USED -> Triple(BadgeUsed.copy(alpha = 0.15f), BadgeUsed, "사용완료")
        TicketStatus.EXPIRED -> Triple(BadgeExpired.copy(alpha = 0.15f), BadgeExpired, "만료")
    }
    StatusLabel(bg = bg, text = text, label = label)
}

@Composable
fun ShowStatusBadge(status: ShowStatus) {
    val (bg, text, label) = when (status) {
        ShowStatus.ON_SALE -> Triple(StatusOnSale.copy(alpha = 0.15f), StatusOnSale, "판매중")
        ShowStatus.SOLD_OUT -> Triple(StatusSoldOut.copy(alpha = 0.15f), StatusSoldOut, "매진")
    }
    StatusLabel(bg = bg, text = text, label = label)
}

@Composable
private fun StatusLabel(bg: Color, text: Color, label: String) {
    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
