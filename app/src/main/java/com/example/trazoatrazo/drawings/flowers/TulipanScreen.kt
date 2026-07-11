package com.example.trazoatrazo.drawings.flowers

import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Fondo ─────────────────────────────────────────────────────────────────────
private val BgDark = Color(0xFF121212)   // Fondo carbón oscuro para resaltar colores

// ── Paleta de color por modo ────────────────────────────────────────────────────
data class TulipanPalette(
    val fill: Color,        // color de relleno del cuerpo
    val fillShadow: Color,  // sombra sutil en el lóbulo izquierdo (menos luz)
    val edge: Color,        // color del trazo/contorno
    val label: String
)

private val PaletteRosa = TulipanPalette(
    fill = Color(0xFFF48FB1), fillShadow = Color(0xFFE07AA0), edge = Color(0xFF8F4A63), label = "Rosa"
)
private val PaletteRojo = TulipanPalette(
    fill = Color(0xFFE53935), fillShadow = Color(0xFFC62828), edge = Color(0xFF7A1414), label = "Rojo"
)
private val PaletteAmarillo = TulipanPalette(
    fill = Color(0xFFFFC107), fillShadow = Color(0xFFE6A800), edge = Color(0xFF8F6A00), label = "Amarillo"
)
private val PaletteAzul = TulipanPalette(
    fill = Color(0xFF42A5F5), fillShadow = Color(0xFF1E88E5), edge = Color(0xFF15558F), label = "Azul"
)

private val TulipanPaletteCycle = listOf(PaletteRosa, PaletteRojo, PaletteAmarillo, PaletteAzul)

// ── Geometría mejorada: 3 pétalos superpuestos ────────────────────────────────
private const val FLOWER_H = 44f
private const val PETAL_W = 19f

/** Genera puntos para el contorno de un pétalo de tulipán desde la base (0,0) hasta la punta (0,-H) */
private fun petalOutline(steps: Int = 24): List<Offset> {
    val points = mutableListOf<Offset>()
    // Lado derecho (base -> punta)
    for (s in 0..steps) {
        val t = s / steps.toFloat()
        val bell = sin(t * PI.toFloat())
        val x = PETAL_W * (0.2f * (1f - t) + bell * 0.8f)
        val y = -FLOWER_H * t
        points.add(Offset(x, y))
    }
    // Lado izquierdo (punta -> base)
    for (s in steps downTo 0) {
        val t = s / steps.toFloat()
        val bell = sin(t * PI.toFloat())
        val x = -PETAL_W * (0.2f * (1f - t) + bell * 0.8f)
        val y = -FLOWER_H * t
        points.add(Offset(x, y))
    }
    return points
}

/** Dibuja el cuerpo del tulipán con 3 pétalos superpuestos para mayor realismo */
private fun DrawScope.drawTulipanShape(
    center: Offset,
    scale: Float,
    beatScale: Float,
    drawT: Float,
    fillT: Float,
    palette: TulipanPalette
) {
    val s = scale * beatScale
    val outline = petalOutline()
    val strokeW = max(2.5f, s * 0.12f)

    // Dividimos el progreso de trazo en 3 pétalos secuenciales
    val p1T = (drawT / 0.35f).coerceIn(0f, 1f)  // Pétalo central (atrás)
    val p2T = ((drawT - 0.35f) / 0.35f).coerceIn(0f, 1f) // Pétalo izquierdo
    val p3T = ((drawT - 0.7f) / 0.3f).coerceIn(0f, 1f)   // Pétalo derecho

    fun drawPetal(pts: List<Offset>, progress: Float, fillColor: Color, alpha: Float, offsetX: Float, rot: Float) {
        if (progress <= 0f) return
        val count = (pts.size * progress).toInt().coerceIn(1, pts.size)
        val currentPts = pts.take(count)
        
        withTransform({
            translate(center.x + offsetX * s, center.y)
            rotate(rot, pivot = Offset(0f, 0f))
        }) {
            // Relleno
            if (fillT > 0f) {
                val fillPath = Path().apply {
                    moveTo(pts[0].x * s, pts[0].y * s)
                    for (p in pts) lineTo(p.x * s, p.y * s)
                    close()
                }
                drawPath(fillPath, color = fillColor.copy(alpha = alpha * fillT))
            }
            
            // Contorno progresivo
            val strokePath = Path().apply {
                if (currentPts.isNotEmpty()) {
                    moveTo(currentPts[0].x * s, currentPts[0].y * s)
                    for (i in 1 until currentPts.size) {
                        lineTo(currentPts[i].x * s, currentPts[i].y * s)
                    }
                }
            }
            drawPath(
                path = strokePath,
                color = palette.edge,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    // 1. Pétalo central (fondo)
    drawPetal(outline, p1T, palette.fillShadow, 0.7f, 0f, 0f)
    // 2. Pétalo izquierdo (frente)
    drawPetal(outline, p2T, palette.fillShadow, 1f, -3f, -12f)
    // 3. Pétalo derecho (frente)
    drawPetal(outline, p3T, palette.fill, 1f, 3f, 12f)
}

/** Tallo recto y hojas largas lanceoladas */
private fun DrawScope.drawTulipanStemAndLeaves(center: Offset, scale: Float, progress: Float, edgeColor: Color) {
    if (progress <= 0f) return
    val s = scale
    val stemLength = 48f * s * progress 
    val strokeW = max(2.8f, s * 0.14f)

    drawLine(
        color = edgeColor,
        start = center,
        end = Offset(center.x, center.y + stemLength),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )

    if (progress > 0.3f) {
        val leafT = ((progress - 0.3f) / 0.7f).coerceIn(0f, 1f)
        
        val rightLeaf = Path().apply {
            moveTo(center.x, center.y + stemLength * 0.7f)
            cubicTo(
                center.x + 24f * s * leafT, center.y + stemLength * 0.5f,
                center.x + 20f * s * leafT, center.y + stemLength * 0.1f,
                center.x + 8f * s * leafT, center.y - 12f * s * leafT
            )
        }
        drawPath(rightLeaf, color = edgeColor.copy(alpha = leafT), style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))

        val leftLeaf = Path().apply {
            moveTo(center.x, center.y + stemLength * 0.85f)
            cubicTo(
                center.x - 28f * s * leafT, center.y + stemLength * 0.6f,
                center.x - 22f * s * leafT, center.y + stemLength * 0.2f,
                center.x - 10f * s * leafT, center.y - 4f * s * leafT
            )
        }
        drawPath(leftLeaf, color = edgeColor.copy(alpha = leafT), style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawTulipanAtProgress(
    center: Offset,
    scale: Float,
    beatScale: Float,
    drawT: Float,
    fillT: Float,
    stemT: Float,
    palette: TulipanPalette,
    showStem: Boolean = true
) {
    if (showStem) {
        drawTulipanStemAndLeaves(center, scale * beatScale, stemT, palette.edge)
    }
    drawTulipanShape(center, scale, beatScale, drawT, fillT, palette)
}

// ── Pantalla de Tulipán Interactivo ────────────────────────────────────────────
@Composable
fun TulipanScreen(onBack: () -> Unit) {

    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }
    var paletteIndex by remember { mutableIntStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }

    val drawProgress = remember { Animatable(0f) }
    val fillAlpha = remember { Animatable(0f) }
    val stemProgress = remember { Animatable(0f) }
    val entryScale = remember { Animatable(0.8f) }

    val pulso = rememberInfiniteTransition(label = "pulso_tulipan")
    val beatScale by pulso.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                1f at 0 using EaseInOut
                1.04f at 300 using EaseInOut
                1f at 600 using EaseInOut
                1f at 1400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "beat_tulipan"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "press_scale"
    )

    // Motor de animación principal
    LaunchedEffect(repetir) {
        etapa = 0
        drawProgress.snapTo(0f)
        fillAlpha.snapTo(0f)
        stemProgress.snapTo(0f)
        entryScale.snapTo(0.8f)

        delay(400L)

        // Animación de entrada simultánea
        launch {
            entryScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        etapa = 1
        stemProgress.animateTo(1f, tween(900, easing = EaseOutCubic))
        delay(150L)
        etapa = 2
        drawProgress.animateTo(1f, tween(2000, easing = LinearEasing))
        delay(200L)
        etapa = 3
        fillAlpha.animateTo(1f, tween(700, easing = EaseOutCubic))
        delay(300L)
        etapa = 4
    }

    val palette = TulipanPaletteCycle[paletteIndex]
    val bgColor = BgDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (etapa >= 4) {
                    paletteIndex = (paletteIndex + 1) % TulipanPaletteCycle.size
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        if (etapa >= 4) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val haloAlpha = (beatScale - 1f) * 3f
                drawCircle(
                    color = palette.fill.copy(alpha = 0.06f + haloAlpha * 0.05f),
                    radius = size.width * 0.3f,
                    center = Offset(size.width / 2f, size.height * 0.38f - 22f * (size.width / 100f))
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pressScale * entryScale.value
                    scaleY = pressScale * entryScale.value
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.38f
            val scale = size.width / 100f
            val beat = if (etapa >= 4) beatScale else 1f

            drawTulipanAtProgress(
                center = Offset(cx, cy),
                scale = scale,
                beatScale = beat,
                drawT = drawProgress.value,
                fillT = fillAlpha.value,
                stemT = stemProgress.value,
                palette = palette
            )
        }

        if (etapa >= 4) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 24.dp)
                    .size(48.dp)
                    .background(color = palette.fill, shape = CircleShape)
                    .border(2.dp, Color(0xFFDDDDDD), CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { paletteIndex = (paletteIndex + 1) % TulipanPaletteCycle.size },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌷", fontSize = 20.sp)
            }
        }

        val context = LocalContext.current
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = etapa >= 4,
                message = "🌷 Lindo Tulipán 🌷",
                subMessage = "Color: ${palette.label}",
                repeatEmoji = "🌷",
                accentColor = palette.fill,
                onRepeat = { repetir++ },
                onBack = onBack,
                onSave = { includeText ->
                    saveTulipanAsImage(
                        context = context,
                        message = "🌷 Lindo Tulipán 🌷",
                        subMessage = "Color: ${palette.label}",
                        bgColor = bgColor,
                        palette = palette,
                        includeText = includeText
                    )
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color(0xFFEAEAEA))
        }
    }
}

fun saveTulipanAsImage(
    context: android.content.Context,
    message: String,
    subMessage: String,
    bgColor: Color,
    palette: TulipanPalette,
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
            density = Density(context),
            layoutDirection = LayoutDirection.Ltr,
            canvas = androidx.compose.ui.graphics.Canvas(canvas),
            size = size
        ) {
            drawRect(color = bgColor, size = size)
            val cx = artSize / 2f
            val cy = artSize * 0.38f
            val scale = artSize / 100f

            drawTulipanAtProgress(
                center = Offset(cx, cy),
                scale = scale,
                beatScale = 1f,
                drawT = 1f,
                fillT = 1f,
                stemT = 1f,
                palette = palette
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

@Preview(showBackground = true, widthDp = 400, heightDp = 800, backgroundColor = 0xFF121212)
@Composable
fun TulipanScreenPreview() {
    TulipanScreen(onBack = {})
}
