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
import com.ssafy.cheket.core.model.Event
import com.ssafy.cheket.core.model.EventStatus
import com.ssafy.cheket.ui.theme.*

@Composable
fun EventCardItem(event: Event, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CardBg).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = event.poster, contentDescription = event.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(84.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(8.dp)).background(Muted),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            // Title + badge inline (badge only for SOLD_OUT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    event.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = OnBackground, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                )
                if (event.status == EventStatus.SOLD_OUT) {
                    EventStatusBadge(event.status)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(event.date, fontSize = 12.sp, color = MutedForeground)
            Text(event.venue, fontSize = 12.sp, color = MutedForeground,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
