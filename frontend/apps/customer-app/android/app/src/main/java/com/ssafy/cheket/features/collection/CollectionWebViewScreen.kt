package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Primary

/**
 * WebView로 홀로그래픽 티켓 상세 UI를 띄우는 화면.
 *
 * 서버: https://j14d108.p.ssafy.io/collection/{ticketId}
 */
private const val COLLECTION_BASE_URL = "https://j14d108.p.ssafy.io/collection/"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CollectionWebViewScreen(
    ticketId: String,
    onBack: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    val url = "$COLLECTION_BASE_URL$ticketId"

    Scaffold(
        topBar = { AppHeader(title = "소장 티켓", onBack = onBack) }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().background(Background).padding(innerPadding)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}
