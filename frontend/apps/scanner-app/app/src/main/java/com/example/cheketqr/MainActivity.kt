package com.example.cheketqr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cheketqr.data.network.TokenStore
import com.example.cheketqr.presentation.login.LoginScreen
import com.example.cheketqr.presentation.scanner.ScannerRoute
import com.example.cheketqr.ui.theme.CheketQRTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenStore.init(this)
        enableEdgeToEdge()
        setContent {
            CheketQRTheme {
                var isLoggedIn by remember { mutableStateOf(TokenStore.isLoggedIn) }

                // TokenStore.clear() 호출 시 (토큰 만료 등) 자동으로 로그인 화면 전환
                LaunchedEffect(Unit) {
                    snapshotFlow { TokenStore.isLoggedIn }
                        .collect { isLoggedIn = it }
                }

                if (isLoggedIn) {
                    ScannerRoute(
                        modifier = Modifier.fillMaxSize(),
                        onLogout = {
                            TokenStore.clear()
                            isLoggedIn = false
                        },
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = { isLoggedIn = true },
                    )
                }
            }
        }
    }
}
