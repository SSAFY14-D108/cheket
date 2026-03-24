package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.Primary

private const val TAG = "CollectionListWebView"

/**
 * 컬렉션 목록을 WebView로 표시.
 *
 * 배포 URL: https://j14d108.p.ssafy.io/customer-collection
 * 로컬 테스트: http://10.0.2.2:3100 (에뮬레이터)
 *
 * 인증 토큰은 ?token=xxx 쿼리 파라미터로 전달.
 */
private const val COLLECTION_LIST_URL = "http://j14d108.p.ssafy.io:3100"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CollectionListWebViewScreen(
    onBack: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val token = remember {
        try {
            val app = context.applicationContext as CheketApplication
            val authDataStore = app.authDataStore
            authDataStore.getAccessToken()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get auth token", e)
            null
        }
    }

    val url = remember(token) {
        if (token != null) {
            "$COLLECTION_LIST_URL?token=$token"
        } else {
            COLLECTION_LIST_URL
        }
    }

    Log.d(TAG, "Loading collection list: $COLLECTION_LIST_URL (token=${if (token != null) "present" else "null"})")

    Scaffold { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        WebView.setWebContentsDebuggingEnabled(true)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                Log.d(TAG, "onPageFinished: $url")
                                isLoading = false
                                // WebView에서 html/body height가 0이 되는 문제 강제 수정
                                view?.evaluateJavascript("""
                                    (function() {
                                        var h = window.innerHeight + 'px';
                                        document.documentElement.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
                                        document.body.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
                                        var children = document.body.children;
                                        for (var i = 0; i < children.length; i++) {
                                            children[i].style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;';
                                        }
                                        console.log('[FIX] Forced height to ' + h);
                                    })()
                                """.trimIndent(), null)
                                // 데이터 로드 후 한번 더
                                view?.postDelayed({
                                    view.evaluateJavascript("""
                                        (function() {
                                            var h = window.innerHeight + 'px';
                                            document.documentElement.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
                                            document.body.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
                                            var children = document.body.children;
                                            for (var i = 0; i < children.length; i++) {
                                                children[i].style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;';
                                            }
                                            console.log('[FIX] Re-forced height to ' + h + ', body.offsetHeight=' + document.body.offsetHeight);
                                        })()
                                    """.trimIndent(), null)
                                }, 2000)
                            }
                            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                                Log.e(TAG, "WebView error: code=$errorCode, desc=$description, url=$failingUrl")
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                Log.d(TAG, "JS [${msg?.messageLevel()}] ${msg?.message()} (${msg?.sourceId()}:${msg?.lineNumber()})")
                                return true
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            // 모바일 뷰포트 지원
                            setSupportZoom(false)
                            builtInZoomControls = false
                        }
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
