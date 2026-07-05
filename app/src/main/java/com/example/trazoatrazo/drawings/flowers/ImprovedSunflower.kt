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
fun ImprovedSunflowerScreen(onBack: () -> Unit) {
    val phi         = 137.508 * (Math.PI / 180.0)
    val totalPuntos = 250
    val semillas    = 120

    var count   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    // Animaciones del tallo y hojas separadas del girasol
    val talloAnim  = remember { Animatable(0f) }  // 0=abajo, 1=arriba (tallo crece)
    val hoja1Anim  = remember { Animatable(0f) }  // hoja izquierda crece
    val hoja2Anim  = remember { Animatable(0f) }  // hoja derecha crece

    val pulso = rememberInfiniteTransition(label = "pulso")
    val brillo by pulso.animateFloat(
        initialValue  = 0.85f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "brillo"
    )

    LaunchedEffect(repetir) {

        count = 0
        talloAnim.snapTo(0f)
        hoja1Anim.snapTo(0f)
        hoja2Anim.snapTo(0f)

        // 1. Tallo sube desde el fondo de la pantalla
        talloAnim.animateTo(1f, tween(700, easing = EaseOutCubic))

        // 2. Hoja izquierda crece del tallo
        hoja1Anim.animateTo(1f, tween(450, easing = EaseOutBack))
        delay(80L)

        // 3. Hoja derecha crece del tallo
        hoja2Anim.animateTo(1f, tween(450, easing = EaseOutBack))
        delay(150L)

        // 4. Empieza el girasol
        while (count < totalPuntos) {
            delay(18L)
            count++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // ── Halo de fondo ─────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (count >= totalPuntos) {
                val cx = size.width  / 2f
                val cy = size.height * 0.30f   // más arriba
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = 0.09f * brillo),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.width * 0.58f
                    ),
                    radius = size.width * 0.58f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Canvas principal ──────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx     = size.width  / 2f
            val cy     = size.height * 0.30f   // girasol más arriba
            val escala = minOf(size.width, size.height) / 160f

            drawImprovedSunflower(
                cx, cy, escala, count,
                talloAnim.value, hoja1Anim.value, hoja2Anim.value
            )
        }

        // ── Botones ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val context = LocalContext.current
            val message = "🌻 Una floresita para ti 🌻"
            val subMessage = "Que el dia de hoy te valla muy bien :) "
            val bgColor = Color(0xFF0D0D0D)

            DrawingButtons(
                visible     = count >= 250, // totalPuntos
                message     = message,
                subMessage  = subMessage,
                repeatEmoji = "🌻",
                accentColor = Color(0xFFB8860B),
                onRepeat    = { repetir++ },
                onBack      = onBack,
                onSave = { includeText ->
                    saveImprovedSunflowerAsImage(
                        context,
                        message,
                        subMessage,
                        bgColor,
                        includeText
                    )
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack)
        }
    }
}

fun saveImprovedSunflowerAsImage(
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
            
            val cx     = artSize / 2f
            val cy     = artSize * 0.30f
            val escala = artSize / 160f 
            
            drawImprovedSunflower(cx, cy, escala, 250, 1f, 1f, 1f)
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

private fun DrawScope.drawImprovedSunflower(
    cx: Float, cy: Float, escala: Float, count: Int,
    talloP: Float, hoja1P: Float, hoja2P: Float
) {
    val phi      = 137.508 * (Math.PI / 180.0)
    val semillas = 120

    // ── Tallo y hojas ────────────────────────────────────
    drawTalloYHojas(cx, cy, escala, talloP, hoja1P, hoja2P)

    // ── Espiral de semillas y pétalos ─────────────────────────────
    for (i in 0 until count) {
        val r     = 4.0 * sqrt(i.toDouble()) * escala
        val theta = i * phi
        val x     = cx + (r * cos(theta)).toFloat()
        val y     = cy - (r * sin(theta)).toFloat()

        if (i < semillas) {
            val isOuter  = i > semillas - 30
            val isMid    = i > semillas - 65 && !isOuter
            val radio    = (3.5f + i * 0.014f) * (escala / 3.5f)

            val colorBase = when {
                isOuter -> Color(0xFF6B7C1A)
                isMid   -> Color(0xFF5A3018)
                else    -> Color(0xFF2E1205)
            }
            val colorBrillo = when {
                isOuter -> Color(0xFF9CB82A)
                isMid   -> Color(0xFF7A4A28)
                else    -> Color(0xFF4A2010)
            }
            val colorPunto = when {
                isOuter -> Color(0xFFBDD430).copy(alpha = 0.5f)
                isMid   -> Color(0xFF3D1A08).copy(alpha = 0.6f)
                else    -> Color(0xFF1A0804).copy(alpha = 0.7f)
            }

            drawCircle(Color.Black.copy(alpha = 0.45f), radio + 1.8f, Offset(x + 0.6f, y + 0.6f))
            drawCircle(colorBase, radio, Offset(x, y))
            drawCircle(colorBrillo.copy(alpha = 0.55f), radio * 0.42f, Offset(x - radio * 0.22f, y - radio * 0.22f))
            drawCircle(colorPunto, radio * 0.22f, Offset(x + radio * 0.15f, y + radio * 0.15f))

            if (isOuter) {
                drawCircle(Color(0xFFE8D000).copy(alpha = 0.45f), radio * 0.18f, Offset(x - radio * 0.1f, y + radio * 0.2f))
            }
        } else {
            val angle = Math.toRadians(i * 137.508)
            val cosA  = cos(angle).toFloat()
            val sinA  = sin(angle).toFloat()
            val largo = (50f + (i - semillas) * 0.18f) * (escala / 3.5f)
            val ancho = largo * 0.38f
            val perpX = -sinA
            val perpY = -cosA

            val path = Path().apply {
                moveTo(x, y)
                cubicTo(x + perpX * ancho * 1.1f, y + perpY * ancho * 1.1f, x + cosA * largo * 0.7f + perpX * ancho, y - sinA * largo * 0.7f + perpY * ancho, x + cosA * largo, y - sinA * largo)
                cubicTo(x + cosA * largo * 0.7f - perpX * ancho, y - sinA * largo * 0.7f - perpY * ancho, x - perpX * ancho * 1.1f, y - perpY * ancho * 1.1f, x, y)
                close()
            }
            drawPath(path, Color(0xFFB8860B).copy(alpha = 0.6f), style = Stroke(1.5f * (escala / 3.5f)))
            drawPath(path, Color(0xFFFFD700))
            drawPath(path, Color(0xFFFFF176).copy(alpha = 0.55f))
            drawLine(Color(0xFFFFB300).copy(alpha = 0.7f), Offset(x, y), Offset(x + cosA * largo * 0.85f, y - sinA * largo * 0.85f), 0.8f * (escala / 3.5f))
        }
    }
}

// ── Tallo + Hojas grandes estilo girasol real ─────────────────────────────────
private fun DrawScope.drawTalloYHojas(
    cx: Float, cy: Float, escala: Float,
    talloP: Float,   // 0→1: tallo crece desde abajo hacia arriba
    hoja1P: Float,   // 0→1: hoja izquierda crece
    hoja2P: Float    // 0→1: hoja derecha crece
) {
    val s = escala

    // El tallo sube desde size.height hasta cy+14*s
    // talloP=0 → solo se ve el punto de abajo, talloP=1 → tallo completo
    val talloTop    = cy + 45f * s
    val talloBot    = size.height
    val talloActual = talloBot - (talloBot - talloTop) * talloP   // empieza abajo y sube

    if (talloP > 0f) {
        // Sombra del tallo
        drawLine(
            Color(0xFF1A2A08).copy(alpha = 0.6f),
            Offset(cx + 2f, talloActual),
            Offset(cx + 2f, talloBot),
            strokeWidth = 6f * s
        )
        // Tallo principal
        drawLine(
            Color(0xFF2E5A10),
            Offset(cx, talloActual),
            Offset(cx, talloBot),
            strokeWidth = 6f * s
        )
        // Brillo lateral del tallo
        drawLine(
            Color(0xFF4A8A1C).copy(alpha = 0.55f),
            Offset(cx - 1.2f * s, talloActual),
            Offset(cx - 1.2f * s, talloBot),
            strokeWidth = 2.5f * s
        )
    }

    // ── Hoja izquierda — más abajo, más pequeña ───────────────────────────
    if (hoja1P > 0f) {
        val hLRootX = cx - 2f * s
        val hLRootY = cy + 118f * s    // misma altura que hoja derecha anterior

        drawHojaGrande(
            rootX   = hLRootX,
            rootY   = hLRootY,
            dirX    = -1f,
            lenMain = 58f * s * hoja1P,   // más pequeña (antes 80f)
            lenSide = 36f * s * hoja1P,   // más pequeña (antes 50f)
            angleUp = -0.55f,
            alpha   = hoja1P,
            s       = s
        )
    }

    // ── Hoja derecha — más abajo ──────────────────────────────────────────
    if (hoja2P > 0f) {
        val hRRootX = cx + 2f * s
        val hRRootY = cy + 138f * s    // un poco más abajo que la izquierda

        drawHojaGrande(
            rootX   = hRRootX,
            rootY   = hRRootY,
            dirX    = 1f,
            lenMain = 68f * s * hoja2P,
            lenSide = 42f * s * hoja2P,
            angleUp = -0.45f,
            alpha   = hoja2P,
            s       = s
        )
    }
}

// ── Hoja grande con forma de "naranja" / corazón abierto ─────────────────────
// Inspirada en la foto: hoja ancha, bordes ligeramente dentados, nervio grueso
private fun DrawScope.drawHojaGrande(
    rootX: Float, rootY: Float,
    dirX: Float,           // -1 = izquierda, +1 = derecha
    lenMain: Float,        // longitud hacia la punta
    lenSide: Float,        // ancho máximo lateral
    angleUp: Float,        // ángulo hacia arriba (negativo = sube)
    alpha: Float,
    s: Float
) {
    // Punta de la hoja
    val tipX = rootX + dirX * lenMain * cos(angleUp) * 0.85f
    val tipY = rootY + lenMain * sin(angleUp) * 1.1f   // sube bastante

    // Punto de máximo ancho (a mitad del camino)
    val midX  = rootX + dirX * lenMain * 0.48f * cos(angleUp)
    val midY  = rootY + lenMain * 0.48f * sin(angleUp)

    // Borde superior de la hoja (lado de arriba)
    val topBX = midX + dirX * lenSide * 0.15f
    val topBY = midY - lenSide * 0.95f   // sube mucho → hoja ancha hacia arriba

    // Borde inferior de la hoja (lado de abajo)
    val botBX = midX + dirX * lenSide * 0.55f
    val botBY = midY + lenSide * 0.35f

    // ── Path principal de la hoja ─────────────────────────────────────────
    val path = Path().apply {
        moveTo(rootX, rootY)

        // Lado superior: raíz → punta pasando por el borde de arriba
        cubicTo(
            rootX + dirX * lenMain * 0.18f, rootY - lenSide * 0.5f,
            topBX, topBY,
            tipX, tipY
        )
        // Lado inferior: punta → raíz pasando por el borde de abajo
        cubicTo(
            tipX - dirX * lenMain * 0.1f, tipY + lenSide * 0.3f,
            botBX, botBY,
            rootX, rootY
        )
        close()
    }

    // Sombra
    val shadowPath = Path().apply {
        moveTo(rootX + 2f, rootY + 2f)
        cubicTo(
            rootX + dirX * lenMain * 0.18f + 2f, rootY - lenSide * 0.5f + 2f,
            topBX + 2f, topBY + 2f,
            tipX + 2f, tipY + 2f
        )
        cubicTo(
            tipX - dirX * lenMain * 0.1f + 2f, tipY + lenSide * 0.3f + 2f,
            botBX + 2f, botBY + 2f,
            rootX + 2f, rootY + 2f
        )
        close()
    }
    drawPath(shadowPath, Color(0xFF0D1F05).copy(alpha = 0.4f * alpha))

    // Relleno base verde oscuro
    drawPath(path, Color(0xFF2D6010).copy(alpha = alpha))
    // Segunda capa más clara (brillo lateral)
    val brightPath = Path().apply {
        moveTo(rootX, rootY)
        cubicTo(
            rootX + dirX * lenMain * 0.18f, rootY - lenSide * 0.5f,
            topBX, topBY,
            tipX, tipY
        )
        lineTo(midX, midY)
        close()
    }
    drawPath(brightPath, Color(0xFF4A8A20).copy(alpha = 0.55f * alpha))

    // ── Nervio central grueso ─────────────────────────────────────────────
    drawLine(
        Color(0xFF1E4A0A).copy(alpha = 0.9f * alpha),
        Offset(rootX, rootY), Offset(tipX, tipY),
        strokeWidth = 2.2f * s
    )
    // Brillo del nervio
    drawLine(
        Color(0xFF5AAA22).copy(alpha = 0.4f * alpha),
        Offset(rootX, rootY), Offset(tipX * 0.9f + rootX * 0.1f, tipY * 0.9f + rootY * 0.1f),
        strokeWidth = 0.9f * s
    )

    // ── Nervios secundarios (4 pares) ─────────────────────────────────────
    val nerviosCount = 4
    for (n in 1..nerviosCount) {
        val t    = n.toFloat() / (nerviosCount + 1)
        val nRX  = rootX + (tipX - rootX) * t
        val nRY  = rootY + (tipY - rootY) * t
        val nLen = lenSide * 0.55f * (1f - t * 0.4f)
        val nAng = angleUp - 0.3f * (1f - t)  // ángulo se abre hacia arriba

        // Nervio hacia arriba
        drawLine(
            Color(0xFF1E4A0A).copy(alpha = 0.55f * alpha),
            Offset(nRX, nRY),
            Offset(nRX + dirX * nLen * 0.7f, nRY - nLen * 0.7f),
            strokeWidth = 1.1f * s
        )
        // Nervio hacia abajo
        drawLine(
            Color(0xFF1E4A0A).copy(alpha = 0.4f * alpha),
            Offset(nRX, nRY),
            Offset(nRX + dirX * nLen * 0.5f, nRY + nLen * 0.45f),
            strokeWidth = 0.9f * s
        )
    }

    // ── Borde de la hoja ──────────────────────────────────────────────────
    drawPath(path, Color(0xFF1A3A08).copy(alpha = alpha), style = Stroke(2f * s))

    // ── Puntitos dentados en el borde (textura tipo naranja) ──────────────
    val borderPoints = 10
    for (b in 0..borderPoints) {
        val t    = b.toFloat() / borderPoints
        // Interpola sobre el borde superior
        val bx   = lerp(rootX, tipX, t) + dirX * lenSide * 0.15f * sin(t * PI.toFloat())
        val by   = lerp(rootY, tipY, t) - lenSide * 0.85f * sin(t * PI.toFloat())
        val dLen = 4f * s
        drawLine(
            Color(0xFF1A3A08).copy(alpha = 0.5f * alpha),
            Offset(bx, by),
            Offset(bx + dirX * dLen * 0.4f, by - dLen),
            strokeWidth = 0.8f * s
        )
    }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t.coerceIn(0f, 1f)