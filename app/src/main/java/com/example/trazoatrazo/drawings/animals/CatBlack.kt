package com.example.trazoatrazo.drawings.animals

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.utils.ImageUtils
import com.example.trazoatrazo.utils.adaptiveColorFor
import com.example.trazoatrazo.utils.subtitleColorFor
import com.example.trazoatrazo.utils.textColorFor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Colores Refinados ──────────────────────────────────────────────
private val CatColor = Color(0xFF1C1C1C)      // Negro azabache
private val CatSecondary = Color(0xFF263238)  // Color muy oscuro para que el texto resalte
private val EyeGreen = Color(0xFFC5E1A5)      // Verde lima suave
private val InnerEar = Color(0xFFFFAB91)      // Coral suave para orejas
private val BgColor = Color(0xFF90EE90)       // Crema muy claro (máximo contraste para el texto)

// ── Cuerpo Sentado ──────────────────────────────────────────────────
private fun DrawScope.drawCatBody(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress

    val bodyY = cy + 20f * scale

    // Cuerpo tipo pera (sentado) con escala suave
    withTransform({
        scale(p, p, Offset(cx, bodyY + 100f * scale))
    }) {
        val bodyPath = Path().apply {
            moveTo(cx - 35f * scale, bodyY)
            quadraticTo(cx - 75f * scale, bodyY + 110f * scale, cx, bodyY + 115f * scale)
            quadraticTo(cx + 75f * scale, bodyY + 110f * scale, cx + 35f * scale, bodyY)
            close()
        }
        drawPath(bodyPath, CatColor)

        // Líneas de las patas delanteras
        if (p > 0.8f) {
            val lineP = ((p - 0.8f) * 5f).coerceIn(0f, 1f)
            drawLine(
                Color.Black.copy(alpha = 0.4f),
                Offset(cx - 20f * scale, bodyY + 50f * scale),
                Offset(cx - 20f * scale, bodyY + (50f + 55f * lineP) * scale),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                Color.Black.copy(alpha = 0.4f),
                Offset(cx + 20f * scale, bodyY + 50f * scale),
                Offset(cx + 20f * scale, bodyY + (50f + 55f * lineP) * scale),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )
        }
    }
}

// ── Cabeza y Rostro Animado ──────────────────────────────────────────
private fun DrawScope.drawCatHead(
    cx: Float, 
    cy: Float, 
    scale: Float, 
    progress: Float,
    earWiggle: Float
) {
    if (progress <= 0f) return
    val p = progress
    val headY = cy - 40f * scale

    withTransform({
        scale(p, p, Offset(cx, headY))
        rotate(earWiggle * 2f, Offset(cx, headY)) // Pequeña rotación de la cabeza
    }) {
        // Orejas con animación de "wiggle"
        val drawEar = { isLeft: Boolean ->
            val sign = if (isLeft) -1f else 1f
            val wiggle = if (isLeft) -earWiggle else earWiggle
            
            withTransform({
                rotate(wiggle * 12f, Offset(cx + (40f * sign) * scale, headY - 20f * scale))
            }) {
                val earPath = Path().apply {
                    moveTo(cx + (25f * sign) * scale, headY - 45f * scale)
                    lineTo(cx + (70f * sign) * scale, headY - 105f * scale)
                    lineTo(cx + (85f * sign) * scale, headY - 15f * scale)
                    close()
                }
                drawPath(earPath, CatColor)

                val innerEarPath = Path().apply {
                    moveTo(cx + (35f * sign) * scale, headY - 50f * scale)
                    lineTo(cx + (60f * sign) * scale, headY - 85f * scale)
                    lineTo(cx + (65f * sign) * scale, headY - 30f * scale)
                    close()
                }
                drawPath(innerEarPath, InnerEar)
            }
        }
        drawEar(true)
        drawEar(false)

        // Cabeza ovalada
        drawOval(
            color = CatColor,
            topLeft = Offset(cx - 100f * scale, headY - 75f * scale),
            size = Size(200f * scale, 150f * scale)
        )

        // Ojos Gigantes
        if (p > 0.7f) {
            val eyeP = ((p - 0.7f) * 3.3f).coerceIn(0f, 1f)
            val eyeSize = 60f * scale * eyeP
            
            val drawEye = { xPos: Float ->
                drawCircle(EyeGreen, eyeSize / 2f, Offset(xPos, headY + 5f * scale))
                drawCircle(Color.Black, eyeSize / 2.8f, Offset(xPos, headY + 5f * scale))
                drawCircle(Color.White, eyeSize / 8f, Offset(xPos - 10f * scale, headY - 6f * scale))
                drawCircle(Color.White, eyeSize / 15f, Offset(xPos + 12f * scale, headY + 12f * scale))
            }
            drawEye(cx - 48f * scale)
            drawEye(cx + 48f * scale)
        }

        // Nariz y boca ":3" (Ahora mucho más visible)
        if (p > 0.85f) {
            val mP = ((p - 0.85f) * 6f).coerceIn(0f, 1f)
            // Nariz
            drawCircle(Color.Black, 5f * scale * mP, Offset(cx, headY + 28f * scale))

            // Boca w - Color más claro para resaltar sobre el negro
            val mouthPath = Path().apply {
                moveTo(cx - 15f * scale * mP, headY + 38f * scale)
                quadraticTo(cx - 8f * scale, headY + 50f * scale, cx, headY + 38f * scale)
                quadraticTo(cx + 8f * scale, headY + 50f * scale, cx + 15f * scale * mP, headY + 38f * scale)
            }
            drawPath(
                mouthPath, 
                color = CatSecondary, 
                style = Stroke(5f * scale, cap = StrokeCap.Round)
            )
        }
    }
}

// ── Cola Más Pequeña y Curvada ──────────────────────────────────────
private fun DrawScope.drawCatTail(cx: Float, cy: Float, scale: Float, progress: Float, swing: Float) {
    if (progress <= 0f) return
    val p = progress

    // Cola más corta y curvada hacia arriba, saliendo desde atrás
    val tailPath = Path().apply {
        moveTo(cx - 30f * scale, cy + 95f * scale)
        quadraticTo(
            cx - 70f * scale - (swing * 10f), cy + 100f * scale,
            cx - 85f * scale - (swing * 5f), cy + 60f * scale
        )
        quadraticTo(
            cx - 95f * scale + (swing * 15f), cy + 30f * scale,
            cx - 65f * scale + (swing * 10f), cy + 35f * scale
        )
    }
    
    drawPath(
        path = tailPath,
        color = CatColor,
        style = Stroke(width = 15f * scale, cap = StrokeCap.Round)
    )
}

@Composable
fun CatBlackScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val bodyAnim = remember { Animatable(0f) }
    val headAnim = remember { Animatable(0f) }
    val tailAnim = remember { Animatable(0f) }
    
    // Animación de las orejas y cara (wiggle)
    val earWiggleAnim = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "tail")
    val tailSwing by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "swing"
    )

    LaunchedEffect(repetir) {
        etapa = 0
        bodyAnim.snapTo(0f); headAnim.snapTo(0f); tailAnim.snapTo(0f)
        delay(300)
        
        // 1. Cuerpo
        etapa = 1
        bodyAnim.animateTo(1f, tween(700, easing = EaseOutBack))
        
        // 2. Cabeza + Animación de orejas al aparecer
        etapa = 2
        launch {
            // Un par de sacudidas tiernas al aparecer
            repeat(2) {
                earWiggleAnim.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
                earWiggleAnim.animateTo(-1f, tween(200, easing = FastOutSlowInEasing))
            }
            earWiggleAnim.animateTo(0f, tween(200))
        }
        headAnim.animateTo(1f, tween(800, easing = EaseOutBack))
        
        // 3. Cola
        etapa = 3
        tailAnim.animateTo(1f, tween(700, easing = EaseOutCubic))
        etapa = 4
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.48f
            val scale = size.width / 400f

            // Cola detrás
            if (etapa >= 3) drawCatTail(cx, cy, scale, tailAnim.value, if(etapa >= 4) tailSwing else 0f)
            
            // Cuerpo
            if (etapa >= 1) drawCatBody(cx, cy, scale, bodyAnim.value)
            
            // Cabeza
            if (etapa >= 2) drawCatHead(
                cx, cy, scale, 
                headAnim.value, 
                if(etapa >= 4) tailSwing * 0.25f else earWiggleAnim.value
            )
        }

        val context = LocalContext.current
        val message = "🐈 Un Gatito muy tierno"
        val subMessage = " :O "

        DrawingButtons(
            visible = etapa >= 4,
            message = message,
            subMessage = subMessage,
            repeatEmoji = "🐾",
            accentColor = CatSecondary.adaptiveColorFor(BgColor),
            backgroundColor = BgColor,
            onRepeat = { repetir++ },
            onBack = onBack,
            onSave = { includeText ->
                saveCatAsImage(
                    context,
                    message,
                    subMessage,
                    BgColor,
                    includeText
                )
            }
        )

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = CatSecondary.adaptiveColorFor(BgColor))
        }
    }
}

fun saveCatAsImage(
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
            
            val cx = artSize / 2f
            val cy = artSize * 0.48f
            val scale = artSize / 400f
            
            drawCatTail(cx, cy, scale, 1f, 0f)
            drawCatBody(cx, cy, scale, 1f)
            drawCatHead(cx, cy, scale, 1f, 0f)
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
