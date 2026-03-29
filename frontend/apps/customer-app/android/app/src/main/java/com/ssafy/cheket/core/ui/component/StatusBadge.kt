package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.core.model.TicketStatus

private val TicketBadgeShape = RoundedCornerShape(9.dp)
private val ShowBadgeShape = RoundedCornerShape(10.dp)
private val TicketBadgeText = Color(0xFF344054)

@Composable
fun TicketStatusBadge(status: TicketStatus) {
    val (dotColor, label) = when (status) {
        TicketStatus.AVAILABLE -> Color(0xFF98A2FF) to "사용 가능"
        TicketStatus.LISTED -> Color(0xFFF04438) to "판매 중"
        TicketStatus.SOLD -> Color(0xFFD0D5DD) to "판매 완료"
        TicketStatus.USED -> Color(0xFFB8C0CC) to "사용 완료"
        TicketStatus.EXPIRED -> Color(0xFFE5E7EB) to "기간 만료"
    }
    TicketStatusLabel(dotColor = dotColor, label = label)
}

@Composable
fun ShowStatusBadge(status: ShowStatus) {
    val (containerColor, textColor, label) = when (status) {
        ShowStatus.UPCOMING -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), "예매 예정")
        ShowStatus.ON_SALE -> Triple(Color(0xFFF4F4F5), Color(0xFF111827), "예매중")
        ShowStatus.SOLD_OUT -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), "매진")
        ShowStatus.COMPLETED -> Triple(Color(0xFFF5F5F5), Color(0xFF9CA3AF), "종료")
    }
    ShowStatusLabel(containerColor = containerColor, textColor = textColor, label = label)
}

@Composable
private fun TicketStatusLabel(
    dotColor: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TicketBadgeText,
        )
    }
}

@Composable
private fun ShowStatusLabel(
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
            .clip(ShowBadgeShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
