package com.ssafy.cheket.features.notification

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.network.dto.HostShowDetailDto
import com.ssafy.cheket.core.network.dto.StakeholderDto
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.core.util.DateTimeUtils
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

private const val TAG = "ContractApproval"

@Composable
fun ContractApprovalScreen(
    showId: Long,
    requestType: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val showService = remember { (context.applicationContext as CheketApplication).appContainer.showService }
    val scope = rememberCoroutineScope()

    var isApproving by remember { mutableStateOf(false) }
    var isRejecting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showRejectDialog by remember { mutableStateOf(false) }

    // 공연 상세 전체
    var showDetail by remember { mutableStateOf<HostShowDetailDto?>(null) }
    var isLoadingShow by remember { mutableStateOf(true) }

    // 내 계약 승인/거절 상태
    var myApprovalStatus by remember { mutableStateOf<String?>(null) }

    val app = remember { context.applicationContext as CheketApplication }
    val userService = remember { app.appContainer.userService }
    var currentUserId by remember { mutableStateOf(app.authDataStore.getUserId()) }

    LaunchedEffect(showId) {
        // userId가 없으면 API로 가져오기
        if (currentUserId == null) {
            try {
                val userResponse = userService.getUserInfo()
                userResponse.data?.let {
                    currentUserId = it.userId
                    app.authDataStore.saveUserId(it.userId)
                    Log.d(TAG, "Fetched userId=${it.userId}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch userId", e)
            }
        }

        // 공연 정보 로딩
        try {
            val response = showService.getHostShowDetail(showId)
            showDetail = response.data
            Log.d(TAG, "Show loaded: ${response.data?.title}, stakeholders=${response.data?.stakeholders?.size}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load show detail", e)
        }

        // 계약 승인/거절 내역 조회
        try {
            val contractResponse = showService.getContractApprovals(showId)
            val approvals = contractResponse.data ?: emptyList()
            val myApproval = approvals.find { it.userId == currentUserId }
            myApprovalStatus = myApproval?.approvalStatus ?: "PENDING"
            Log.d(TAG, "Contract status: $myApprovalStatus (userId=$currentUserId, match=${myApproval != null})")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load approvals", e)
            myApprovalStatus = "PENDING"
        }

        isLoadingShow = false
    }

    val title = if (requestType == "RQ_CREATE") "공연 등록 승인 요청" else "공연 수정 승인 요청"
    val detail = showDetail

    Scaffold(
        topBar = { AppHeader(title = title, onBack = onBack) },
    ) { innerPadding ->
        if (isLoadingShow) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── 공연 기본 정보 카드 ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // 포스터
                        if (detail?.posterUrl != null) {
                            coil.compose.AsyncImage(
                                model = detail.posterUrl,
                                contentDescription = detail.title,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }

                        // 제목
                        Text(
                            text = detail?.title ?: "공연 #$showId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                        )

                        // 아티스트
                        if (!detail?.artist.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Person, null, tint = Primary, modifier = Modifier.size(16.dp))
                                Text(detail!!.artist!!, fontSize = 14.sp, color = OnBackground)
                            }
                        }

                        // 장소
                        if (detail?.venue != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.LocationOn, null, tint = Primary, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(detail.venue.name ?: "", fontSize = 14.sp, color = OnBackground)
                                    if (!detail.venue.address.isNullOrBlank()) {
                                        Text(detail.venue.address, fontSize = 12.sp, color = MutedForeground)
                                    }
                                }
                            }
                        }

                        // 공연 기간
                        if (detail?.show != null && detail.show.showStartDate != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.CalendarMonth, null, tint = Primary, modifier = Modifier.size(16.dp))
                                val start = DateTimeUtils.formatDate(detail.show.showStartDate)
                                val end = detail.show.showEndDate?.let { DateTimeUtils.formatDate(it) } ?: start
                                Text(
                                    text = if (start == end) start else "$start ~ $end",
                                    fontSize = 14.sp,
                                    color = OnBackground,
                                )
                            }
                        }

                        // 설명
                        if (!detail?.description.isNullOrBlank()) {
                            HorizontalDivider(color = BorderColor)
                            Text(
                                text = detail!!.description!!,
                                fontSize = 13.sp,
                                color = MutedForeground,
                                lineHeight = 20.sp,
                            )
                        }

                        // 최종 수정일
                        if (!detail?.updatedAt.isNullOrBlank()) {
                            Text(
                                text = "최종 수정: ${DateTimeUtils.formatDateTime(detail!!.updatedAt!!)}",
                                fontSize = 11.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }

                // ── 수익 분배 계약 ──
                val stakeholders = detail?.stakeholders ?: emptyList()
                if (stakeholders.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("수익 분배 계약", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnBackground)
                            HorizontalDivider(color = BorderColor)

                            // 사람 stakeholders (id가 있는 것)
                            stakeholders.filter { it.id != null }.forEach { sh ->
                                val isMe = sh.role == "ARTIST" && sh.id?.toLong() == currentUserId
                                StakeholderRow(sh, isMe)
                            }

                            // 수수료 (id가 null인 것) — 별도 UI
                            stakeholders.filter { it.id == null }.forEach { sh ->
                                PlatformFeeRow(sh)
                            }

                            HorizontalDivider(color = BorderColor)
                            // 합계
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("합계", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                                Text(
                                    text = formatBps(stakeholders.sumOf { it.shareBps }),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackground,
                                )
                            }
                        }
                    }
                }

                // ── 결과 메시지 ──
                resultMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.contains("승인")) PrimaryLight else Danger.copy(alpha = 0.1f)
                        ),
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (msg.contains("승인")) Primary else Danger,
                        )
                    }
                }

                // ── 이미 승인/거절한 경우 ──
                if (myApprovalStatus == "APPROVED" && resultMessage == null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryLight),
                    ) {
                        Text("계약을 승인했습니다.", Modifier.padding(16.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary)
                    }
                } else if (myApprovalStatus == "REJECTED" && resultMessage == null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Danger.copy(alpha = 0.1f)),
                    ) {
                        Text("계약을 거절했습니다.", Modifier.padding(16.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Danger)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── 버튼 ──
                if (resultMessage == null && myApprovalStatus == "PENDING") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isApproving && !isRejecting,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        ) {
                            if (isRejecting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Danger)
                            else Text("거절", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Button(
                            onClick = {
                                isApproving = true
                                scope.launch {
                                    try {
                                        showService.approveContract(showId)
                                        resultMessage = "계약이 승인되었습니다."
                                    } catch (e: Exception) {
                                        resultMessage = "승인 실패: ${e.message}"
                                    }
                                    isApproving = false
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isApproving && !isRejecting,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        ) {
                            if (isApproving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = White)
                            else Text("승인", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                        }
                    }
                } else {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        Text("확인", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                    }
                }
            }
        }
    }

    // 거절 다이얼로그
    if (showRejectDialog) {
        com.ssafy.cheket.core.ui.component.CheketDialog(
            title = "계약 거절",
            message = "정말로 이 계약을 거절하시겠습니까?\n거절 후에는 재승인이 필요합니다.",
            confirmText = "거절",
            dismissText = "취소",
            onConfirm = {
                showRejectDialog = false
                isRejecting = true
                scope.launch {
                    try {
                        showService.rejectContract(showId)
                        resultMessage = "계약이 거절되었습니다."
                    } catch (e: Exception) {
                        resultMessage = "거절 실패: ${e.message}"
                    }
                    isRejecting = false
                }
            },
            onDismiss = { showRejectDialog = false },
            isDanger = true,
        )
    }
}

// ── 사람 Stakeholder Row ──
@Composable
private fun StakeholderRow(sh: StakeholderDto, isMe: Boolean) {
    val roleLabel = when (sh.role) {
        "ORGANIZER" -> "기획사"
        "ARTIST" -> "아티스트"
        else -> sh.role
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isMe) Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Primary, RoundedCornerShape(10.dp))
                    .background(PrimaryLight)
                    .padding(12.dp)
                else Modifier.padding(vertical = 4.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = sh.name ?: "(미지정)",
                    fontSize = 15.sp,
                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                    color = if (isMe) Primary else OnBackground,
                )
                if (isMe) {
                    Text(
                        text = "(본인)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
            Text(roleLabel, fontSize = 12.sp, color = MutedForeground)
        }
        Text(
            text = formatBps(sh.shareBps),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMe) Primary else OnBackground,
        )
    }
}

// ── 플랫폼 수수료 Row (id null) ──
@Composable
private fun PlatformFeeRow(sh: StakeholderDto) {
    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("플랫폼 수수료", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MutedForeground)
        Text(
            text = formatBps(sh.shareBps),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedForeground,
        )
    }
}

private fun formatBps(bps: Int): String {
    val whole = bps / 100
    val frac = bps % 100
    return if (frac == 0) "${whole}%" else "${whole}.${frac}%"
}
