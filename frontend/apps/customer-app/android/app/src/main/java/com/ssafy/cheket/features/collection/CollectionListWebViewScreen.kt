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
    private val sendInterval = 16L // ~60fps

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}
