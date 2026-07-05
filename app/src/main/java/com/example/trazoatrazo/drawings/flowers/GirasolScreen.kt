package com.example.trazoatrazo.drawings.flowers

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
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun GirasolScreen(onBack: () -> Unit) {
    val totalPuntos = 250

    // count sube de 0 a totalPuntos de a 1 por frame → aparición punto a punto
    var count   by remember { mutableIntStateOf(0) }
    // repetir cambia cada vez que el usuario presiona "Repetir" → relanza LaunchedEffect
    var repetir by remember { mutableIntStateOf(0) }

    // Animación de "pulso" continuo para dar vida al girasol completo
    val pulso = rememberInfiniteTransition(label = "pulso")
    val brillo by pulso.animateFloat(
        initialValue = 0.85f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "brillo"
    )

    // Cada vez que `repetir` cambia (o al entrar por primera vez), reinicia la animación
    LaunchedEffect(repetir) {
        count = 0
        while (count < totalPuntos) {
            delay(18L)
            count++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))  // negro suave, no puro
    ) {
        // Fondo decorativo: resplandor central
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width  / 2f
            val cy = size.height / 2f

            // Halo de fondo que pulsa cuando ya terminó
            if (count >= totalPuntos) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.08f * brillo),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Canvas del girasol
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx     = size.width  / 2f
            val cy     = size.height / 2f
            val escala = minOf(size.width, size.height) / 160f  // escala responsiva

            drawGirasol(cx, cy, escala, count)
        }

        // ── Botones reutilizables ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val context = LocalContext.current
            val message = "🌻 Una flor para ti :D🌻"
            val subMessage = "Que tengas un gran dia "

            DrawingButtons(
                visible      = count >= totalPuntos,
                message      = message,
                subMessage   = subMessage,
                repeatEmoji  = "🌻",
                accentColor  = Color(0xFFB8860B),
                onRepeat     = { repetir++ },
                onBack       = onBack,
                onSave = { includeText ->
                    saveGirasolAsImage(
                        context,
                        message,
                        subMessage,
                        Color(0xFF0D0D0D),
                        includeText
                    )
                }
            )
        }

        // Botón volver siempre visible arriba izquierda
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack)
        }
    }
}

private fun DrawScope.drawGirasol(cx: Float, cy: Float, escala: Float, count: Int) {
    val phi      = 137.508 * (PI / 180.0)
    val semillas = 120

    for (i in 0 until count) {
        val r     = 4.0 * sqrt(i.toDouble()) * escala
        val theta = i * phi
        val x     = cx + (r * cos(theta)).toFloat()
        val y     = cy - (r * sin(theta)).toFloat()

        if (i < semillas) {
            // ── Semilla: círculo café con borde oscuro ────────────
            val radio = (3.5f + i * 0.012f) * (escala / 3.5f)
            drawCircle(
                color  = Color(0xFF5C2E00),
                radius = radio + 1.2f,
                center = Offset(x, y)
            )
            drawCircle(
                color  = Color(0xFF8B4513),
                radius = radio,
                center = Offset(x, y)
            )
            // Brillo de semilla
            drawCircle(
                color  = Color(0xFFCD853F).copy(alpha = 0.5f),
                radius = radio * 0.45f,
                center = Offset(x - radio * 0.25f, y - radio * 0.25f)
            )
        } else {
            // ── Pétalo: forma de lágrima/elipse orientada ─────────
            val angle   = Math.toRadians(i * 137.508)
            val cosA    = cos(angle).toFloat()
            val sinA    = sin(angle).toFloat()

            // Tamaño del pétalo crece con i para el borde exterior
            val largo   = (50f + (i - semillas) * 0.18f) * (escala / 3.5f)
            val ancho   = largo * 0.38f

            // Vector perpendicular
            val perpX   = -sinA
            val perpY   = -cosA

            val path = Path().apply {
                // Base del pétalo (en el punto de la espiral)
                moveTo(x, y)
                // Lado izquierdo con curva
                cubicTo(
                    x + perpX * ancho * 1.1f,
                    y + perpY * ancho * 1.1f,
                    x + cosA  * largo * 0.7f + perpX * ancho,
                    y - sinA  * largo * 0.7f + perpY * ancho,
                    x + cosA  * largo,
                    y - sinA  * largo
                )
                // Lado derecho con curva
                cubicTo(
                    x + cosA  * largo * 0.7f - perpX * ancho,
                    y - sinA  * largo * 0.7f - perpY * ancho,
                    x - perpX * ancho * 1.1f,
                    y - perpY * ancho * 1.1f,
                    x, y
                )
                close()
            }

            // Sombra del pétalo
            drawPath(
                path  = path,
                color = Color(0xFFB8860B).copy(alpha = 0.6f),
                style = Stroke(width = 1.5f * (escala / 3.5f))
            )
            // Relleno con gradiente simulado (dos capas)
            drawPath(path, color = Color(0xFFFFD700))
            drawPath(path, color = Color(0xFFFFF176).copy(alpha = 0.55f))

            // Nervio central del pétalo
            drawLine(
                color       = Color(0xFFFFB300).copy(alpha = 0.7f),
                start       = Offset(x, y),
                end         = Offset(x + cosA * largo * 0.85f, y - sinA * largo * 0.85f),
                strokeWidth = 0.8f * (escala / 3.5f)
            )
        }
    }
}

fun saveGirasolAsImage(
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
            val cy = artSize / 2f
            val escala = artSize / 160f 
            
            drawGirasol(cx, cy, escala, 250) // totalPuntos
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
