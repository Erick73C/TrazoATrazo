package com.example.trazoatrazo.drawings.shapes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.ui.unit.dp
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.utils.ImageUtils
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlin.io.path.Path
import kotlin.io.path.moveTo

@Composable
fun HarleyScreen(onBack: () -> Unit) {
    var repetir by remember { mutableIntStateOf(0) }
    var etapa by remember { mutableIntStateOf(0) }

    val screenAnim = remember { Animatable(0f) }
    val headAnim = remember { Animatable(0f) }
    val earsAnim = remember { Animatable(0f) }
    val faceAnim = remember { Animatable(0f) }
    val eyesAnim = remember { Animatable(0f) }
    val collarAnim = remember { Animatable(0f) }
    val bodyAnim = remember { Animatable(0f) }
    val armsAnim = remember { Animatable(0f) }
    val hammerAnim = remember { Animatable(0f) }
    val feetAnim = remember { Animatable(0f) }

    LaunchedEffect(repetir) {
        etapa = 0
        screenAnim.snapTo(0f)
        headAnim.snapTo(0f)
        earsAnim.snapTo(0f)
        faceAnim.snapTo(0f)
        eyesAnim.snapTo(0f)
        collarAnim.snapTo(0f)
        bodyAnim.snapTo(0f)
        armsAnim.snapTo(0f)
        hammerAnim.snapTo(0f)
        feetAnim.snapTo(0f)

        delay(300L)
        screenAnim.animateTo(1f, tween(400, easing = EaseOutBack))

        // Etapa 1: Cabeza
        headAnim.animateTo(1f, tween(700, easing = EaseOutCubic))
        etapa = 1
        delay(100L)

        // Etapa 2: Orejas
        earsAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
        etapa = 2
        delay(100L)

        // Etapa 3: Cara y antifaz
        faceAnim.animateTo(1f, tween(600, easing = EaseOutCubic))
        etapa = 3
        delay(100L)

        // Etapa 4: Ojos
        eyesAnim.animateTo(1f, tween(400, easing = EaseOutCubic))
        etapa = 4
        delay(100L)

        // Etapa 5: Cuello
        collarAnim.animateTo(1f, tween(400, easing = EaseOutCubic))
        etapa = 5
        delay(100L)

        // Etapa 6: Cuerpo
        bodyAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
        etapa = 6
        delay(100L)

        // Etapa 7: Brazos
        armsAnim.animateTo(1f, tween(600, easing = EaseOutCubic))
        etapa = 7
        delay(100L)

        // Etapa 8: Mazo
        hammerAnim.animateTo(1f, tween(600, easing = EaseOutCubic))
        etapa = 8
        delay(100L)

        // Etapa 9: Pies
        feetAnim.animateTo(1f, tween(400, easing = EaseOutCubic))
        etapa = 9
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
            drawHarleyQuinnAnimated(
                centerX = size.width / 2,
                centerY = size.height / 2.3f,
                scale = 1.8f,
                headAnim = headAnim.value,
                earsAnim = earsAnim.value,
                faceAnim = faceAnim.value,
                eyesAnim = eyesAnim.value,
                collarAnim = collarAnim.value,
                bodyAnim = bodyAnim.value,
                armsAnim = armsAnim.value,
                hammerAnim = hammerAnim.value,
                feetAnim = feetAnim.value
            )
        }

        BackMenuButton(onBack = onBack)

        val context = LocalContext.current
        val message = "Harley Quinn"
        val subMessage = ""
        val bgColor = Color.Gray

        DrawingButtons(
            visible = true,
            onRepeat = { repetir++ },
            onBack = onBack,
            message = message,
            subMessage = subMessage,
            backgroundColor = bgColor,
            onSave = { includeText ->
                saveHarleyAsImage(
                    context,
                    message,
                    subMessage,
                    bgColor,
                    includeText
                )
            }
        )
    }
}

fun saveHarleyAsImage(
    context: android.content.Context,
    message: String,
    subMessage: String,
    bgColor: Color,
    includeText: Boolean
) {
    try {
        val artSize = 1024
        val footerHeight = if (includeText) 180 else 0
        val bitmap = Bitmap.createBitmap(artSize, artSize + footerHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        val drawScope = CanvasDrawScope()
        val size = Size(artSize.toFloat(), artSize.toFloat())
        
        drawScope.draw(
            density = androidx.compose.ui.unit.Density(context),
            layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr,
            canvas = androidx.compose.ui.graphics.Canvas(canvas),
            size = size
        ) {
            drawRect(color = bgColor, size = size)
            drawHarleyQuinnAnimated(
                centerX = artSize / 2f,
                centerY = artSize / 2.3f,
                scale = 2.5f,
                headAnim = 1f,
                earsAnim = 1f,
                faceAnim = 1f,
                eyesAnim = 1f,
                collarAnim = 1f,
                bodyAnim = 1f,
                armsAnim = 1f,
                hammerAnim = 1f,
                feetAnim = 1f
            )
        }

        if (includeText) {
            val paint = Paint()
            paint.color = bgColor.toArgb()
            canvas.drawRect(0f, artSize.toFloat(), artSize.toFloat(), (artSize + footerHeight).toFloat(), paint)
            ImageUtils.drawFooterText(canvas, artSize, artSize.toFloat(), bgColor, message, subMessage)
        }

        ImageUtils.saveBitmapToGallery(context, bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// ── FUNCIÓN: Dibujar Harley Quinn CON ANIMACIONES ──
fun DrawScope.drawHarleyQuinnAnimated(
    centerX: Float,
    centerY: Float,
    scale: Float = 1f,
    headAnim: Float,
    earsAnim: Float,
    faceAnim: Float,
    eyesAnim: Float,
    collarAnim: Float,
    bodyAnim: Float,
    armsAnim: Float,
    hammerAnim: Float,
    feetAnim: Float
) {
    // ───── COLORES ─────
    val harleyRed = Color(0xFFE63946)
    val harleyBlack = Color(0xFF1A1A1A)
    val woodColor = Color(0xFFD2913C)

    val headWidth = 220f * scale
    val headHeight = 160f * scale
    val headTop = centerY - 110f * scale
    val headCorner = 32f * scale
    val squarePart = 70f * scale

    // ═════════════════════════════════════════════════════════════
    // 1. CABEZA (Con animación de escala)
    // ═════════════════════════════════════════════════════════════
    if (headAnim > 0f) {
        val scaleHead = headAnim

        // Parte superior CUADRADA (Izquierda - Roja)
        drawRect(
            color = harleyRed.copy(alpha = scaleHead),
            topLeft = Offset(centerX - headWidth / 2, headTop),
            size = Size(headWidth / 2, squarePart)
        )
        // Parte superior CUADRADA (Derecha - Negra)
        drawRect(
            color = harleyBlack.copy(alpha = scaleHead),
            topLeft = Offset(centerX, headTop),
            size = Size(headWidth / 2, squarePart)
        )

        // Parte redondeada inferior (Izquierda - Roja)
        drawRoundRect(
            color = harleyRed.copy(alpha = scaleHead),
            topLeft = Offset(centerX - headWidth / 2, headTop + squarePart),
            size = Size(headWidth / 2, headHeight - squarePart),
            cornerRadius = CornerRadius(headCorner, headCorner)
        )
        // Parte redondeada inferior (Derecha - Negra)
        drawRoundRect(
            color = harleyBlack.copy(alpha = scaleHead),
            topLeft = Offset(centerX, headTop + squarePart),
            size = Size(headWidth / 2, headHeight - squarePart),
            cornerRadius = CornerRadius(headCorner, headCorner)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // 2. OREJAS (Con fade in)
    // ═════════════════════════════════════════════════════════════
    if (earsAnim > 0f) {
        // Oreja izquierda (Roja)
        val leftEarPath = Path().apply {
            moveTo(centerX - headWidth / 2 + 20f * scale, headTop - 4f * scale)
            quadraticBezierTo(
                centerX - headWidth / 2 - 65f * scale, headTop - 0f * scale,
                centerX - headWidth / 2 - 50f * scale, headTop + 100f * scale
            )
            lineTo(centerX - headWidth / 2 + 10f * scale, headTop + 90f * scale)
        }
        drawPath(leftEarPath, harleyRed.copy(alpha = earsAnim))
        drawCircle(Color.White.copy(alpha = earsAnim), radius = 15f * scale, center = Offset(centerX - headWidth / 2 - 55f * scale, headTop + 115f * scale))

        // Oreja derecha (Negra)
        val rightEarPath = Path().apply {
            moveTo(centerX + headWidth / 2 - 20f * scale, headTop - 4f * scale)
            quadraticBezierTo(
                centerX + headWidth / 2 + 65f * scale, headTop - 0f * scale,
                centerX + headWidth / 2 + 50f * scale, headTop + 100f * scale
            )
            lineTo(centerX + headWidth / 2 - 10f * scale, headTop + 90f * scale)
        }
        drawPath(rightEarPath, harleyBlack.copy(alpha = earsAnim))
        drawCircle(Color.White.copy(alpha = earsAnim), radius = 15f * scale, center = Offset(centerX + headWidth / 2 + 55f * scale, headTop + 115f * scale))
    }

    // ═════════════════════════════════════════════════════════════
    // 3. CARA BLANCA Y ANTIFAZ
    // ═════════════════════════════════════════════════════════════
    if (faceAnim > 0f) {
        val faceWidth = 185f * scale
        val faceHeight = 110f * scale
        drawRoundRect(
            color = Color.White.copy(alpha = faceAnim),
            topLeft = Offset(centerX - faceWidth / 2, headTop + 55f * scale),
            size = Size(faceWidth, faceHeight),
            cornerRadius = CornerRadius(55f * scale, 55f * scale)
        )

        // Antifaz
        val maskPath = Path().apply {
            moveTo(centerX - 85f * scale, headTop + 90f * scale)
            quadraticBezierTo(centerX - 95f * scale, headTop + 80f * scale, centerX - 85f * scale, headTop + 70f * scale)
            quadraticBezierTo(centerX, headTop + 60f * scale, centerX + 85f * scale, headTop + 70f * scale)
            quadraticBezierTo(centerX + 95f * scale, headTop + 80f * scale, centerX + 85f * scale, headTop + 90f * scale)
            lineTo(centerX + 75f * scale, headTop + 130f * scale)
            quadraticBezierTo(centerX, headTop + 115f * scale, centerX - 75f * scale, headTop + 130f * scale)
            lineTo(centerX - 85f * scale, headTop + 90f * scale)
            close()
        }
        drawPath(maskPath, Color(0xFF151515).copy(alpha = faceAnim))
    }

    // ═════════════════════════════════════════════════════════════
    // 4. OJOS
    // ═════════════════════════════════════════════════════════════
    if (eyesAnim > 0f) {
        drawArc(
            color = Color.White.copy(alpha = eyesAnim),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset(centerX - 55f * scale, headTop + 95f * scale),
            size = Size(20f * scale, 12f * scale)
        )
        drawArc(
            color = Color.White.copy(alpha = eyesAnim),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = Offset(centerX + 35f * scale, headTop + 95f * scale),
            size = Size(20f * scale, 12f * scale)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // 5. CUELLO
    // ═════════════════════════════════════════════════════════════
    if (collarAnim > 0f) {
        val collarPath = Path().apply {
            moveTo(centerX - 75f * scale, headTop + headHeight - 5f * scale)
            lineTo(centerX - 45f * scale, headTop + headHeight + 45f * scale)
            lineTo(centerX, headTop + headHeight + 15f * scale)
            lineTo(centerX + 45f * scale, headTop + headHeight + 45f * scale)
            lineTo(centerX + 75f * scale, headTop + headHeight - 5f * scale)
        }
        drawPath(collarPath, Color.LightGray.copy(alpha = collarAnim))
    }

    // ═════════════════════════════════════════════════════════════
    // 6. CUERPO
    // ═════════════════════════════════════════════════════════════
    if (bodyAnim > 0f) {
        val bodyWidth = 130f * scale
        val bodyHeight = 90f * scale
        val bodyTop = headTop + headHeight + 5f * scale

        drawRect(
            color = harleyBlack.copy(alpha = bodyAnim),
            topLeft = Offset(centerX - bodyWidth / 2, bodyTop),
            size = Size(bodyWidth / 2, bodyHeight)
        )
        drawRect(
            color = harleyRed.copy(alpha = bodyAnim),
            topLeft = Offset(centerX, bodyTop),
            size = Size(bodyWidth / 2, bodyHeight)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // 7. BRAZOS
    // ═════════════════════════════════════════════════════════════
    if (armsAnim > 0f) {
        val bodyWidth = 130f * scale
        val bodyTop = headTop + headHeight + 5f * scale
        val handleY = bodyTop + 60f * scale

        // Brazo izquierdo
        val leftArmPath = Path().apply {
            moveTo(centerX - bodyWidth / 2 + 5f * scale, bodyTop + 25f * scale)
            quadraticBezierTo(
                centerX - bodyWidth / 2 - 35f * scale, handleY,
                centerX - 50f * scale, handleY
            )
        }
        drawPath(leftArmPath, harleyBlack.copy(alpha = armsAnim), style = Stroke(width = 26f * scale, cap = StrokeCap.Round))
        drawCircle(Color.White.copy(alpha = armsAnim), radius = 14f * scale, center = Offset(centerX - 55f * scale, handleY))

        // Brazo derecho
        val rightArmPath = Path().apply {
            moveTo(centerX + bodyWidth / 2 - 5f * scale, bodyTop + 25f * scale)
            quadraticBezierTo(
                centerX + bodyWidth / 2 + 25f * scale, handleY,
                centerX + 80f * scale, handleY
            )
        }
        drawPath(rightArmPath, harleyRed.copy(alpha = armsAnim), style = Stroke(width = 26f * scale, cap = StrokeCap.Round))
        drawCircle(Color.White.copy(alpha = armsAnim), radius = 14f * scale, center = Offset(centerX + 85f * scale, handleY))
    }

    // ═════════════════════════════════════════════════════════════
    // 8. MAZO
    // ═════════════════════════════════════════════════════════════
    if (hammerAnim > 0f) {
        val bodyTop = headTop + headHeight + 5f * scale
        val handleY = bodyTop + 60f * scale
        val hammerSize = 115f * scale

        // Mango
        drawLine(
            color = woodColor.copy(alpha = hammerAnim),
            start = Offset(centerX - 100f * scale, handleY),
            end = Offset(centerX + 130f * scale, handleY),
            strokeWidth = 16f * scale,
            cap = StrokeCap.Round
        )

        // Cabeza del mazo
        drawRoundRect(
            color = woodColor.copy(alpha = hammerAnim),
            topLeft = Offset(centerX + 85f * scale, bodyTop - 15f * scale),
            size = Size(hammerSize, hammerSize + 30f * scale),
            cornerRadius = CornerRadius(15f * scale, 15f * scale)
        )
    }

    // ═════════════════════════════════════════════════════════════
    // 9. PIES
    // ═════════════════════════════════════════════════════════════
    if (feetAnim > 0f) {
        val bodyWidth = 130f * scale
        val bodyTop = headTop + headHeight + 5f * scale
        val bodyHeight = 90f * scale

        drawRoundRect(
            color = harleyBlack.copy(alpha = feetAnim),
            topLeft = Offset(centerX - bodyWidth / 2 - 15f * scale, bodyTop + bodyHeight - 10f * scale),
            size = Size(bodyWidth / 2 + 10f * scale, 40f * scale),
            cornerRadius = CornerRadius(20f * scale, 20f * scale)
        )
        drawRoundRect(
            color = harleyRed.copy(alpha = feetAnim),
            topLeft = Offset(centerX + 5f * scale, bodyTop + bodyHeight - 10f * scale),
            size = Size(bodyWidth / 2 + 10f * scale, 40f * scale),
            cornerRadius = CornerRadius(20f * scale, 20f * scale)
        )
    }
}