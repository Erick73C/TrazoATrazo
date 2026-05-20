package com.example.trazoatrazo.drawings.special

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay

// Paleta
private val BgColor      = Color(0xFF0D0A07)
private val EnvCream     = Color(0xFFF5E6C8)
private val EnvShadow    = Color(0xFFD4B896)
private val EnvBorder    = Color(0xFFD4A017)
private val EnvFlapLight = Color(0xFFEDD9A3)
private val LetterBg     = Color(0xFFFFFBF5)
private val LetterLine   = Color(0xFFE0D5C5)
private val GoldAccent   = Color(0xFFD4A017)

// Pantalla principal
@Composable
fun EnvelopeScreen(
    onBack:       () -> Unit,
    onReadLetter: () -> Unit
) {
    var etapa      by remember { mutableIntStateOf(0) }
    var abrirCarta by remember { mutableStateOf(false) }

    val envelopeAnim = remember { Animatable(0f) }
    val flapAnim     = remember { Animatable(0f) }
    val letterAnim   = remember { Animatable(0f) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        delay(400L)
        envelopeAnim.animateTo(1f, tween(800, easing = EaseOutBack))
        etapa = 1
    }

    // Secuencia de apertura al tocar
    LaunchedEffect(abrirCarta) {
        if (!abrirCarta) return@LaunchedEffect
        etapa = 2
        // Solapa se dobla hacia adentro
        flapAnim.animateTo(1f, tween(650, easing = EaseInOutCubic))
        delay(150L)
        // Carta sube del sobre
        etapa = 3
        letterAnim.animateTo(1f, tween(900, easing = EaseOutBack))
        delay(350L)
        etapa = 4
    }

    // Bob suave mientras espera ser tocado
    val bob = rememberInfiniteTransition(label = "bob")
    val bobY by bob.animateFloat(
        initialValue  = 0f,
        targetValue   = -10f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "bobY"
    )
    val offsetY = if (etapa == 1) bobY else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // Halo de fond
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (envelopeAnim.value > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            GoldAccent.copy(alpha = 0.09f * envelopeAnim.value),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height * 0.44f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width / 2f, size.height * 0.44f)
                )
            }
        }

        // Canvas principal (clickable solo en etapa 1)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (etapa == 1) Modifier.clickable { abrirCarta = true }
                    else Modifier
                )
        ) {
            val cx      = size.width / 2f
            val cy      = size.height * 0.44f + offsetY
            val scale   = size.width / 420f
            val envH    = 195f * scale
            val envTopY = cy - envH / 2f

            // Orden: carta → cuerpo sobre → solapa
            if (etapa >= 3) {
                drawLetter(cx, envTopY, scale, letterAnim.value)
            }
            drawEnvelopeBody(cx, cy, scale, envelopeAnim.value)
            drawEnvelopeFlap(cx, cy, scale, envelopeAnim.value, flapAnim.value)
        }

        // Texto de ayuda
        if (etapa == 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
            ) {
                Text(
                    text     = "✉️  Toca el sobre para abrirlo",
                    fontSize = 14.sp,
                    color    = GoldAccent.copy(alpha = 0.75f)
                )
            }
        }

        // Botones finales
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible     = etapa >= 4,
                message     = "✉️  Una carta para ti ✉️",
                subMessage  = "Con mucho cariño",
                repeatEmoji = "📖",
                repeatLabel = "Leer carta",
                accentColor = GoldAccent,
                onRepeat    = onReadLetter,
                onBack      = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = GoldAccent.copy(alpha = 0.9f))
        }
    }
}

// Carta que sale del sobre
private fun DrawScope.drawLetter(
    cx: Float, envTopY: Float, scale: Float, progress: Float
) {
    val letW    = 118f * scale
    val letH    = 230f * scale
    // Empieza dentro del sobre, sube al animarse
    val letTopY = envTopY - letH * 0.65f * progress + 12f * scale

    // Sombra
    drawRoundRect(
        color        = Color.Black.copy(alpha = 0.22f * progress),
        topLeft      = Offset(cx - letW + 5f, letTopY + 5f),
        size         = Size(letW * 2, letH),
        cornerRadius = CornerRadius(10f * scale)
    )
    // Cuerpo de la carta
    drawRoundRect(
        color        = LetterBg,
        topLeft      = Offset(cx - letW, letTopY),
        size         = Size(letW * 2, letH),
        cornerRadius = CornerRadius(10f * scale)
    )

    // Líneas de texto simuladas
    if (progress > 0.35f) {
        val lp = ((progress - 0.35f) / 0.65f).coerceIn(0f, 1f)
        val x1 = cx - letW + 22f * scale
        val x2 = cx + letW - 22f * scale

        data class Linea(val xFin: Float)
        val lineas = listOf(
            Linea(x2),
            Linea(cx + 15f * scale),
            Linea(x2),
            Linea(cx - 8f * scale),
            Linea(x2),
            Linea(cx + 30f * scale),
        )
        lineas.forEachIndexed { i, linea ->
            val lProgress = ((lp * lineas.size) - i).coerceIn(0f, 1f)
            if (lProgress > 0f) {
                drawLine(
                    color       = LetterLine.copy(alpha = lProgress),
                    start       = Offset(x1, letTopY + 32f * scale + i * 24f * scale),
                    end         = Offset(lerpF(x1, linea.xFin, lProgress),
                        letTopY + 32f * scale + i * 24f * scale),
                    strokeWidth = 2.8f * scale,
                    cap         = StrokeCap.Round
                )
            }
        }

        // Corazón al final de la carta
        if (lp > 0.88f) {
            val hA = ((lp - 0.88f) / 0.12f).coerceIn(0f, 1f)
            drawCircle(Color(0xFFE8002D).copy(alpha = 0.65f * hA),
                8f * scale, Offset(cx, letTopY + letH - 40f * scale))
        }
    }

    // Borde
    drawRoundRect(
        color        = LetterLine.copy(alpha = progress),
        topLeft      = Offset(cx - letW, letTopY),
        size         = Size(letW * 2, letH),
        cornerRadius = CornerRadius(10f * scale),
        style        = Stroke(1.5f * scale)
    )
}

// Cuerpo del sobre
private fun DrawScope.drawEnvelopeBody(
    cx: Float, cy: Float, scale: Float, progress: Float
) {
    val p    = progress.coerceIn(0f, 1f)
    val envW = 145f * scale * p
    val envH = 195f * scale * p
    val r    = 14f * scale * p

    val left   = cx - envW
    val top    = cy - envH / 2f
    val bottom = cy + envH / 2f
    val right  = cx + envW

    // Sombra
    drawRoundRect(
        color        = Color.Black.copy(alpha = 0.35f * p),
        topLeft      = Offset(left + 5f, top + 5f),
        size         = Size(envW * 2, envH),
        cornerRadius = CornerRadius(r)
    )
    // Cuerpo
    drawRoundRect(
        color        = EnvCream,
        topLeft      = Offset(left, top),
        size         = Size(envW * 2, envH),
        cornerRadius = CornerRadius(r)
    )
    // Pliegues en V (parte inferior del sobre)
    drawLine(EnvShadow.copy(alpha = 0.7f * p),
        Offset(left, bottom), Offset(cx, cy + 10f * scale * p), 2.2f * scale)
    drawLine(EnvShadow.copy(alpha = 0.7f * p),
        Offset(right, bottom), Offset(cx, cy + 10f * scale * p), 2.2f * scale)
    // Pliegues laterales
    drawLine(EnvShadow.copy(alpha = 0.35f * p),
        Offset(left, top), Offset(cx, cy + 10f * scale * p), 1.5f * scale)
    drawLine(EnvShadow.copy(alpha = 0.35f * p),
        Offset(right, top), Offset(cx, cy + 10f * scale * p), 1.5f * scale)

    // Sello dorado
    if (p > 0.7f) {
        val sA = ((p - 0.7f) / 0.3f).coerceIn(0f, 1f)
        val sealY = bottom - 28f * scale
        drawCircle(GoldAccent.copy(alpha = 0.9f * sA),       14f * scale, Offset(cx, sealY))
        drawCircle(Color(0xFFFFF0B0).copy(alpha = 0.5f * sA), 9f * scale, Offset(cx, sealY))
        drawCircle(GoldAccent.copy(alpha = sA), 14f * scale,
            Offset(cx, sealY), style = Stroke(1.8f * scale))
    }

    // Borde
    drawRoundRect(
        color        = EnvBorder.copy(alpha = p),
        topLeft      = Offset(left, top),
        size         = Size(envW * 2, envH),
        cornerRadius = CornerRadius(r),
        style        = Stroke(2.5f * scale)
    )
}

// ── Solapa del sobre ───────────────────────────────────────────────────────────
private fun DrawScope.drawEnvelopeFlap(
    cx: Float, cy: Float, scale: Float,
    envProgress: Float, flapProgress: Float
) {
    val p       = envProgress.coerceIn(0f, 1f)
    val envW    = 145f * scale * p
    val envH    = 195f * scale * p
    val flapH   = 82f * scale * p
    val envTopY = cy - envH / 2f

    // scaleY: 1 → 0 (se aplana) → -1 (dobla hacia adentro)
    val scaleY = 1f - 2f * flapProgress

    withTransform({
        scale(scaleX = 1f, scaleY = scaleY, pivot = Offset(cx, envTopY))
    }) {
        val flapPath = Path().apply {
            moveTo(cx - envW, envTopY)
            lineTo(cx,        envTopY - flapH)
            lineTo(cx + envW, envTopY)
            close()
        }
        // Color: claro cuando cerrada, más oscuro al verse el reverso
        val flapColor = if (flapProgress < 0.5f) EnvFlapLight else EnvShadow
        drawPath(flapPath, flapColor)
        drawPath(flapPath, EnvBorder.copy(alpha = p), style = Stroke(2f * scale))
    }
}

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)