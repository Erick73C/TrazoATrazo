package com.example.trazoatrazo.drawings.shapes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlin.random.Random

@Composable
fun TrofeoScreen(onBack: () -> Unit) {
    var repetir by remember { mutableIntStateOf(0) }
    val screenAnim = remember { Animatable(0f) }
    val drawProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(repetir) {
        screenAnim.snapTo(0f)
        drawProgress.snapTo(0f)

        screenAnim.animateTo(1f, tween(400, easing = EaseOutBack))
        drawProgress.animateTo(1f, tween(4500, easing = LinearEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAnim.value)
                .offset(y = ((1f - screenAnim.value) * 30).dp)
        ) {
            drawInovatecTrophy(
                centerX = size.width / 2,
                centerY = size.height / 2.0f,
                scale = 1.9f,
                progress = drawProgress.value,
                textMeasurer = textMeasurer
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = true,
                message = "🏆 ¡Ganadora Regional! 🏆",
                subMessage = "Felicitaciones a Sandra Rebeca por ganar la etapa regional de Inovatec :D",
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color.Black)
        }
    }
}

fun DrawScope.drawInovatecTrophy(
    centerX: Float,
    centerY: Float,
    scale: Float = 1f,
    progress: Float = 1f,
    textMeasurer: TextMeasurer
) {
    val goldColor = Color(0xFFFFD700)
    val baseColor = Color(0xFF992222)
    val strokeWidth = 5f * scale

    // ═══════════════════════════════════════════════════════════
    // FASE 1: Base (0.0 - 0.15)
    // ═══════════════════════════════════════════════════════════
    val basePhase = (progress / 0.15f).coerceIn(0f, 1f)
    if (basePhase > 0f) {
        val basePath = Path().apply {
            moveTo(centerX - 70f * scale, centerY + 80f * scale)
            lineTo(centerX - 70f * scale + (140f * scale * basePhase), centerY + 80f * scale)
        }
        drawPath(basePath, baseColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        
        if (basePhase >= 1f) {
            drawRoundRect(
                color = baseColor,
                topLeft = Offset(centerX - 70f * scale, centerY + 80f * scale),
                size = Size(140f * scale, 50f * scale),
                cornerRadius = CornerRadius(25f * scale)
            )
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FASE 2: Pedestal y Parte Inferior Copa (0.15 - 0.40)
    // ═══════════════════════════════════════════════════════════
    val cupPhase = ((progress - 0.15f) / 0.25f).coerceIn(0f, 1f)
    if (cupPhase > 0f) {
        val pedestalHeight = 15f * scale
        val currentPedestal = pedestalHeight * (cupPhase / 0.2f).coerceIn(0f, 1f)
        
        drawLine(
            color = goldColor,
            start = Offset(centerX, centerY + 80f * scale),
            end = Offset(centerX, centerY + 80f * scale - currentPedestal),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        if (cupPhase > 0.2f) {
            val trapPhase = ((cupPhase - 0.2f) / 0.8f).coerceIn(0f, 1f)
            val trapPath = Path().apply {
                moveTo(centerX, centerY + 65f * scale)
                lineTo(centerX - (50f * scale * trapPhase), centerY + 65f * scale - (55f * scale * trapPhase))
                moveTo(centerX, centerY + 65f * scale)
                lineTo(centerX + (50f * scale * trapPhase), centerY + 65f * scale - (55f * scale * trapPhase))
            }
            drawPath(trapPath, goldColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

            if (trapPhase >= 1f) {
                val trapFill = Path().apply {
                    moveTo(centerX - 10f * scale, centerY + 65f * scale)
                    lineTo(centerX + 10f * scale, centerY + 65f * scale)
                    lineTo(centerX + 50f * scale, centerY + 10f * scale)
                    lineTo(centerX - 50f * scale, centerY + 10f * scale)
                    close()
                }
                drawPath(trapFill, goldColor)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FASE 3: Cuerpo Superior Rectangular (0.40 - 0.60)
    // ═══════════════════════════════════════════════════════════
    val bodyPhase = ((progress - 0.40f) / 0.20f).coerceIn(0f, 1f)
    if (bodyPhase > 0f) {
        val rectPath = Path().apply {
            moveTo(centerX - 50f * scale, centerY + 10f * scale)
            lineTo(centerX - 50f * scale, centerY + 10f * scale - (60f * scale * bodyPhase))
            moveTo(centerX + 50f * scale, centerY + 10f * scale)
            lineTo(centerX + 50f * scale, centerY + 10f * scale - (60f * scale * bodyPhase))
            if (bodyPhase >= 1f) {
                moveTo(centerX - 50f * scale, centerY - 50f * scale)
                lineTo(centerX + 50f * scale, centerY - 50f * scale)
            }
        }
        drawPath(rectPath, goldColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

        if (bodyPhase >= 1f) {
            drawRect(
                color = goldColor,
                topLeft = Offset(centerX - 50f * scale, centerY - 50f * scale),
                size = Size(100f * scale, 60f * scale)
            )
        }
    }

    // ═══════════════════════════════════════════════════════════
    // FASE 4: Asas Unificadas (0.60 - 0.80)
    // ═══════════════════════════════════════════════════════════
    val handlePhase = ((progress - 0.60f) / 0.20f).coerceIn(0f, 1f)
    if (handlePhase > 0f) {
        val leftHandle = Path().apply {
            moveTo(centerX - 50f * scale, centerY - 40f * scale)
            cubicTo(
                centerX - 100f * scale, centerY - 40f * scale,
                centerX - 100f * scale, centerY + 45f * scale * handlePhase,
                centerX - 35f * scale, centerY + 45f * scale * handlePhase
            )
        }
        drawPath(leftHandle, goldColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))

        val rightHandle = Path().apply {
            moveTo(centerX + 50f * scale, centerY - 40f * scale)
            cubicTo(
                centerX + 100f * scale, centerY - 40f * scale,
                centerX + 100f * scale, centerY + 45f * scale * handlePhase,
                centerX + 35f * scale, centerY + 45f * scale * handlePhase
            )
        }
        drawPath(rightHandle, goldColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
    }

    // ═══════════════════════════════════════════════════════════
    // FASE 5: Número 1 (0.80 - 0.90)
    // ═══════════════════════════════════════════════════════════
    val numPhase = ((progress - 0.80f) / 0.10f).coerceIn(0f, 1f)
    if (numPhase > 0f) {
        val textLayoutResult = textMeasurer.measure(
            text = "1",
            style = TextStyle(
                fontSize = (28 * scale).sp,
                fontWeight = FontWeight.Black,
                color = Color.Black.copy(alpha = numPhase)
            )
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                centerX - textLayoutResult.size.width / 2,
                centerY - 10f * scale - textLayoutResult.size.height / 2
            )
        )
    }

    // ═══════════════════════════════════════════════════════════
    // FASE 6: Confeti Uno a Uno (0.85 - 1.0)
    // ═══════════════════════════════════════════════════════════
    if (progress > 0.85f) {
        val confetiProgress = ((progress - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val random = Random(123)
        val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, Color(0xFFFFA500))
        
        val totalConfetti = 80
        repeat(totalConfetti) { i ->
            val delay = i.toFloat() / totalConfetti
            val individualProgress = ((confetiProgress - delay * 0.7f) / 0.3f).coerceIn(0f, 1f)
            
            if (individualProgress > 0f) {
                val rx = centerX + (random.nextFloat() - 0.5f) * 550f * scale
                val ryBase = centerY - 150f * scale - random.nextFloat() * 250f * scale
                val ry = ryBase + (individualProgress * 150f * scale)
                
                drawCircle(
                    color = colors.random(random).copy(alpha = individualProgress),
                    radius = (2f + random.nextFloat() * 3f) * scale,
                    center = Offset(rx, ry)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 400, heightDp = 900)
@Composable
fun TrofeoScreenPreview() {
    TrofeoScreen(onBack = {})
}
