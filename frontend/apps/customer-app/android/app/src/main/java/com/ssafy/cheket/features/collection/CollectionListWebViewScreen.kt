package com.ssafy.cheket.features.collection

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.ssafy.cheket.CheketApplication
import com.ssafy.cheket.core.ui.component.AppHeader
import com.ssafy.cheket.ui.theme.Background
import com.ssafy.cheket.ui.theme.MutedForeground
import com.ssafy.cheket.ui.theme.OnBackground
import com.ssafy.cheket.ui.theme.Primary

private const val TAG = "CollectionListWebView"
private const val COLLECTION_LIST_URL = "http://j14d108.p.ssafy.io:3100"
private const val WEB_READY_TIMEOUT_MS = 10_000L
private const val WEB_TIMEOUT_MESSAGE = "서버에 연결할 수 없습니다."

private class CollectionWebAppBridge(
    private val onReady: () -> Unit,
    private val onError: (String?) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var readySignaled = false

    @JavascriptInterface
    fun onAppReady() {
        if (readySignaled) return
        readySignaled = true
        mainHandler.post(onReady)
    }

    @JavascriptInterface
    fun onAppError(message: String?) {
        mainHandler.post { onError(message) }
    }
}

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
    private val sendInterval = 50L

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

        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        val tiltX = pitch.coerceIn(-15f, 15f)
        val tiltY = roll.coerceIn(-15f, 15f)

        webView.post {
            webView.evaluateJavascript(
                "window.dispatchEvent(new CustomEvent('nativeTilt',{detail:{tiltX:$tiltX,tiltY:$tiltY}}))",
                null,
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CollectionListWebViewScreen(
    onBack: () -> Unit,
) {
    var showOverlay by remember { mutableStateOf(true) }
    var pageError by remember { mutableStateOf<String?>(null) }

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
        if (token != null) "$COLLECTION_LIST_URL?token=$token" else COLLECTION_LIST_URL
    }
    val tiltBridge = remember { mutableStateOf<TiltSensorBridge?>(null) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val readyTimeout = remember { mutableStateOf<Runnable?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            readyTimeout.value?.let(mainHandler::removeCallbacks)
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

    val revealContent: (WebView) -> Unit = remember(context, tiltBridge) {
        { webView ->
            readyTimeout.value?.let(mainHandler::removeCallbacks)
            readyTimeout.value = null
            webView.postVisualStateCallback(
                SystemClock.uptimeMillis(),
                object : WebView.VisualStateCallback() {
                    override fun onComplete(requestId: Long) {
                        Log.d(TAG, "Visual state ready: requestId=$requestId")
                        pageError = null
                        showOverlay = false
                        if (tiltBridge.value == null) {
                            val bridge = TiltSensorBridge(context, webView)
                            bridge.start()
                            tiltBridge.value = bridge
                            Log.d(TAG, "TiltBridge started after web ready")
                        }
                    }
                },
            )
        }
    }

    Scaffold(
        topBar = { AppHeader(title = "컬렉션", onBack = onBack) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding),
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        WebView.setWebContentsDebuggingEnabled(true)
                        addJavascriptInterface(
                            CollectionWebAppBridge(
                                onReady = { revealContent(this) },
                                onError = { message ->
                                    Log.e(TAG, "Web app reported error: ${message ?: "unknown"}")
                                    readyTimeout.value?.let(mainHandler::removeCallbacks)
                                    readyTimeout.value = null
                                    pageError = message ?: WEB_TIMEOUT_MESSAGE
                                    showOverlay = true
                                },
                            ),
                            "CheketCollectionBridge",
                        )

                        webViewClient = object : WebViewClient() {
                            override fun onPageCommitVisible(view: WebView?, url: String?) {
                                Log.d(TAG, "onPageCommitVisible: $url")
                                view?.evaluateJavascript(heightFixJs, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                Log.d(TAG, "onPageFinished: $url")
                                readyTimeout.value?.let(mainHandler::removeCallbacks)
                                view?.evaluateJavascript(heightFixJs) { height ->
                                    Log.d(TAG, "[FIX] Forced height to $height")
                                }
                                view?.postDelayed(
                                    {
                                        view.evaluateJavascript(heightFixJs) { height ->
                                            Log.d(TAG, "[FIX] Re-forced height to $height")
                                        }
                                    },
                                    350L,
                                )
                                if (view != null) {
                                    val timeoutRunnable = Runnable {
                                        if (showOverlay) {
                                            Log.w(TAG, "Web app ready signal timed out")
                                            pageError = WEB_TIMEOUT_MESSAGE
                                            showOverlay = true
                                        }
                                    }
                                    readyTimeout.value = timeoutRunnable
                                    mainHandler.postDelayed(timeoutRunnable, WEB_READY_TIMEOUT_MS)
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?,
                            ) {
                                Log.e(TAG, "WebView error: code=$errorCode, desc=$description, url=$failingUrl")
                                readyTimeout.value?.let(mainHandler::removeCallbacks)
                                readyTimeout.value = null
                                pageError = description ?: WEB_TIMEOUT_MESSAGE
                                showOverlay = true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                                Log.d(
                                    TAG,
                                    "JS [${message?.messageLevel()}] ${message?.message()} (${message?.sourceId()}:${message?.lineNumber()})",
                                )
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

            val overlayAlpha by animateFloatAsState(
                targetValue = if (showOverlay) 1f else 0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "overlayFade",
            )

            if (overlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background.copy(alpha = overlayAlpha))
                        .zIndex(10f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (pageError == null) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse,
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
                                text = "컬렉션 목록",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground.copy(alpha = overlayAlpha),
                            )
                            Text(
                                text = "불러오는 중...",
                                fontSize = 13.sp,
                                color = MutedForeground,
                            )
                        } else {
                            Text(
                                text = "불러오기 실패",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnBackground.copy(alpha = overlayAlpha),
                            )
                            Text(
                                text = pageError ?: "컬렉션 목록을 불러올 수 없습니다.",
                                fontSize = 13.sp,
                                color = MutedForeground,
                            )
                        }
                    }
                }
            }
        }
    }
}
