package com.example.trazoatrazo.drawings.shapes

import android.graphics.DashPathEffect
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
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons

@Composable
fun BatmanScreen(onBack: () -> Unit) {
    var repetir by remember { mutableIntStateOf(0) }
    val screenAnim = remember { Animatable(0f) }
    val drawProgress = remember { Animatable(0f) }

    LaunchedEffect(repetir) {
        screenAnim.snapTo(0f)
        drawProgress.snapTo(0f)

        screenAnim.animateTo(1f, tween(400, easing = EaseOutBack))
        drawProgress.animateTo(1f, tween(3500, easing = LinearEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAnim.value)
                .offset(y = ((1f - screenAnim.value) * 30).dp)
        ) {
            drawBatmanAnimated(
                centerX = size.width / 2,
                centerY = size.height / 2.5f,
                scale = 1.9f,
                progress = drawProgress.value
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = true,
                message = "🦇El señor de la noche :O🦇",
                subMessage = "Referencia al peluche que te di por tu cumpleaños del año pasado",
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color.White)
        }
    }
}

fun DrawScope.drawBatmanAnimated(
    centerX: Float,
    centerY: Float,
    scale: Float = 1f,
    progress: Float = 1f
) {
    // ───── COLORES ─────
    val batBlack = Color(0xFF1A1C22)
    val batGrayBody = Color(0xFF8A8E94)
    val batSkin = Color(0xFFE5C29F)
    val batYellow = Color(0xFFFFD700)
    val batWhite = Color.White

    // ───── CAPA (ATRÁS) ─────
    if (progress > 0f) {
        val capePath = Path().apply {
            moveTo(centerX - 30f * scale, centerY + 50f * scale)
            cubicTo(centerX - 100f * scale, centerY + 60f * scale, centerX - 160f * scale, centerY + 140f * scale, centerX - 180f * scale, centerY + 200f * scale)
            lineTo(centerX - 120f * scale, centerY + 170f * scale)
            lineTo(centerX - 80f * scale, centerY + 200f * scale)
            lineTo(centerX - 40f * scale, centerY + 175f * scale)
            lineTo(centerX, centerY + 205f * scale)
            lineTo(centerX + 40f * scale, centerY + 175f * scale)
            lineTo(centerX + 80f * scale, centerY + 200f * scale)
            lineTo(centerX + 30f * scale, centerY + 50f * scale)
            close()
        }
        drawPath(capePath, batBlack.copy(alpha = progress))
    }

    // ───── PIERNAS Y BOTAS ─────
    if (progress > 0.05f) {
        drawRoundRect(
            color = batGrayBody.copy(alpha = progress),
            topLeft = Offset(centerX - 35f * scale, centerY + 120f * scale),
            size = Size(25f * scale, 40f * scale),
            cornerRadius = CornerRadius(10f * scale)
        )

        val bootLeftPath = Path().apply {
            moveTo(centerX - 40f * scale, centerY + 150f * scale)
            lineTo(centerX - 10f * scale, centerY + 150f * scale)
            lineTo(centerX - 5f * scale, centerY + 175f * scale)
            quadraticBezierTo(centerX - 25f * scale, centerY + 185f * scale, centerX - 55f * scale, centerY + 175f * scale)
            close()
        }
        drawPath(bootLeftPath, batBlack.copy(alpha = progress))
    }

    if (progress > 0.1f) {
        drawRoundRect(
            color = batGrayBody.copy(alpha = progress),
            topLeft = Offset(centerX + 10f * scale, centerY + 120f * scale),
            size = Size(25f * scale, 40f * scale),
            cornerRadius = CornerRadius(10f * scale)
        )

        val bootRightPath = Path().apply {
            moveTo(centerX + 5f * scale, centerY + 150f * scale)
            lineTo(centerX + 35f * scale, centerY + 150f * scale)
            lineTo(centerX + 45f * scale, centerY + 175f * scale)
            quadraticBezierTo(centerX + 25f * scale, centerY + 185f * scale, centerX + 5f * scale, centerY + 175f * scale)
            close()
        }
        drawPath(bootRightPath, batBlack.copy(alpha = progress))
    }

    // ───── CUERPO ─────
    if (progress > 0.15f) {
        val undiesPath = Path().apply {
            moveTo(centerX - 35f * scale, centerY + 105f * scale)
            lineTo(centerX + 35f * scale, centerY + 105f * scale)
            lineTo(centerX + 25f * scale, centerY + 135f * scale)
            lineTo(centerX - 25f * scale, centerY + 135f * scale)
            close()
        }
        drawPath(undiesPath, batBlack.copy(alpha = progress))
    }

    if (progress > 0.2f) {
        val torsoPath = Path().apply {
            moveTo(centerX - 30f * scale, centerY + 60f * scale)
            lineTo(centerX + 30f * scale, centerY + 60f * scale)
            lineTo(centerX + 35f * scale, centerY + 110f * scale)
            lineTo(centerX - 35f * scale, centerY + 110f * scale)
            close()
        }
        drawPath(torsoPath, batGrayBody.copy(alpha = progress))
    }

    // ───── SÍMBOLO (PECHO) ─────
    if (progress > 0.25f) {
        val symbolPath = Path().apply {
            val sx = centerX
            val sy = centerY + 82f * scale
            val sw = 16f * scale
            val sh = 7f * scale

            moveTo(sx, sy + sh * 0.2f)
            lineTo(sx - sw * 0.2f, sy - sh * 0.6f)
            lineTo(sx - sw * 0.4f, sy - sh * 0.1f)
            lineTo(sx - sw * 0.9f, sy - sh * 0.5f)
            quadraticBezierTo(sx - sw * 0.5f, sy + sh * 0.8f, sx, sy + sh)
            quadraticBezierTo(sx + sw * 0.5f, sy + sh * 0.8f, sx + sw * 0.9f, sy - sh * 0.5f)
            lineTo(sx + sw * 0.4f, sy - sh * 0.1f)
            lineTo(sx + sw * 0.2f, sy - sh * 0.6f)
            close()
        }
        drawPath(symbolPath, batBlack.copy(alpha = progress))
    }

    // ───── BRAZOS ─────
    if (progress > 0.35f) {
        val leftArmGrayPath = Path().apply {
            moveTo(centerX - 30f * scale, centerY + 65f * scale)
            quadraticBezierTo(centerX - 55f * scale, centerY + 75f * scale, centerX - 52f * scale, centerY + 95f * scale)
            lineTo(centerX - 35f * scale, centerY + 95f * scale)
            close()
        }
        drawPath(leftArmGrayPath, batGrayBody.copy(alpha = progress))

        val leftGlovePath = Path().apply {
            moveTo(centerX - 52f * scale, centerY + 95f * scale)
            quadraticBezierTo(centerX - 65f * scale, centerY + 115f * scale, centerX - 40f * scale, centerY + 130f * scale)
            quadraticBezierTo(centerX - 25f * scale, centerY + 110f * scale, centerX - 35f * scale, centerY + 95f * scale)
            close()
        }
        drawPath(leftGlovePath, batBlack.copy(alpha = progress))
        // Borde sutil para distinguir el guante
        drawPath(
            path = leftGlovePath,
            color = Color.White.copy(alpha = 0.15f * progress),
            style = Stroke(width = 1.2f * scale)
        )
    }

    if (progress > 0.45f) {
        val rightArmGrayPath = Path().apply {
            moveTo(centerX + 30f * scale, centerY + 65f * scale)
            quadraticBezierTo(centerX + 55f * scale, centerY + 75f * scale, centerX + 52f * scale, centerY + 95f * scale)
            lineTo(centerX + 35f * scale, centerY + 95f * scale)
            close()
        }
        drawPath(rightArmGrayPath, batGrayBody.copy(alpha = progress))

        val rightGlovePath = Path().apply {
            moveTo(centerX + 52f * scale, centerY + 95f * scale)
            quadraticBezierTo(centerX + 65f * scale, centerY + 115f * scale, centerX + 40f * scale, centerY + 130f * scale)
            quadraticBezierTo(centerX + 25f * scale, centerY + 110f * scale, centerX + 35f * scale, centerY + 95f * scale)
            close()
        }
        drawPath(rightGlovePath, batBlack.copy(alpha = progress))
        // Borde sutil para distinguir el guante
        drawPath(
            path = rightGlovePath,
            color = Color.White.copy(alpha = 0.15f * progress),
            style = Stroke(width = 1.2f * scale)
        )
    }

    // ───── CINTURÓN ─────
    if (progress > 0.55f) {
        val beltPath = Path().apply {
            moveTo(centerX - 35f * scale, centerY + 105f * scale)
            lineTo(centerX + 35f * scale, centerY + 105f * scale)
            lineTo(centerX + 36f * scale, centerY + 115f * scale)
            lineTo(centerX - 36f * scale, centerY + 115f * scale)
            close()
        }
        drawPath(beltPath, batYellow.copy(alpha = progress))
    }

    // ───── CABEZA ─────
    if (progress > 0.65f) {
        val headPath = Path().apply {
            moveTo(centerX - 95f * scale, centerY - 85f * scale)
            lineTo(centerX - 110f * scale, centerY - 150f * scale)
            lineTo(centerX - 60f * scale, centerY - 105f * scale)
            lineTo(centerX + 60f * scale, centerY - 105f * scale)
            lineTo(centerX + 110f * scale, centerY - 150f * scale)
            lineTo(centerX + 95f * scale, centerY - 85f * scale)
            quadraticBezierTo(centerX + 125f * scale, centerY + 20f * scale, centerX + 75f * scale, centerY + 80f * scale)
            quadraticBezierTo(centerX, centerY + 110f * scale, centerX - 75f * scale, centerY + 80f * scale)
            quadraticBezierTo(centerX - 125f * scale, centerY + 20f * scale, centerX - 95f * scale, centerY - 85f * scale)
            close()
        }
        drawPath(headPath, batBlack.copy(alpha = progress))
    }

    // ───── CARA ─────
    if (progress > 0.75f) {
        val facePath = Path().apply {
            moveTo(centerX - 90f * scale, centerY + 15f * scale)
            quadraticBezierTo(centerX, centerY - 10f * scale, centerX + 90f * scale, centerY + 15f * scale)
            quadraticBezierTo(centerX + 85f * scale, centerY + 85f * scale, centerX, centerY + 95f * scale)
            quadraticBezierTo(centerX - 85f * scale, centerY + 85f * scale, centerX - 90f * scale, centerY + 15f * scale)
            close()
        }
        drawPath(facePath, batSkin.copy(alpha = progress))
    }

    // ───── OJOS ─────
    if (progress > 0.8f) {
        val eyeLeftPath = Path().apply {
            moveTo(centerX - 75f * scale, centerY - 40f * scale)
            lineTo(centerX - 25f * scale, centerY - 25f * scale)
            lineTo(centerX - 70f * scale, centerY - 15f * scale)
            close()
        }
        drawPath(eyeLeftPath, batWhite.copy(alpha = progress))

        val eyeRightPath = Path().apply {
            moveTo(centerX + 75f * scale, centerY - 40f * scale)
            lineTo(centerX + 25f * scale, centerY - 25f * scale)
            lineTo(centerX + 70f * scale, centerY - 15f * scale)
            close()
        }
        drawPath(eyeRightPath, batWhite.copy(alpha = progress))
    }

    // ───── DETALLES DE EXPRESIÓN ─────
    if (progress > 0.9f) {
        val expressionColor = batBlack.copy(alpha = 0.4f * progress)
        drawLine(
            color = expressionColor,
            start = Offset(centerX - 68f * scale, centerY - 5f * scale),
            end = Offset(centerX - 35f * scale, centerY - 8f * scale),
            strokeWidth = 1.5f * scale
        )
        drawLine(
            color = expressionColor,
            start = Offset(centerX + 68f * scale, centerY - 5f * scale),
            end = Offset(centerX + 35f * scale, centerY - 8f * scale),
            strokeWidth = 1.5f * scale
        )
        drawLine(
            color = expressionColor,
            start = Offset(centerX - 15f * scale, centerY + 55f * scale),
            end = Offset(centerX + 15f * scale, centerY + 55f * scale),
            strokeWidth = 5f * scale
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B)
@Composable
fun BatmanScreenPreview() {
    BatmanScreen(onBack = {})
}