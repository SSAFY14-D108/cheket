package com.ssafy.cheket.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.ssafy.cheket.ui.theme.Muted
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.PrimaryLight

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
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = show.poster,
            contentDescription = show.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(92.dp)
                .height(122.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Muted),
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val listBadgeStatus = when (show.status) {
                ShowStatus.UPCOMING,
                ShowStatus.COMPLETED,
                -> show.status
                else -> null
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = show.name,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                listBadgeStatus?.let { ShowStatusBadge(it) }
            }

            show.artistName
                ?.takeIf { it.isNotBlank() }
                ?.let { artistName ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryLight)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = artistName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                MetadataRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = MutedForeground,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = show.date,
                )
                MetadataRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            tint = MutedForeground,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = locationText,
                )
            }
        }
    }
}

@Composable
private fun MetadataRow(
    icon: @Composable () -> Unit,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon()
        Text(
            text = text,
            fontSize = 13.sp,
            color = MutedForeground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
