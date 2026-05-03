package com.example.trazoatrazo.drawings.Animals

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
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*

// ── Paleta de Colores ─────────────────────────────────────────────────────────
private val BgWater      = Color(0x00000000)
private val ShellBrown   = Color(0xFF8B4513) // Café
private val ShellLight   = Color(0xFFA0522D)
private val ShellDark    = Color(0xFF5D2E0A)
private val SkinGreen    = Color(0xFF90EE90)
private val SkinDark     = Color(0xFF2E7D32)
private val EyeBlack     = Color(0xFF111111)

// ── Dibujar Cabeza ────────────────────────────────────────────────────────────
private fun DrawScope.drawTurtleHead(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val headR = 26f * scale * p
    val offsetHeadY = -100f * scale * p
    
    // Cuello (sale del caparazón hacia arriba)
    val neckWidth = 22f * scale * p
    val neckHeight = 45f * scale * p
    drawRect(
        color = SkinGreen,
        topLeft = Offset(cx - neckWidth / 2, cy - 75f * scale * p),
        size = Size(neckWidth, neckHeight)
    )
    
    // Cabeza
    drawCircle(
        color = SkinGreen,
        radius = headR,
        center = Offset(cx, cy + offsetHeadY)
    )
    // Borde cabeza
    drawCircle(
        color = SkinDark,
        radius = headR,
        center = Offset(cx, cy + offsetHeadY),
        style = Stroke(width = 2.5f * scale)
    )
    
    // Ojos
    if (p > 0.8f) {
        val eyeProg = ((p - 0.8f) * 5f).coerceIn(0f, 1f)
        // Ojo Izquierdo
        drawCircle(Color.White.copy(alpha = eyeProg), 5f * scale, Offset(cx - 11f * scale, cy + offsetHeadY - 4f * scale))
        drawCircle(EyeBlack.copy(alpha = eyeProg), 2.5f * scale, Offset(cx - 11f * scale, cy + offsetHeadY - 4f * scale))
        // Ojo Derecho
        drawCircle(Color.White.copy(alpha = eyeProg), 5f * scale, Offset(cx + 11f * scale, cy + offsetHeadY - 4f * scale))
        drawCircle(EyeBlack.copy(alpha = eyeProg), 2.5f * scale, Offset(cx + 11f * scale, cy + offsetHeadY - 4f * scale))
    }
}

// ── Dibujar Patas y Cola ──────────────────────────────────────────────────────
private fun DrawScope.drawTurtleLimbs(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val legW = 38f * scale * p
    val legH = 28f * scale * p
    
    // Patas traseras (posicionadas abajo)
    withTransform({
        rotate(degrees = 35f, pivot = Offset(cx + 55f * scale * p, cy + 65f * scale * p))
    }) {
        drawOval(SkinGreen, Offset(cx + 40f * scale * p, cy + 55f * scale * p), Size(legW, legH))
        drawOval(SkinDark, Offset(cx + 40f * scale * p, cy + 55f * scale * p), Size(legW, legH), style = Stroke(2f * scale))
    }
    withTransform({
        rotate(degrees = -35f, pivot = Offset(cx - 55f * scale * p, cy + 65f * scale * p))
    }) {
        drawOval(SkinGreen, Offset(cx - 78f * scale * p, cy + 55f * scale * p), Size(legW, legH))
        drawOval(SkinDark, Offset(cx - 78f * scale * p, cy + 55f * scale * p), Size(legW, legH), style = Stroke(2f * scale))
    }
    
    // Patas delanteras (posicionadas arriba)
    withTransform({
        rotate(degrees = -40f, pivot = Offset(cx + 60f * scale * p, cy - 65f * scale * p))
    }) {
        drawOval(SkinGreen, Offset(cx + 45f * scale * p, cy - 75f * scale * p), Size(legW, legH))
        drawOval(SkinDark, Offset(cx + 45f * scale * p, cy - 75f * scale * p), Size(legW, legH), style = Stroke(2f * scale))
    }
    withTransform({
        rotate(degrees = 40f, pivot = Offset(cx - 60f * scale * p, cy - 65f * scale * p))
    }) {
        drawOval(SkinGreen, Offset(cx - 83f * scale * p, cy - 75f * scale * p), Size(legW, legH))
        drawOval(SkinDark, Offset(cx - 83f * scale * p, cy - 75f * scale * p), Size(legW, legH), style = Stroke(2f * scale))
    }
    
    // Cola (atrás)
    val tailPath = Path().apply {
        moveTo(cx, cy + 80f * scale * p)
        lineTo(cx - 12f * scale * p, cy + 105f * scale * p)
        lineTo(cx + 12f * scale * p, cy + 105f * scale * p)
        close()
    }
    drawPath(tailPath, color = SkinGreen)
    drawPath(tailPath, color = SkinDark, style = Stroke(width = 2f * scale))
}

// ── Dibujar Caparazón ─────────────────────────────────────────────────────────
private fun DrawScope.drawTurtleShell(cx: Float, cy: Float, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val p = progress
    val shellRadiusX = 80f * scale * p
    val shellRadiusY = 95f * scale * p
    
    // Sombra base
    drawOval(
        color = Color.Black.copy(alpha = 0.15f * p),
        topLeft = Offset(cx - shellRadiusX + 5f, cy - shellRadiusY + 5f),
        size = Size(shellRadiusX * 2, shellRadiusY * 2)
    )

    // Cuerpo principal del caparazón (Café)
    drawOval(
        color = ShellBrown,
        topLeft = Offset(cx - shellRadiusX, cy - shellRadiusY),
        size = Size(shellRadiusX * 2, shellRadiusY * 2)
    )
    
    // Patrón decorativo del caparazón
    if (p > 0.4f) {
        val patternProg = ((p - 0.4f) * 1.6f).coerceIn(0f, 1f)
        val strokeW = 3.5f * scale
        
        // Hexágono/Círculo central
        drawCircle(
            color = ShellLight.copy(alpha = patternProg),
            radius = 38f * scale * patternProg,
            center = Offset(cx, cy),
            style = Stroke(width = strokeW)
        )
        
        // Líneas radiales que dividen las placas
        for (i in 0 until 6) {
            val angle = (i * 60f - 90f) * (PI.toFloat() / 180f)
            val start = Offset(cx + 38f * scale * cos(angle), cy + 38f * scale * sin(angle))
            val end = Offset(cx + shellRadiusX * cos(angle), cy + shellRadiusY * sin(angle))
            drawLine(
                color = ShellLight.copy(alpha = patternProg),
                start = start,
                end = end,
                strokeWidth = strokeW
            )
        }
    }
    
    // Borde exterior fuerte
    drawOval(
        color = ShellDark,
        topLeft = Offset(cx - shellRadiusX, cy - shellRadiusY),
        size = Size(shellRadiusX * 2, shellRadiusY * 2),
        style = Stroke(width = 5.5f * scale)
    )
}

@Composable
fun TurtleScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val shellAnim = remember { Animatable(0f) }
    val limbsAnim = remember { Animatable(0f) }
    val headAnim  = remember { Animatable(0f) }

    LaunchedEffect(repetir) {
        etapa = 0
        shellAnim.snapTo(0f)
        limbsAnim.snapTo(0f)
        headAnim.snapTo(0f)

        delay(500L)
        
        // 1. Caparazón aparece primero (es la base)
        etapa = 1
        shellAnim.animateTo(1f, tween(1100, easing = EaseOutBack))
        
        // 2. Las patas y la cola salen de debajo
        etapa = 2
        limbsAnim.animateTo(1f, tween(900, easing = EaseOutCubic))
        
        // 3. La cabeza se asoma al final
        etapa = 3
        headAnim.animateTo(1f, tween(850, easing = EaseOutBack))
        
        delay(300L)
        etapa = 4 // Todo listo
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWater)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.45f
            val scale = size.width / 420f

            // Orden de dibujo para capas:
            // 1. Miembros (debajo del caparazón)
            if (etapa >= 2) {
                drawTurtleLimbs(cx, cy, scale, limbsAnim.value)
            }
            
            // 2. Cabeza (también se asoma desde abajo)
            if (etapa >= 3) {
                drawTurtleHead(cx, cy, scale, headAnim.value)
            }

            // 3. Caparazón (arriba de todo)
            if (etapa >= 1) {
                drawTurtleShell(cx, cy, scale, shellAnim.value)
            }
        }

        // Interfaz de usuario
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = etapa >= 4,
                message = "🐢 ¡Una linda tortuga! 🐢",
                subMessage = "Lenta pero segura, ¡va hacia arriba!",
                repeatEmoji = "🐢",
                accentColor = SkinDark,
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = SkinDark)
        }
    }
}
