package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MusicNote
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
import com.ssafy.cheket.core.util.DateTimeUtils
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground

private val V0Foreground = Color(0xFF111111)
private val V0SeatText = Color(0xFF333333)
private val PosterHeight = 122.dp

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
        Box(
            modifier = Modifier
                .width(92.dp)
                .height(PosterHeight)
                .clip(RoundedCornerShape(14.dp))
                .background(Muted),
            contentAlignment = Alignment.Center,
        ) {
            if (ticket.poster.isNotBlank()) {
                AsyncImage(
                    model = ticket.poster,
                    contentDescription = ticket.showName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(PosterHeight)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
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

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${ticket.seatLabel} · ${ticket.grade}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = V0SeatText,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MutedForeground,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = DateTimeUtils.formatShowDateTime(ticket.showDate),
                        fontSize = 12.sp,
                        color = MutedForeground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
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
}
