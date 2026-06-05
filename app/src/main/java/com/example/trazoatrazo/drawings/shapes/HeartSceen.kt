package com.example.trazoatrazo.drawings.shapes

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*

// ── Paleta ────────────────────────────────────────────────────────────────────
private val BgDark       = Color(0xFF0D0D0D)
private val HeartWhite   = Color(0xFFFFFFFF)
private val HeartRed     = Color(0xFFE8002D)
private val HeartRedDeep = Color(0xFF8B0000)
private val HeartPink    = Color(0xFFFF6B8A)
private val HeartShine   = Color(0xFFFFE0E8)
private val StrokeWhite  = Color(0xFFEEEEEE)
private val StrokeRed    = Color(0xFF8B0000)
private val ParticleRed  = Color(0xFFFF4D6D)
private val ParticlePink = Color(0xFFFFB3C1)

// ── Genera puntos de la curva del corazón ─────────────────────────────────────
// Fórmula paramétrica: x = 16sin³(t), y = 13cos(t) - 5cos(2t) - 2cos(3t) - cos(4t)
private fun heartPoints(
    cx: Float, cy: Float, scale: Float, count: Int = 300
): List<Offset> {
    val points = mutableListOf<Offset>()
    for (i in 0..count) {
        val t  = (i.toDouble() / count) * 2 * PI
        val x  = 16 * sin(t).pow(3)
        val y  = -(13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t))
        points.add(Offset(cx + (x * scale).toFloat(), cy + (y * scale).toFloat()))
    }
    return points
}

// ── Dibuja el trazo del corazón hasta `progress` ──────────────────────────────
private fun DrawScope.drawHeartStroke(
    cx: Float, cy: Float, scale: Float,
    progress: Float, color: Color, strokeW: Float
) {
    val points = heartPoints(cx, cy, scale)
    val end    = (progress * points.size).toInt().coerceIn(1, points.size)
    val path   = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until end) lineTo(points[i].x, points[i].y)
    }
    drawPath(path, color = color, style = Stroke(
        width = strokeW,
        cap   = StrokeCap.Round,
        join  = StrokeJoin.Round
    ))
}

// ── Dibuja el relleno completo del corazón ────────────────────────────────────
private fun DrawScope.drawHeartFill(
    cx: Float, cy: Float, scale: Float,
    fillColor: Color, alpha: Float = 1f
) {
    val points = heartPoints(cx, cy, scale)
    val path   = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, color = fillColor.copy(alpha = alpha))
}

// ── Brillo interior del corazón ───────────────────────────────────────────────
private fun DrawScope.drawHeartShine(
    cx: Float, cy: Float, scale: Float, alpha: Float
) {
    // Brillo superior izquierdo (reflejo de luz)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(HeartShine.copy(alpha = 0.6f * alpha), Color.Transparent),
            center = Offset(cx - scale * 4f, cy - scale * 4f),
            radius = scale * 5f
        ),
        radius = scale * 5f,
        center = Offset(cx - scale * 4f, cy - scale * 4f)
    )
    // Brillo secundario más pequeño
    drawCircle(
        color  = Color.White.copy(alpha = 0.35f * alpha),
        radius = scale * 1.8f,
        center = Offset(cx - scale * 5.5f, cy - scale * 5.5f)
    )
}

// ── Partículas que explotan al completarse ────────────────────────────────────
data class Particle(val angle: Float, val distance: Float, val size: Float, val color: Color)

private fun DrawScope.drawParticles(
    cx: Float, cy: Float, scale: Float,
    progress: Float, particles: List<Particle>
) {
    val p = progress.coerceIn(0f, 1f)
    particles.forEach { particle ->
        val rad  = Math.toRadians(particle.angle.toDouble()).toFloat()
        val dist = particle.distance * scale * p * 3.5f
        val x    = cx + cos(rad) * dist
        val y    = cy + sin(rad) * dist
        val size = particle.size * scale * (1f - p * 0.5f)
        val a    = (1f - p).coerceIn(0f, 1f)
        drawCircle(particle.color.copy(alpha = a), size, Offset(x, y))
    }
}

// ── Pulso del corazón completo ────────────────────────────────────────────────
private fun DrawScope.drawHeartbeat(
    cx: Float, cy: Float, baseScale: Float, beatScale: Float,
    fillColor: Color, glowColor: Color
) {
    val s = baseScale * beatScale

    // Resplandor exterior (glow)
    for (i in 3 downTo 1) {
        drawHeartFill(cx, cy, s + i * baseScale * 0.04f,
            glowColor, alpha = 0.07f * (4 - i))
    }

    // Relleno con gradiente simulado (dos capas)
    drawHeartFill(cx, cy, s, fillColor)
    drawHeartFill(cx, cy, s, HeartRedDeep.copy(alpha = 0.3f))   // sombra base

    // Brillo
    drawHeartShine(cx, cy, s, 1f)

    // Borde
    val points = heartPoints(cx, cy, s)
    val path   = Path().apply {
        moveTo(points[0].x, points[0].y)
        points.forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, color = StrokeRed, style = Stroke(
        width = 2.5f * (s / baseScale),
        cap   = StrokeCap.Round,
        join  = StrokeJoin.Round
    ))
}

// ── Pantalla ──────────────────────────────────────────────────────────────────
@Composable
fun HeartScreen(onBack: () -> Unit) {

    // Estados
    var etapa     by remember { mutableIntStateOf(0) }
    var repetir   by remember { mutableIntStateOf(0) }

    // Animaciones
    val drawProgress  = remember { Animatable(0f) }   // trazo del corazón
    val fillAlpha     = remember { Animatable(0f) }   // aparición del relleno
    val colorProgress = remember { Animatable(0f) }   // blanco → rojo
    val particleAnim  = remember { Animatable(0f) }   // explosión de partículas

    // Pulso continuo cuando termina
    val pulso = rememberInfiniteTransition(label = "pulso")
    val beatScale by pulso.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = keyframes {
                durationMillis = 800
                1f    at 0    with EaseInOut
                1.06f at 150  with EaseInOut
                1f    at 300  with EaseInOut
                1f    at 800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "beat"
    )

    // Partículas generadas una vez
    val particles = remember {
        List(18) { i ->
            Particle(
                angle    = i * 20f,
                distance = 8f + (i % 4) * 3f,
                size     = 0.8f + (i % 3) * 0.4f,
                color    = if (i % 2 == 0) ParticleRed else ParticlePink
            )
        }
    }

    // Motor principal
    LaunchedEffect(repetir) {
        etapa = 0
        drawProgress.snapTo(0f)
        fillAlpha.snapTo(0f)
        colorProgress.snapTo(0f)
        particleAnim.snapTo(0f)

        delay(400L)

        // 1. Trazar el contorno del corazón (blanco)
        etapa = 1
        drawProgress.animateTo(1f, tween(2200, easing = LinearEasing))

        delay(200L)

        // 2. Rellenar de blanco
        etapa = 2
        fillAlpha.animateTo(1f, tween(600, easing = EaseOutCubic))

        delay(300L)

        // 3. Cambiar color blanco → rojo
        etapa = 3
        colorProgress.animateTo(1f, tween(1200, easing = EaseInOutCubic))

        // 4. Partículas explotan
        etapa = 4
        particleAnim.animateTo(1f, tween(800, easing = EaseOutCubic))

        delay(300L)
        etapa = 5   // mostrar botones
    }

    // Color interpolado blanco → rojo
    val currentFill = lerp(HeartWhite, HeartRed, colorProgress.value)
    val currentGlow = lerp(Color.White.copy(alpha = 0.3f), HeartPink, colorProgress.value)

    // Fondo: oscuro que se tiñe levemente de rojo al completarse
    val bgColor = lerp(BgDark, Color(0xFF1A0005), colorProgress.value * 0.7f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Halo de fondo pulsante cuando está completo
        if (etapa >= 5) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val halo = (beatScale - 1f) * 10f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(HeartRed.copy(alpha = 0.12f + halo * 0.02f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.42f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width / 2f, size.height * 0.42f)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx    = size.width  / 2f
            val cy    = size.height * 0.42f
            val scale = size.width  / 45f    // tamaño del corazón

            // ── Etapa 1: Trazo del contorno ───────────────────────────────
            if (etapa >= 1) {
                val strokeColor = lerp(StrokeWhite, StrokeRed, colorProgress.value)
                val strokeW     = 3.5f * (scale / (size.width / 45f))

                // Sombra del trazo
                drawHeartStroke(cx + 2f, cy + 2f, scale,
                    drawProgress.value, Color.Black.copy(alpha = 0.3f), strokeW + 2f)
                // Trazo principal
                drawHeartStroke(cx, cy, scale,
                    drawProgress.value, strokeColor, strokeW)
            }

            // ── Etapa 2+3: Relleno animado ────────────────────────────────
            if (etapa >= 2) {
                if (etapa < 5) {
                    // Relleno normal durante la transición de color
                    drawHeartFill(cx, cy, scale, currentFill, fillAlpha.value)
                    if (fillAlpha.value > 0.5f) {
                        drawHeartShine(cx, cy, scale, fillAlpha.value)
                    }
                    // Borde
                    val strokeColor = lerp(StrokeWhite, StrokeRed, colorProgress.value)
                    drawHeartStroke(cx, cy, scale, 1f, strokeColor, 3f)
                } else {
                    // Corazón completo con pulso
                    drawHeartbeat(cx, cy, scale, beatScale, currentFill, currentGlow)
                }
            }

            // ── Etapa 4: Partículas ───────────────────────────────────────
            if (etapa >= 4) {
                drawParticles(cx, cy, scale, particleAnim.value, particles)
            }
        }

        // ── Botones ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible     = etapa >= 5,
                message     = "❤️ Corazon 14 de febrero❤️",
                subMessage  = "Eres una gran amistad" ,
                repeatEmoji = "❤️",
                accentColor = HeartRed,
                onRepeat    = { repetir++ },
                onBack      = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color.White)
        }
    }
}

// ── Interpolación de color ────────────────────────────────────────────────────
private fun lerp(a: Color, b: Color, t: Float): Color {
    val tc = t.coerceIn(0f, 1f)
    return Color(
        red   = a.red   + (b.red   - a.red)   * tc,
        green = a.green + (b.green - a.green) * tc,
        blue  = a.blue  + (b.blue  - a.blue)  * tc,
        alpha = a.alpha + (b.alpha - a.alpha) * tc
    )
}