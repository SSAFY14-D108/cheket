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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

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

            ScanGuideOverlay(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 44.dp)
            )

            // 로그아웃 버튼 (우측 상단)
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 12.dp),
            ) {
                Text(
                    text = "로그아웃",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
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
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
            text = "QR 코드를 프레임에 맞춰주세요",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(260.dp)
                .border(
                    width = 4.dp,
                    color = Color(0xFFFFD54F),
                    shape = RoundedCornerShape(24.dp)
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
            text = "카메라 권한이 필요합니다.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRequestPermission) {
            Text("권한 허용")
        }
    }
}

@Composable
private fun ResultDialog(
    state: ScanResultDialogState,
    onDismiss: () -> Unit
) {
    val (title, message, containerColor) = when (state) {
        is ScanResultDialogState.Success -> Triple(
            "입장 승인",
            state.message,
            Color(0xFF1B5E20),
        )
        is ScanResultDialogState.Failure -> Triple(
            "입장 실패",
            state.message,
            Color(0xFFB71C1C),
        )
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                if (state is ScanResultDialogState.Success) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = buildString {
                            state.data.showTitle?.let { appendLine(it) }
                            state.data.sectionName?.let { append("$it ") }
                            state.data.seatNo?.let { appendLine(it) }
                            state.data.grade?.let { appendLine("등급: $it") }
                            state.data.userName?.let { appendLine("입장자: $it") }
                            state.data.checkedInAt?.let { appendLine("체크인: $it") }
                        }.trimEnd(),
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
