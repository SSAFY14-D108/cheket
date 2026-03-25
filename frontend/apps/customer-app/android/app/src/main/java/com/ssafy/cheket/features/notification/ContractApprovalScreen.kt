package com.ssafy.cheket.features.notification

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.*
import kotlinx.coroutines.launch

private const val TAG = "ContractApproval"

/**
 * 공연 등록/수정 승인 요청에 대한 계약 승인/거절 화면.
 * RQ_CREATE, RQ_UPDATE 알림에서 진입.
 *
 * TODO: 계약 세부 내용 API 연동 (수익 분배 비율, 스테이크홀더 목록 등)
 */
@Composable
fun ContractApprovalScreen(
    showId: Long,
    requestType: String, // "RQ_CREATE" or "RQ_UPDATE"
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

    val title = if (requestType == "RQ_CREATE") "공연 등록 승인 요청" else "공연 수정 승인 요청"

    Scaffold(
        topBar = { AppHeader(title = title, onBack = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 계약 정보 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "공연 ID: $showId",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                    )
                    Text(
                        text = if (requestType == "RQ_CREATE")
                            "새 공연 등록에 대한 스마트 컨트랙트 승인이 요청되었습니다."
                        else
                            "공연 정보 수정에 대한 스마트 컨트랙트 승인이 요청되었습니다.",
                        fontSize = 14.sp,
                        color = MutedForeground,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }

            // TODO: 계약 세부 내용 (수익 분배 비율, 스테이크홀더 목록 등)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Muted),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("계약 세부 내용", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnBackground)
                    Text(
                        "계약 세부 내용 API 연동 예정\n(수익 분배 비율, 스테이크홀더 목록 등)",
                        fontSize = 13.sp,
                        color = MutedForeground,
                        lineHeight = 18.sp,
                    )
                }
            }

            // 결과 메시지
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

            Spacer(Modifier.weight(1f))

            // 승인/거절 버튼
            if (resultMessage == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 거절
                    OutlinedButton(
                        onClick = { showRejectDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isApproving && !isRejecting,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                    ) {
                        if (isRejecting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Danger)
                        } else {
                            Text("거절", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    // 승인
                    Button(
                        onClick = {
                            isApproving = true
                            scope.launch {
                                try {
                                    showService.approveContract(showId)
                                    Log.d(TAG, "approve($showId) success")
                                    resultMessage = "계약이 승인되었습니다."
                                    isApproving = false
                                } catch (e: Exception) {
                                    Log.e(TAG, "approve($showId) failed", e)
                                    resultMessage = "승인 실패: ${e.message}"
                                    isApproving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isApproving && !isRejecting,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    ) {
                        if (isApproving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = White)
                        } else {
                            Text("승인", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
                        }
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

    // 거절 확인 다이얼로그
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("계약 거절", fontWeight = FontWeight.Bold) },
            text = { Text("정말로 이 계약을 거절하시겠습니까?\n거절 후에는 재승인이 필요합니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    isRejecting = true
                    scope.launch {
                        try {
                            showService.rejectContract(showId)
                            Log.d(TAG, "reject($showId) success")
                            resultMessage = "계약이 거절되었습니다."
                            isRejecting = false
                        } catch (e: Exception) {
                            Log.e(TAG, "reject($showId) failed", e)
                            resultMessage = "거절 실패: ${e.message}"
                            isRejecting = false
                        }
                    }
                }) { Text("거절", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("취소", color = MutedForeground) }
            },
        )
    }
}
