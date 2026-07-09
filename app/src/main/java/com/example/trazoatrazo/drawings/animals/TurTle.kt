package com.example.trazoatrazo.drawings.animals

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Paleta de Colores ─────────────────────────────────────────────────────────
private val BgWater = Color(0xFF003366) // Azul clarito para que el texto resalte
private val ShellBrown = Color(0xFF8B4513)
private val ShellLight = Color(0xFFA0522D)
private val ShellDark = Color(0xFF5D2E0A)
private val SkinGreen = Color(0xFF90EE90)
private val SkinDark = Color(0xFF2E7D32)
private val EyeBlack = Color(0xFF111111)

// ... (Las funciones drawTurtleHead, drawTurtleLimbs y drawTurtleShell se mantienen igual)
private fun DrawScope.drawTurtleHead(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val headR = 26f * scale * p
    val offsetHeadY = -100f * scale * p

    val neckWidth = 22f * scale * p
    val neckHeight = 45f * scale * p
    drawRect(
        color = SkinGreen,
        topLeft = Offset(cx - neckWidth / 2, cy - 75f * scale * p),
        size = Size(neckWidth, neckHeight)
    )

    drawCircle(
        color = SkinGreen,
        radius = headR,
        center = Offset(cx, cy + offsetHeadY)
    )
    drawCircle(
        color = SkinDark,
        radius = headR,
        center = Offset(cx, cy + offsetHeadY),
        style = Stroke(width = 2.5f * scale)
    )

    if (p > 0.8f) {
        val eyeProg = ((p - 0.8f) * 5f).coerceIn(0f, 1f)
        drawCircle(
            Color.White.copy(alpha = eyeProg),
            5f * scale,
            Offset(cx - 11f * scale, cy + offsetHeadY - 4f * scale)
        )
        drawCircle(
            EyeBlack.copy(alpha = eyeProg),
            2.5f * scale,
            Offset(cx - 11f * scale, cy + offsetHeadY - 4f * scale)
        )
        drawCircle(
            Color.White.copy(alpha = eyeProg),
            5f * scale,
            Offset(cx + 11f * scale, cy + offsetHeadY - 4f * scale)
        )
        drawCircle(
            EyeBlack.copy(alpha = eyeProg),
            2.5f * scale,
            Offset(cx + 11f * scale, cy + offsetHeadY - 4f * scale)
        )
    }
}

private fun DrawScope.drawTurtleLimbs(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val legW = 38f * scale * p
    val legH = 28f * scale * p

    withTransform({ rotate(35f, Offset(cx + 55f * scale * p, cy + 65f * scale * p)) }) {
        drawOval(SkinGreen, Offset(cx + 40f * scale * p, cy + 55f * scale * p), Size(legW, legH))
        drawOval(
            SkinDark,
            Offset(cx + 40f * scale * p, cy + 55f * scale * p),
            Size(legW, legH),
            style = Stroke(2f * scale)
        )
    }
    withTransform({ rotate(-35f, Offset(cx - 55f * scale * p, cy + 65f * scale * p)) }) {
        drawOval(SkinGreen, Offset(cx - 78f * scale * p, cy + 55f * scale * p), Size(legW, legH))
        drawOval(
            SkinDark,
            Offset(cx - 78f * scale * p, cy + 55f * scale * p),
            Size(legW, legH),
            style = Stroke(2f * scale)
        )
    }
    withTransform({ rotate(-40f, Offset(cx + 60f * scale * p, cy - 65f * scale * p)) }) {
        drawOval(SkinGreen, Offset(cx + 45f * scale * p, cy - 75f * scale * p), Size(legW, legH))
        drawOval(
            SkinDark,
            Offset(cx + 45f * scale * p, cy - 75f * scale * p),
            Size(legW, legH),
            style = Stroke(2f * scale)
        )
    }
    withTransform({ rotate(40f, Offset(cx - 60f * scale * p, cy - 65f * scale * p)) }) {
        drawOval(SkinGreen, Offset(cx - 83f * scale * p, cy - 75f * scale * p), Size(legW, legH))
        drawOval(
            SkinDark,
            Offset(cx - 83f * scale * p, cy - 75f * scale * p),
            Size(legW, legH),
            style = Stroke(2f * scale)
        )
    }

    val tailPath = Path().apply {
        moveTo(cx, cy + 80f * scale * p)
        lineTo(cx - 12f * scale * p, cy + 105f * scale * p)
        lineTo(cx + 12f * scale * p, cy + 105f * scale * p)
        close()
    }
    drawPath(tailPath, color = SkinGreen)
    drawPath(tailPath, color = SkinDark, style = Stroke(width = 2f * scale))
}

private fun DrawScope.drawTurtleShell(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val shellRadiusX = 80f * scale * p
    val shellRadiusY = 95f * scale * p

    drawOval(
        Color.Black.copy(0.15f * p),
        Offset(cx - shellRadiusX + 5f, cy - shellRadiusY + 5f),
        Size(shellRadiusX * 2, shellRadiusY * 2)
    )
    drawOval(
        ShellBrown,
        Offset(cx - shellRadiusX, cy - shellRadiusY),
        Size(shellRadiusX * 2, shellRadiusY * 2)
    )

    if (p > 0.4f) {
        val patternProg = ((p - 0.4f) * 1.6f).coerceIn(0f, 1f)
        val strokeW = 3.5f * scale
        drawCircle(
            ShellLight.copy(alpha = patternProg),
            38f * scale * patternProg,
            Offset(cx, cy),
            style = Stroke(strokeW)
        )
        for (i in 0 until 6) {
            val angle = (i * 60f - 90f) * (PI.toFloat() / 180f)
            val start = Offset(cx + 38f * scale * cos(angle), cy + 38f * scale * sin(angle))
            val end = Offset(cx + shellRadiusX * cos(angle), cy + shellRadiusY * sin(angle))
            drawLine(ShellLight.copy(alpha = patternProg), start, end, strokeWidth = strokeW)
        }
    }
    drawOval(
        ShellDark,
        Offset(cx - shellRadiusX, cy - shellRadiusY),
        Size(shellRadiusX * 2, shellRadiusY * 2),
        style = Stroke(5.5f * scale)
    )
}

@Composable
fun TurtleScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val shellAnim = remember { Animatable(0f) }
    val limbsAnim = remember { Animatable(0f) }
    val headAnim = remember { Animatable(0f) }

    // Animación de flotado infinita
    val infiniteTransition = rememberInfiniteTransition(label = "flotado")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movimiento"
    )

    LaunchedEffect(repetir) {
        etapa = 0
        shellAnim.snapTo(0f)
        limbsAnim.snapTo(0f)
        headAnim.snapTo(0f)
        delay(500L)

        etapa = 1
        shellAnim.animateTo(1f, tween(1100, easing = EaseOutBack))
        etapa = 2
        limbsAnim.animateTo(1f, tween(900, easing = EaseOutCubic))
        etapa = 3
        headAnim.animateTo(1f, tween(850, easing = EaseOutBack))
        delay(300L)
        etapa = 4
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWater) // Ahora el fondo es azul claro
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            // Si el dibujo terminó, aplicamos el floatingOffset al centro Y
            val currentCy = (size.height * 0.45f) + if (etapa >= 4) floatingOffset else 0f
            val scale = size.width / 420f

            if (etapa >= 2) drawTurtleLimbs(cx, currentCy, scale, limbsAnim.value)
            if (etapa >= 3) drawTurtleHead(cx, currentCy, scale, headAnim.value)
            if (etapa >= 1) drawTurtleShell(cx, currentCy, scale, shellAnim.value)
        }

        val context = LocalContext.current
        val message = "🐢 ¡Una linda tortuga! 🐢"
        val subMessage = "Hace referencia a la tortuga que te di XD y al Recuerdo figura de tortuga" +
                " de la galeria de recuerdos "

        DrawingButtons(
            visible = etapa >= 4,
            message = message,
            subMessage = subMessage,
            repeatEmoji = "🔄",
            accentColor = SkinDark.adaptiveColorFor(BgWater),
            backgroundColor = BgWater,
            onRepeat = { repetir++ },
            onBack = onBack,
            onSave = { includeText ->
                saveTurtleAsImage(
                    context,
                    message,
                    subMessage,
                    BgWater,
                    includeText
                )
            }
        )

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = SkinDark.adaptiveColorFor(BgWater))
        }
    }
}

fun saveTurtleAsImage(
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
            val cy = artSize * 0.45f
            val scale = artSize / 420f
            
            drawTurtleLimbs(cx, cy, scale, 1f)
            drawTurtleHead(cx, cy, scale, 1f)
            drawTurtleShell(cx, cy, scale, 1f)
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TurtleScreenPreview() {
    TurtleScreen(onBack = {})
}
