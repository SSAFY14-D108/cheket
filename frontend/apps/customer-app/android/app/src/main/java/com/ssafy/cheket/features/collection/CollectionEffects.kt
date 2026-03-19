package com.ssafy.cheket.features.collection

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.*
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────
// Holo Variant
// ─────────────────────────────────────────────────────────────────────
enum class HoloVariant {
    RAINBOW, AURORA, PRISM, COSMOS, SUNSET, NEON
}

private val HOLO_VARIANTS = HoloVariant.entries.toTypedArray()

fun getTicketHoloVariant(ticketId: String): HoloVariant {
    val num = ticketId.replace(Regex("\\D"), "").toIntOrNull() ?: 0
    return HOLO_VARIANTS[num % HOLO_VARIANTS.size]
}

fun getHoloColors(variant: HoloVariant): List<Color> = when (variant) {
    HoloVariant.RAINBOW -> listOf(
        Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77),
        Color(0xFF4D96FF), Color(0xFFA66CFF), Color(0xFFFF6B6B),
    )
    HoloVariant.AURORA -> listOf(
        Color(0xFF00D2FF), Color(0xFF3A7BD5), Color(0xFF6DD5FA),
        Color(0xFF00F5A0), Color(0xFF00D2FF),
    )
    HoloVariant.PRISM -> listOf(
        Color(0xFFFF61D2), Color(0xFFFE9090), Color(0xFFFFEDBE),
        Color(0xFF91F5AD), Color(0xFF61C0FF), Color(0xFFFF61D2),
    )
    HoloVariant.COSMOS -> listOf(
        Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFFF59E0B),
        Color(0xFF10B981), Color(0xFF6366F1), Color(0xFF8B5CF6),
    )
    HoloVariant.SUNSET -> listOf(
        Color(0xFFFF512F), Color(0xFFF09819), Color(0xFFFF6E7F),
        Color(0xFFFF512F),
    )
    HoloVariant.NEON -> listOf(
        Color(0xFF00FF87), Color(0xFF60EFFF), Color(0xFF0061FF),
        Color(0xFFFF00E5), Color(0xFF00FF87),
    )
}

// ─────────────────────────────────────────────────────────────────────
// Number Aura Types
// ─────────────────────────────────────────────────────────────────────
enum class NumberAuraType { NONE, AURA_RARE, MILESTONE_RARE, SPARK_RARE }

fun getNumberAura(ticketId: String): NumberAuraType {
    val value = ticketId.replace(Regex("\\D"), "").toIntOrNull() ?: return NumberAuraType.NONE
    if (value == 0) return NumberAuraType.NONE
    if (value == 1) return NumberAuraType.AURA_RARE
    if (value % 100 == 0) return NumberAuraType.MILESTONE_RARE
    val s = value.toString()
    if ("777" in s || "888" in s || "999" in s) return NumberAuraType.SPARK_RARE
    if (s.length > 1 && s.all { it == s[0] }) return NumberAuraType.SPARK_RARE
    return NumberAuraType.NONE
}

// ─────────────────────────────────────────────────────────────────────
// Grade colors
// ─────────────────────────────────────────────────────────────────────
data class GradeColor(val bg: Color, val text: Color)

private val GRADE_COLORS = mapOf(
    "VIP" to GradeColor(Color(0xFFf59e0b), Color.Black),
    "VIP PIT" to GradeColor(Color(0xFFf59e0b), Color.Black),
    "R" to GradeColor(Color(0xFFef4444), Color.White),
    "R석" to GradeColor(Color(0xFFef4444), Color.White),
    "FLOOR" to GradeColor(Color(0xFF3b82f6), Color.White),
    "GA PIT" to GradeColor(Color(0xFFef4444), Color.White),
    "S" to GradeColor(Color(0xFF3b82f6), Color.White),
    "S석" to GradeColor(Color(0xFF3b82f6), Color.White),
    "A" to GradeColor(Color(0xFF22c55e), Color.White),
    "A석" to GradeColor(Color(0xFF22c55e), Color.White),
    "STAND" to GradeColor(Color(0xFF22c55e), Color.White),
    "2F" to GradeColor(Color(0xFFa855f7), Color.White),
    "1F" to GradeColor(Color(0xFF06b6d4), Color.White),
    "GA" to GradeColor(Color(0xFF3b82f6), Color.White),
)

fun getGradeColor(grade: String): GradeColor {
    return GRADE_COLORS[grade] ?: GradeColor(Color(0xFF6b7280), Color.White)
}

// ─────────────────────────────────────────────────────────────────────
// Tilt State (sensor-based)
// ─────────────────────────────────────────────────────────────────────
data class TiltState(val x: Float = 0f, val y: Float = 0f)

@Composable
fun rememberTiltState(maxDegrees: Float = 10f): State<TiltState> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(TiltState()) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // event.values: [x, y, z] in m/s^2
                // normalize to -1..1 range (gravity ~9.81)
                val nx = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                val ny = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                tiltState.value = TiltState(
                    x = -ny * maxDegrees, // tilt around X axis
                    y = nx * maxDegrees,  // tilt around Y axis
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    return tiltState
}

// ─────────────────────────────────────────────────────────────────────
// FlippableCard — Card with flip animation
// ─────────────────────────────────────────────────────────────────────
@Composable
fun FlippableCard(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val density = LocalDensity.current.density
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cardFlip",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY + rotation
                cameraDistance = 12f * density
            },
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// HoloOverlay — Animated holographic gradient overlay
// ─────────────────────────────────────────────────────────────────────
@Composable
fun HoloOverlay(
    variant: HoloVariant,
    tiltX: Float = 0f,
    tiltY: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val holoColors = remember(variant) { getHoloColors(variant) }
    val infiniteTransition = rememberInfiniteTransition(label = "holoShimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerPhase",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                drawHolographicOverlay(holoColors, shimmerPhase, tiltX, tiltY)
            }
    )
}

fun DrawScope.drawHolographicOverlay(
    holoColors: List<Color>,
    phase: Float,
    tiltX: Float,
    tiltY: Float,
) {
    val w = size.width
    val h = size.height
    val baseAngle = phase * 360f
    val tiltAngle = tiltY * 4f
    val angle = Math.toRadians((baseAngle + tiltAngle).toDouble())

    val cx = w / 2f
    val cy = h / 2f
    val radius = maxOf(w, h) * 0.8f

    val startX = cx + cos(angle).toFloat() * radius
    val startY = cy + sin(angle).toFloat() * radius
    val endX = cx - cos(angle).toFloat() * radius
    val endY = cy - sin(angle).toFloat() * radius

    val overlayColors = holoColors.map { it.copy(alpha = 0.18f) }

    val brush = Brush.linearGradient(
        colors = overlayColors,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
    )

    drawRect(brush = brush, blendMode = BlendMode.Screen)

    // Glare highlight
    val glareCx = cx + tiltY * w * 0.06f
    val glareCy = cy - tiltX * h * 0.06f
    val glareBrush = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.04f),
            Color.Transparent,
        ),
        center = Offset(glareCx, glareCy),
        radius = minOf(w, h) * 0.6f,
    )
    drawRect(brush = glareBrush)
}

// ─────────────────────────────────────────────────────────────────────
// GlareEffect — Radial gradient that follows tilt
// ─────────────────────────────────────────────────────────────────────
fun Modifier.glareEffect(tiltX: Float, tiltY: Float): Modifier = this.drawWithContent {
    drawContent()
    val cx = size.width / 2f + tiltY * size.width * 0.1f
    val cy = size.height / 2f - tiltX * size.height * 0.1f
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.03f),
                Color.Transparent,
            ),
            center = Offset(cx, cy),
            radius = minOf(size.width, size.height) * 0.55f,
        ),
    )
}

// ─────────────────────────────────────────────────────────────────────
// SparkleParticles — Canvas-based particle system
// ─────────────────────────────────────────────────────────────────────
private data class Sparkle(
    var x: Float,
    var y: Float,
    var size: Float,
    var alpha: Float,
    var speed: Float,
    var phase: Float,
    var twinkleSpeed: Float,
    val color: Color,
)

@Composable
fun SparkleParticles(
    particleCount: Int = 12,
    modifier: Modifier = Modifier,
) {
    val sparkleColors = remember {
        listOf(
            Color.White,
            Color(0xFFF5F8FF),
            Color(0xFFE6DAFF),
            Color(0xFFCAE6FF),
        )
    }

    val sparkles = remember(particleCount) {
        List(particleCount) {
            Sparkle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1.5f,
                alpha = Random.nextFloat() * 0.7f + 0.2f,
                speed = Random.nextFloat() * 0.0003f + 0.0001f,
                phase = Random.nextFloat() * 2f * PI.toFloat(),
                twinkleSpeed = Random.nextFloat() * 0.003f + 0.001f,
                color = sparkleColors[Random.nextInt(sparkleColors.size)],
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val timeMillis by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparkleTime",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        sparkles.forEach { s ->
            val t = timeMillis
            // Twinkle: oscillate alpha
            val twinkle = sin(t * s.twinkleSpeed + s.phase)
            val currentAlpha = (s.alpha * (0.4f + 0.6f * (twinkle * 0.5f + 0.5f))).coerceIn(0f, 1f)

            // Gentle drift
            val driftX = sin(t * s.speed + s.phase) * 0.03f
            val driftY = cos(t * s.speed * 0.7f + s.phase) * 0.02f
            val px = ((s.x + driftX) % 1f + 1f) % 1f * w
            val py = ((s.y + driftY) % 1f + 1f) % 1f * h

            // Draw star shape
            drawCircle(
                color = s.color.copy(alpha = currentAlpha),
                radius = s.size,
                center = Offset(px, py),
            )
            // Cross highlight for larger particles
            if (s.size > 2.5f) {
                val crossLen = s.size * 1.8f
                drawLine(
                    color = s.color.copy(alpha = currentAlpha * 0.6f),
                    start = Offset(px - crossLen, py),
                    end = Offset(px + crossLen, py),
                    strokeWidth = 0.8f,
                )
                drawLine(
                    color = s.color.copy(alpha = currentAlpha * 0.6f),
                    start = Offset(px, py - crossLen),
                    end = Offset(px, py + crossLen),
                    strokeWidth = 0.8f,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// NumberAura — Glow effect for special ticket numbers
// ─────────────────────────────────────────────────────────────────────
fun Modifier.numberAura(auraType: NumberAuraType): Modifier {
    if (auraType == NumberAuraType.NONE) return this

    val (innerColor, outerColor) = when (auraType) {
        NumberAuraType.AURA_RARE -> Color(0xFFA66CFF).copy(alpha = 0.35f) to Color(0xFFA66CFF).copy(alpha = 0f)
        NumberAuraType.MILESTONE_RARE -> Color(0xFFFFD93D).copy(alpha = 0.30f) to Color(0xFFFFD93D).copy(alpha = 0f)
        NumberAuraType.SPARK_RARE -> Color(0xFF60EFFF).copy(alpha = 0.30f) to Color(0xFF60EFFF).copy(alpha = 0f)
        else -> return this
    }

    return this.drawBehind {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val glowRadius = maxOf(size.width, size.height) * 0.65f

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(innerColor, outerColor),
                center = Offset(cx, cy),
                radius = glowRadius,
            ),
        )
    }
}
