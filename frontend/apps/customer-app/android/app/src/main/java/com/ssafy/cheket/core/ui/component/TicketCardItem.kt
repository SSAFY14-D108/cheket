package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.core.model.Ticket
import com.ssafy.cheket.ui.theme.*

@Composable
fun TicketCardItem(ticket: Ticket, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CardBg).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = ticket.poster, contentDescription = ticket.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(80.dp)
                .clip(RoundedCornerShape(8.dp)).background(Muted),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 1.dp)) {
            // Title + badge inline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    ticket.showName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = OnBackground, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                TicketStatusBadge(ticket.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${ticket.seatLabel} · ${ticket.grade}", fontSize = 12.sp, color = MutedForeground)
            Spacer(modifier = Modifier.height(2.dp))
            Text(ticket.showDate, fontSize = 12.sp, color = MutedForeground)
            Text(ticket.venue, fontSize = 12.sp, color = MutedForeground,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
