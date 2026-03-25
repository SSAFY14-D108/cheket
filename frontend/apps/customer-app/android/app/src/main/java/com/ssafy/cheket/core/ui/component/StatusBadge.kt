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
import com.ssafy.cheket.ui.theme.BadgeUsed
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.PrimaryLight

private val PillShape = RoundedCornerShape(50)

@Composable
fun TicketStatusBadge(status: TicketStatus) {
    val (containerColor, textColor, label) = when (status) {
        TicketStatus.AVAILABLE -> Triple(PrimaryLight, Primary, "사용 가능")
        TicketStatus.LISTED -> Triple(Color(0xFFFFF2E8), BadgeListed, "판매 중")
        TicketStatus.SOLD -> Triple(Color(0xFFFFECEC), BadgeExpired, "판매 완료")
        TicketStatus.USED -> Triple(Color(0xFFF0F3F6), BadgeUsed, "사용 완료")
        TicketStatus.EXPIRED -> Triple(Color(0xFFF5F5F5), Color(0xFF6B7280), "기간 만료")
    }
    StatusLabel(containerColor = containerColor, textColor = textColor, label = label)
}

@Composable
fun ShowStatusBadge(status: ShowStatus) {
    val (containerColor, textColor, label) = when (status) {
        ShowStatus.UPCOMING -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), "예매 예정")
        ShowStatus.ON_SALE -> Triple(Color(0xFFF4F4F5), Color(0xFF111827), "예매중")
        ShowStatus.SOLD_OUT -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), "매진")
        ShowStatus.COMPLETED -> Triple(Color(0xFFF5F5F5), Color(0xFF9CA3AF), "종료")
    }
    StatusLabel(containerColor = containerColor, textColor = textColor, label = label)
}

@Composable
private fun StatusLabel(
    containerColor: Color,
    textColor: Color,
    label: String,
) {
    Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(PillShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
