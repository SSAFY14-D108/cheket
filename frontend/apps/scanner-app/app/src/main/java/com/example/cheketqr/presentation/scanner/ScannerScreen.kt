package com.example.cheketqr.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

private val Primary = Color(0xFF00C598)
private val ScanFrameColor = Color(0xFF00C598)

@Composable
fun ScannerRoute(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    viewModel: ScannerViewModel = viewModel(factory = ScannerViewModelFactory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onCameraPermissionChanged
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onCameraPermissionChanged(granted)
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ScannerScreen(
        modifier = modifier,
        state = state,
        onRequestPermission = {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onQrDetected = viewModel::onQrDetected,
        onDismissDialog = viewModel::dismissDialog,
        onLogout = onLogout,
    )
}

@Composable
private fun ScannerScreen(
    modifier: Modifier,
    state: ScannerUiState,
    onRequestPermission: () -> Unit,
    onQrDetected: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onLogout: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                isScannerEnabled = state.isScannerEnabled,
                onQrDetected = onQrDetected
            )

            // 상단 가이드
            ScanGuideOverlay(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            )

            // 하단 로그아웃 버튼
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
            ) {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "로그아웃",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        } else {
            PermissionGuide(
                modifier = Modifier.align(Alignment.Center),
                onRequestPermission = onRequestPermission
            )
        }

        if (state.isVerifying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "확인 중...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        state.resultDialog?.let { dialogState ->
            ResultDialog(
                state = dialogState,
                onDismiss = onDismissDialog
            )
        }
    }
}

@Composable
private fun ScanGuideOverlay(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR 코드를 스캔하세요",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "티켓 QR 코드를 프레임 안에 맞춰주세요",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.68f)
                .height(250.dp)
                .border(
                    width = 3.dp,
                    color = ScanFrameColor,
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}

@Composable
private fun PermissionGuide(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "카메라 권한이 필요합니다",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "QR 코드를 스캔하려면 카메라 접근을 허용해주세요.",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("권한 허용", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ResultDialog(
    state: ScanResultDialogState,
    onDismiss: () -> Unit
) {
    val isSuccess = state is ScanResultDialogState.Success
    val containerColor = if (isSuccess) Color(0xFF0D3320) else Color(0xFF3B1111)
    val accentColor = if (isSuccess) Primary else Color(0xFFEF4444)
    val title = if (isSuccess) "입장 승인" else "입장 실패"
    val message = when (state) {
        is ScanResultDialogState.Success -> state.message
        is ScanResultDialogState.Failure -> state.message
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아이콘
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Close,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(44.dp),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                )

                if (state is ScanResultDialogState.Success) {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 티켓 정보
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.data.showTitle?.let {
                            InfoRow("공연", it)
                        }
                        state.data.userName?.let {
                            InfoRow("입장자", it)
                        }
                        val seatInfo = buildString {
                            state.data.sectionName?.let { append("$it ") }
                            state.data.seatNo?.let { append(it) }
                        }.trim()
                        if (seatInfo.isNotBlank()) {
                            InfoRow("좌석", seatInfo)
                        }
                        state.data.grade?.let {
                            InfoRow("등급", it)
                        }
                        state.data.checkedInAt?.let {
                            InfoRow("체크인", formatCheckinTime(it))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "확인",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * ISO datetime (2026-03-28T14:30:00) → "14:30" 또는 "2026.03.28 14:30"
 */
private fun formatCheckinTime(raw: String): String {
    return try {
        // "2026-03-28T14:30:00" or "2026-03-28T14:30:00.123"
        val dateTimePart = raw.substringBefore(".")  // remove millis
        val parts = dateTimePart.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            val date = "${dateParts[0]}.${dateParts[1]}.${dateParts[2]}"
            val time = "${timeParts[0]}:${timeParts[1]}"
            "$date $time"
        } else {
            raw
        }
    } catch (_: Exception) {
        raw
    }
}
