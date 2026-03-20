package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
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
import com.ssafy.cheket.core.model.Show
import com.ssafy.cheket.core.model.ShowStatus
import com.ssafy.cheket.ui.theme.*

@Composable
fun ShowCardItem(show: Show, onClick: () -> Unit = {}) {
    val locationText = buildString {
        append(show.venue)
        if (show.region.isNotBlank() && !show.venue.contains(show.region)) {
            append(", ")
            append(show.region)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .elevatedSurfaceSoft(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = show.poster, contentDescription = show.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(84.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Muted),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = show.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                if (show.status == ShowStatus.SOLD_OUT) {
                    ShowStatusBadge(show.status)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = show.date,
                    fontSize = 14.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MutedForeground,
                    modifier = Modifier.size(17.dp),
                )
                Text(
                    text = locationText,
                    fontSize = 14.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
