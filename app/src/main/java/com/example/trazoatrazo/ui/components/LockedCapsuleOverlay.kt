package com.example.trazoatrazo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.domain.model.TimeCapsule
import com.example.trazoatrazo.utils.CapsuleUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ── Paleta del overlay de cápsula bloqueada ───────────────────────────────────
private val CapsuleBg      = Color(0xFF0D0A07)
private val LockGold       = Color(0xFFD4A017)
private val LockGoldSoft   = Color(0xFFF5E6C8)
private val LockShadow     = Color(0xFF4A3A10)
private val TextSoft       = Color(0xFFB8A57A)

/**
 * Pantalla flotante que se muestra cuando el usuario intenta acceder a un
 * dibujo cuya [TimeCapsule] todavía no llegó a su fecha de desbloqueo.
 *
 * Se usa desde dos lugares: [com.example.trazoatrazo.ui.components.DrawingCard]
 * (al tocar una tarjeta bloqueada) y `AppNavigation` (como gate defensivo si
 * se intenta navegar directo al `drawingId` real de una cápsula bloqueada).
 *
 * No recibe `onRepeat` ni usa [DrawingButtons] porque no hay animación de
 * dibujo que "repetir" — es un estado informativo, no un dibujo terminado.
 */
@Composable
fun LockedCapsuleOverlay(
    capsule: TimeCapsule,
    onBack:  () -> Unit
) {
    val daysRemaining = remember(capsule.id) { CapsuleUtils.daysRemainingFor(capsule.drawingId) }

    var etapa by remember { mutableIntStateOf(0) }

    val entryAnim  = remember { Animatable(0f) }
    val shackleAnim = remember { Animatable(0f) } // el arco del candado "se cierra" al aparecer

    LaunchedEffect(capsule.id) {
        etapa = 0
        entryAnim.snapTo(0f)
        shackleAnim.snapTo(0f)

        entryAnim.animateTo(1f, tween(600, easing = EaseOutBack))
        etapa = 1
        shackleAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
        etapa = 2
    }

    // Pulso continuo del brillo dorado + pequeño balanceo del candado
    val pulso = rememberInfiniteTransition(label = "capsule_pulse")
    val glowPulse by pulso.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val swayAngle by pulso.animateFloat(
        initialValue  = -3f,
        targetValue   = 3f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )

    // Partículas doradas flotando alrededor (generadas una sola vez)
    val sparkles = remember(capsule.id) {
        val rng = Random(capsule.id.hashCode())
        List(16) {
            Triple(
                rng.nextFloat(),                       // xFrac
                rng.nextFloat(),                        // yFrac
                0.5f + rng.nextFloat() * 2f              // fase de parpadeo
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CapsuleBg)
            .pointerInput(Unit) {
                detectTapGestures { /* Bloquear clics en el fondo */ }
            }
    ) {
        // ── Halo de fondo ────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(LockGold.copy(alpha = 0.10f * glowPulse), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.40f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width / 2f, size.height * 0.40f)
            )

            // Chispitas doradas flotando
            sparkles.forEach { (xf, yf, phase) ->
                val twinkle = (sin(glowPulse * PI.toFloat() * 2f + phase) + 1f) / 2f
                drawCircle(
                    color  = LockGoldSoft.copy(alpha = 0.15f + 0.35f * twinkle),
                    radius = 2.2f,
                    center = Offset(xf * size.width, yf * size.height * 0.75f)
                )
            }
        }

        // ── Candado animado ──────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(160.dp)
                .graphicsLayer {
                    scaleX = entryAnim.value
                    scaleY = entryAnim.value
                    rotationZ = if (etapa >= 2) swayAngle else 0f
                }
        ) {
            drawLockIcon(shackleProgress = shackleAnim.value, glowPulse = glowPulse)
        }

        // ── Textos ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 200.dp, start = 28.dp, end = 28.dp)
                .graphicsLayer { alpha = entryAnim.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(capsule.emoji, fontSize = 30.sp)

            Text(
                text       = capsule.title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = LockGold,
                textAlign  = TextAlign.Center
            )

            Text(
                text       = capsule.lockedMessage,
                fontSize   = 14.sp,
                color      = TextSoft,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (daysRemaining > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(LockGold.copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (daysRemaining == 1L) "Falta 1 día ⏳" else "Faltan $daysRemaining días ⏳",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = LockGold
                    )
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = LockGold.copy(alpha = 0.9f))
        }
    }
}

// ── Dibujo del candado (arco + cuerpo) ────────────────────────────────────────
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLockIcon(
    shackleProgress: Float,
    glowPulse:       Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val s  = size.width / 160f

    val bodyW = 84f * s
    val bodyH = 62f * s
    val bodyTop = cy + 6f * s

    // Arco (shackle) — se "cierra" verticalmente al aparecer (0 = abierto arriba, 1 = cerrado)
    // El arco ahora baja lo suficiente para "entrar" en el cuerpo
    val shackleRadius = 32f * s
    val shackleMaxOffsetY = 24f * s
    val shackleOffsetY = (1f - shackleProgress) * -shackleMaxOffsetY
    val shackleTop = bodyTop - shackleRadius * 1.2f + shackleOffsetY

    // Dibujar las "patas" del arco que entran al cuerpo
    val shackleStrokeW = 10f * s
    
    // Parte curva del arco
    drawArc(
        color      = LockGold.copy(alpha = 0.65f + 0.35f * glowPulse),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter  = false,
        topLeft    = Offset(cx - shackleRadius, shackleTop),
        size       = Size(shackleRadius * 2, shackleRadius * 2),
        style      = Stroke(width = shackleStrokeW, cap = StrokeCap.Round)
    )
    
    // Patas rectas del arco (conectan el arco con el cuerpo)
    val patasTop = shackleTop + shackleRadius
    val patasBottom = bodyTop + 10f * s // Entran un poco en el cuerpo
    
    drawLine(
        color = LockGold.copy(alpha = 0.65f + 0.35f * glowPulse),
        start = Offset(cx - shackleRadius, patasTop),
        end = Offset(cx - shackleRadius, patasBottom),
        strokeWidth = shackleStrokeW,
        cap = StrokeCap.Butt
    )
    drawLine(
        color = LockGold.copy(alpha = 0.65f + 0.35f * glowPulse),
        start = Offset(cx + shackleRadius, patasTop),
        end = Offset(cx + shackleRadius, patasBottom),
        strokeWidth = shackleStrokeW,
        cap = StrokeCap.Butt
    )

    // Sombra del cuerpo
    drawRoundRect(
        color        = LockShadow.copy(alpha = 0.5f),
        topLeft      = Offset(cx - bodyW / 2 + 4f, bodyTop + 4f),
        size         = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(16f * s)
    )
    // Cuerpo del candado
    drawRoundRect(
        brush        = Brush.verticalGradient(
            colors = listOf(LockGoldSoft, LockGold),
            startY = bodyTop,
            endY   = bodyTop + bodyH
        ),
        topLeft      = Offset(cx - bodyW / 2, bodyTop),
        size         = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(16f * s)
    )
    drawRoundRect(
        color        = LockShadow.copy(alpha = 0.7f),
        topLeft      = Offset(cx - bodyW / 2, bodyTop),
        size         = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(16f * s),
        style        = Stroke(width = 2.5f * s)
    )

    // Ojo de la cerradura (mejorado)
    val keyholeY = bodyTop + bodyH * 0.45f
    drawCircle(LockShadow.copy(alpha = 0.9f), 8f * s, Offset(cx, keyholeY))
    val trianglePath = Path().apply {
        moveTo(cx - 4f * s, keyholeY)
        lineTo(cx + 4f * s, keyholeY)
        lineTo(cx + 6f * s, keyholeY + 18f * s)
        lineTo(cx - 6f * s, keyholeY + 18f * s)
        close()
    }
    drawPath(trianglePath, color = LockShadow.copy(alpha = 0.9f))

    // Brillo superior sutil
    drawRoundRect(
        color        = Color.White.copy(alpha = 0.15f),
        topLeft      = Offset(cx - bodyW / 2 + 6f * s, bodyTop + 6f * s),
        size         = Size(bodyW - 12f * s, bodyH * 0.25f),
        cornerRadius = CornerRadius(10f * s)
    )
}




