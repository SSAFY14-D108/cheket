package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.ui.theme.*

private val V0Foreground = Color(0xFF111111)
private val V0SeatText = Color(0xFF333333)

@Composable
fun TicketCardItem(ticket: Ticket, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Poster — h-20 w-20 rounded-lg (80x80)
        AsyncImage(
            model = ticket.poster,
            contentDescription = ticket.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Muted),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 2.dp),
        ) {
            // Title + status badge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = ticket.showName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = V0Foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                TicketStatusBadge(ticket.status)
            }

            Spacer(Modifier.height(4.dp))

            // Seat · Grade
            Text(
                text = "${ticket.seatLabel} · ${ticket.grade}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = V0SeatText,
            )

            Spacer(Modifier.height(4.dp))

            // Date row with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = com.ssafy.cheket.core.util.DateTimeUtils.formatShowDateTime(ticket.showDate),
                    fontSize = 12.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(2.dp))

            // Venue row with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = ticket.venue,
                    fontSize = 12.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
