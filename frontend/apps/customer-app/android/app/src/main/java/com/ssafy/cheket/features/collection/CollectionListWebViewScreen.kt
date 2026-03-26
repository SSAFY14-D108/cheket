package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary

private const val TAG = "CollectionListWebView"

private const val COLLECTION_LIST_URL = "http://j14d108.p.ssafy.io:3100"

/**
 * Android 가속계 → WebView tilt Bridge.
 * SensorManager에서 가속계를 읽어 WebView에 JS 이벤트로 전달.
 */
private class TiltSensorBridge(
    context: Context,
    private val webView: WebView,
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var lastSendTime = 0L
    private val sendInterval = 50L // ~20fps (성능 최적화: 60fps → 20fps)

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        Log.d(TAG, "TiltSensor started (accel=${accelerometer != null}, mag=${magnetometer != null})")
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "TiltSensor stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter for smoothing
                gravity[0] = 0.8f * gravity[0] + 0.2f * event.values[0]
                gravity[1] = 0.8f * gravity[1] + 0.2f * event.values[1]
                gravity[2] = 0.8f * gravity[2] + 0.2f * event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = 0.8f * geomagnetic[0] + 0.2f * event.values[0]
                geomagnetic[1] = 0.8f * geomagnetic[1] + 0.2f * event.values[1]
                geomagnetic[2] = 0.8f * geomagnetic[2] + 0.2f * event.values[2]
            }
        }

        val now = System.currentTimeMillis()
        if (now - lastSendTime < sendInterval) return
        lastSendTime = now

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)
        if (!success) return

        SensorManager.getOrientation(rotationMatrix, orientation)

        // orientation[1] = pitch (앞뒤 기울기), orientation[2] = roll (좌우 기울기)
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat() // -90 ~ 90
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()  // -180 ~ 180

        // 카드 tilt에 적합하도록 -15 ~ 15도 범위로 클램핑
        val tiltX = pitch.coerceIn(-15f, 15f)
        val tiltY = roll.coerceIn(-15f, 15f)

        webView.post {
            webView.evaluateJavascript(
                "window.dispatchEvent(new CustomEvent('nativeTilt',{detail:{tiltX:$tiltX,tiltY:$tiltY}}))",
                null,
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

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
            app.authDataStore.getAccessToken()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get auth token", e)
            null
        }
    }

    val url = remember(token) {
        if (token != null) "$COLLECTION_LIST_URL?token=$token"
        else COLLECTION_LIST_URL
    }

    // TiltSensor lifecycle
    val tiltBridge = remember { mutableStateOf<TiltSensorBridge?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            tiltBridge.value?.stop()
        }
    }

    Log.d(TAG, "Loading collection list: $COLLECTION_LIST_URL (token=${if (token != null) "present" else "null"})")

    val heightFixJs = """
        (function() {
            var h = window.innerHeight + 'px';
            document.documentElement.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
            document.body.style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;overflow:hidden!important;';
            var children = document.body.children;
            for (var i = 0; i < children.length; i++) {
                children[i].style.cssText += 'height:' + h + '!important;min-height:' + h + '!important;';
            }
            return h;
        })()
    """.trimIndent()

    Scaffold(
        topBar = { com.ssafy.cheket.core.ui.component.AppHeader(title = "컬렉션", onBack = onBack) },
    ) { innerPadding ->
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
                        WebView.setWebContentsDebuggingEnabled(true) // release 시 false로 변경

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                Log.d(TAG, "onPageFinished: $url")
                                isLoading = false
                                // height fix 즉시 + 2초 후 재적용
                                view?.evaluateJavascript(heightFixJs) { h ->
                                    Log.d(TAG, "[FIX] Forced height to $h")
                                }
                                view?.postDelayed({
                                    view.evaluateJavascript(heightFixJs) { h ->
                                        Log.d(TAG, "[FIX] Re-forced height to $h")
                                    }
                                }, 2000)

                                // 가속계 Bridge 시작 (React hydration + API fetch 완료 대기)
                                view?.postDelayed({
                                    if (tiltBridge.value == null) {
                                        val bridge = TiltSensorBridge(ctx, view)
                                        bridge.start()
                                        tiltBridge.value = bridge
                                        Log.d(TAG, "TiltBridge started (delayed)")
                                    }
                                }, 3000)
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
                            setSupportZoom(false)
                            builtInZoomControls = false
                        }

                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            // 진입 애니메이션 오버레이 — WebView 로딩을 가리고 자연스럽게 전환
            val showOverlay = remember { mutableStateOf(true) }
            val overlayAlpha by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (showOverlay.value) 1f else 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 600,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing,
                ),
                label = "overlayFade",
            )

            // height fix 완료 후 + 최소 1.2초 보장 후 오버레이 숨김
            LaunchedEffect(isLoading) {
                if (!isLoading) {
                    kotlinx.coroutines.delay(1200L) // 최소 진입 애니메이션 시간
                    showOverlay.value = false
                }
            }

            if (overlayAlpha > 0f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Background.copy(alpha = overlayAlpha))
                        .zIndex(10f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // 스피너 + 펄스 애니메이션
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                            ),
                            label = "scale",
                        )

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = Primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(64.dp),
                            )
                        }

                        Text(
                            text = "컬렉션",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnBackground.copy(alpha = overlayAlpha),
                        )
                        Text(
                            text = "컬렉션을 불러오는 중...",
                            fontSize = 13.sp,
                            color = MutedForeground,
                        )
                    }
                }
            }
        }
    }
}
