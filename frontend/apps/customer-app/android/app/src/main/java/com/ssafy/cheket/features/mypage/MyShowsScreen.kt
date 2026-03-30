package com.ssafy.cheket.features.mypage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.MyShowDto
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.ui.component.EmptyState
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.BorderColor
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary
import com.ssafy.cheket.ui.theme.White

private const val TAG = "MyShowsScreen"

@Composable
fun MyShowsScreen(
    onShowClick: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as CheketApplication }
    val userService = remember { app.appContainer.userService }

    var isLoading by remember { mutableStateOf(true) }
    var shows by remember { mutableStateOf<List<MyShowDto>>(emptyList()) }
    var useMock by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = userService.getMyShows()
            if (response.httpStatusCode in 200..299 && response.data != null) {
                shows = response.data
                Log.d(TAG, "Loaded ${shows.size} shows from API")
            } else {
                Log.w(TAG, "API returned ${response.httpStatusCode}, using mock")
                useMock = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "API not available, using mock", e)
            useMock = true
        }

        if (useMock) {
            shows = mockMyShows()
        }
        isLoading = false
    }

    Scaffold(
        topBar = { AppHeader(title = "참여 공연", onBack = onBack) },
        containerColor = Background,
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                }
            }

            shows.isEmpty() -> {
                EmptyState(
                    title = "참여한 공연이 없어요",
                    description = "아티스트로 참여한 공연이 여기에 표시됩니다.",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                ) {
                    items(shows, key = { it.showId }) { show ->
                        MyShowCard(
                            show = show,
                            onClick = { onShowClick(show.showId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyShowCard(
    show: MyShowDto,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 포스터
        AsyncImage(
            model = show.posterUrl,
            contentDescription = show.title,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BorderColor),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = show.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            show.artist?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MutedForeground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val dateText = formatShowDateRange(show.showStartDate, show.showEndDate)
            if (dateText.isNotBlank()) {
                Text(
                    text = dateText,
                    fontSize = 12.sp,
                    color = MutedForeground,
                )
            }
        }
    }
}

private fun formatShowDateRange(start: String?, end: String?): String {
    if (start == null) return ""
    val startDate = start.substringBefore("T")
    val endDate = end?.substringBefore("T")
    return if (endDate != null && startDate != endDate) {
        "$startDate ~ $endDate"
    } else {
        startDate
    }
}

private fun mockMyShows() = listOf(
    MyShowDto(
        showId = 1,
        title = "CHEKET 콘서트",
        artist = "CHEKET BAND",
        posterUrl = null,
        showStartDate = "2026-04-01T19:00:00",
        showEndDate = "2026-04-03T22:00:00",
    ),
    MyShowDto(
        showId = 2,
        title = "봄 페스티벌 2026",
        artist = "SSAFY MUSIC",
        posterUrl = null,
        showStartDate = "2026-05-10T18:00:00",
        showEndDate = "2026-05-10T21:00:00",
    ),
)
