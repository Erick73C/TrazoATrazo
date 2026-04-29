package com.tuapp.drawbloom.drawings.flowers

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*

// ── Paleta ────────────────────────────────────────────────────────────────────
private val BgBlue       = Color(0xFF87CEEB)
private val KhakiLight   = Color(0xFFF0E68C)
private val KhakiDark    = Color(0xFFBDB76B)
private val GreenDark    = Color(0xFF006400)
private val GreenLime    = Color(0xFF32CD32)
private val GreenLawn    = Color(0xFF7CFC00)
private val GreenMid     = Color(0xFF228B22)
private val PinkLight    = Color(0xFFFFC0CB)
private val PinkDark     = Color(0xFFE75480)
private val YellowPetal  = Color(0xFFFFE000)
private val YellowShine  = Color(0xFFFFF9A0)
private val YellowShadow = Color(0xFFD4A017)
private val RedCenter    = Color(0xFF8B1A1A)
private val RedShine     = Color(0xFFB22222)
private val BlackLine    = Color(0xFF111111)

// ── Cono con animación de escala ──────────────────────────────────────────────
private fun DrawScope.drawCono(cx: Float, cy: Float, s: Float, progress: Float) {
    if (progress <= 0f) return
    val p    = progress.coerceIn(0f, 1f)
    val topW = 110f * s * p
    val botW = 22f  * s * p
    val h    = 145f * s * p

    val path = Path().apply {
        moveTo(cx - topW, cy)
        quadraticBezierTo(cx, cy - 12 * s * p, cx + topW, cy)
        quadraticBezierTo(cx + topW * 0.55f, cy + h * 0.75f, cx + botW, cy + h)
        quadraticBezierTo(cx, cy + h + 12 * s * p, cx - botW, cy + h)
        quadraticBezierTo(cx - topW * 0.55f, cy + h * 0.75f, cx - topW, cy)
        close()
    }
    val shadowPath = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx + topW * 0.55f, cy + h * 0.75f, cx + botW, cy + h)
        quadraticBezierTo(cx, cy + h + 12 * s * p, cx, cy + h)
        lineTo(cx, cy)
        close()
    }

    drawPath(path, color = KhakiLight.copy(alpha = p))
    drawPath(shadowPath, color = KhakiDark.copy(alpha = 0.28f * p))
    drawPath(path, color = KhakiDark.copy(alpha = p), style = Stroke(width = 3f * s))
    if (p > 0.5f) {
        drawLine(
            KhakiDark.copy(alpha = 0.5f * p),
            Offset(cx, cy + 5 * s * p),
            Offset(cx, cy + h - 10 * s * p),
            1.2f * s
        )
    }
}

// ── Hoja mejorada ─────────────────────────────────────────────────────────────
private fun DrawScope.drawHoja(
    rootX: Float, rootY: Float,
    angleDeg: Float, len: Float, wid: Float,
    s: Float, fill: Color, progress: Float = 1f
) {
    val p    = progress.coerceIn(0f, 1f)
    val rad  = Math.toRadians(angleDeg.toDouble()).toFloat()
    val tipX = rootX + len * s * p * cos(rad)
    val tipY = rootY - len * s * p * sin(rad)
    val px   = -sin(rad)
    val py   =  cos(rad)
    val hw   = wid * s * p

    val path = Path().apply {
        moveTo(rootX, rootY)
        quadraticBezierTo(
            rootX + len * s * p * 0.45f * cos(rad) + px * hw,
            rootY - len * s * p * 0.45f * sin(rad) - py * hw,
            tipX, tipY
        )
        quadraticBezierTo(
            rootX + len * s * p * 0.45f * cos(rad) - px * hw,
            rootY - len * s * p * 0.45f * sin(rad) + py * hw,
            rootX, rootY
        )
        close()
    }
    drawPath(path, color = fill.copy(alpha = p))
    drawLine(
        GreenDark.copy(alpha = 0.55f * p),
        Offset(rootX, rootY),
        Offset(tipX * 0.90f + rootX * 0.10f, tipY * 0.90f + rootY * 0.10f),
        1.3f * s
    )
    drawPath(path, color = GreenDark.copy(alpha = p), style = Stroke(2.2f * s))
}

// ── Flor (pétalos uno a uno) ──────────────────────────────────────────────────
private fun DrawScope.drawFlor(cx: Float, cy: Float, s: Float, progress: Float) {
    if (progress <= 0f) return
    val nPetals = 8
    val pLen    = 38f * s
    val pWid    = 16f * s
    val centerR = 20f * s

    val petalsToShow = (progress * nPetals).toInt().coerceIn(0, nPetals)
    val lastProg     = ((progress * nPetals) - petalsToShow).coerceIn(0f, 1f)

    for (i in 0 until nPetals) {
        val rad   = Math.toRadians(360.0 / nPetals * i).toFloat()
        val pLenI = when {
            i < petalsToShow  -> pLen
            i == petalsToShow -> pLen * lastProg
            else              -> 0f
        }
        if (pLenI <= 0f) continue
        val pWidI = pWid * (pLenI / pLen)
        val px    = cx + pLenI * cos(rad)
        val py    = cy + pLenI * sin(rad)
        val perpX = -sin(rad)
        val perpY =  cos(rad)

        val petal = Path().apply {
            moveTo(cx, cy)
            cubicTo(
                cx + pLenI * 0.3f * cos(rad) + perpX * pWidI,
                cy + pLenI * 0.3f * sin(rad) + perpY * pWidI,
                cx + pLenI * 0.7f * cos(rad) + perpX * pWidI * 0.9f,
                cy + pLenI * 0.7f * sin(rad) + perpY * pWidI * 0.9f,
                px, py
            )
            cubicTo(
                cx + pLenI * 0.7f * cos(rad) - perpX * pWidI * 0.9f,
                cy + pLenI * 0.7f * sin(rad) - perpY * pWidI * 0.9f,
                cx + pLenI * 0.3f * cos(rad) - perpX * pWidI,
                cy + pLenI * 0.3f * sin(rad) - perpY * pWidI,
                cx, cy
            )
            close()
        }
        drawPath(petal, color = YellowPetal)
        drawPath(petal, color = YellowShine.copy(alpha = 0.4f))
        drawPath(petal, color = BlackLine, style = Stroke(2f * s))
        drawLine(
            YellowShadow.copy(alpha = 0.55f),
            Offset(cx, cy),
            Offset(cx + pLenI * 0.78f * cos(rad), cy + pLenI * 0.78f * sin(rad)),
            0.9f * s
        )
    }

    if (progress > 0.5f) {
        val a = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
        drawCircle(Color.Black.copy(alpha = 0.25f * a), centerR + 2f, Offset(cx + 1f, cy + 1f))
        drawCircle(RedCenter.copy(alpha = a), centerR, Offset(cx, cy))
        drawCircle(RedShine.copy(alpha = 0.5f * a), centerR * 0.5f,
            Offset(cx - centerR * 0.2f, cy - centerR * 0.2f))
        drawCircle(BlackLine.copy(alpha = a), centerR, Offset(cx, cy), style = Stroke(2f * s))
    }
}

// ── Moño con animación de expansión ──────────────────────────────────────────
private fun DrawScope.drawMonyo(cx: Float, cy: Float, s: Float, progress: Float) {
    if (progress <= 0f) return
    val p     = progress.coerceIn(0f, 1f)
    val wingW = 58f * s * p
    val wingH = 26f * s * p

    val left = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx - wingW * 0.5f, cy - wingH * 1.2f,
            cx - wingW * 1.0f, cy - wingH * 0.8f,
            cx - wingW * 1.1f, cy + 4 * s * p
        )
        cubicTo(
            cx - wingW * 0.9f, cy + wingH * 1.1f,
            cx - wingW * 0.4f, cy + wingH * 0.6f,
            cx, cy
        )
        close()
    }
    val right = Path().apply {
        moveTo(cx, cy)
        cubicTo(
            cx + wingW * 0.5f, cy - wingH * 1.2f,
            cx + wingW * 1.0f, cy - wingH * 0.8f,
            cx + wingW * 1.1f, cy + 4 * s * p
        )
        cubicTo(
            cx + wingW * 0.9f, cy + wingH * 1.1f,
            cx + wingW * 0.4f, cy + wingH * 0.6f,
            cx, cy
        )
        close()
    }

    drawPath(left,  color = PinkDark.copy(alpha = 0.18f * p), style = Stroke(6f * s))
    drawPath(right, color = PinkDark.copy(alpha = 0.18f * p), style = Stroke(6f * s))
    drawPath(left,  color = PinkLight.copy(alpha = p))
    drawPath(left,  color = PinkDark.copy(alpha = p), style = Stroke(2.5f * s))
    drawPath(right, color = PinkLight.copy(alpha = p))
    drawPath(right, color = PinkDark.copy(alpha = p), style = Stroke(2.5f * s))

    val nudoR = 12f * s * p
    drawCircle(PinkLight.copy(alpha = p), nudoR, Offset(cx, cy))
    drawCircle(PinkDark.copy(alpha = 0.35f * p), nudoR * 0.5f,
        Offset(cx - 3f * s * p, cy - 3f * s * p))
    drawCircle(PinkDark.copy(alpha = p), nudoR, Offset(cx, cy), style = Stroke(2f * s))
}

// ── Pantalla ──────────────────────────────────────────────────────────────────
@Composable
fun FlowerScreen(onBack: () -> Unit) {
    var etapa   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    // Animaciones individuales
    val conoAnim  = remember { Animatable(0f) }
    val hojaAnims = remember { List(5) { Animatable(0f) } }  // 5 hojas
    val florAnims = remember { List(6) { Animatable(0f) } }  // 6 flores
    val monyoAnim = remember { Animatable(0f) }

    LaunchedEffect(repetir) {
        // Reset todo
        etapa = 0
        conoAnim.snapTo(0f)
        hojaAnims.forEach { it.snapTo(0f) }
        florAnims.forEach { it.snapTo(0f) }
        monyoAnim.snapTo(0f)

        delay(300L)

        // 1. Cono crece desde el centro
        conoAnim.animateTo(1f, tween(600, easing = EaseOutBack))
        etapa = 1

        delay(150L)

        // 2. Hojas aparecen en abanico desde el tallo
        etapa = 2
        hojaAnims.forEachIndexed { i, anim ->
            anim.animateTo(1f, tween(350, easing = EaseOutCubic))
            delay(60L)
        }

        delay(100L)

        // 3. Flores una a una
        etapa = 3
        florAnims.forEach { anim ->
            anim.animateTo(1f, tween(480, easing = EaseOutCubic))
            delay(70L)
        }

        delay(200L)

        // 4. Moño se expande desde el centro
        etapa = 4
        monyoAnim.animateTo(1f, tween(550, easing = EaseOutBack))

        delay(300L)
        etapa = 5
    }

    // Pulso sutil del fondo al terminar
    val pulso = rememberInfiniteTransition(label = "pulso")
    val haloA by pulso.animateFloat(
        0f, 0.07f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "halo"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlue)
    ) {
        if (etapa >= 5) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color.White.copy(alpha = haloA), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.38f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width / 2f, size.height * 0.38f)
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w       = size.width
            val h       = size.height
            val s       = w / 390f          // ligeramente más grande
            val conoCx  = w * 0.52f
            val conoTop = h * 0.44f
            val conoH   = 145f * s
            val conoBot = conoTop + conoH

            // ── 1. Cono ───────────────────────────────────────────────────
            drawCono(conoCx, conoTop, s, conoAnim.value)

            // ── 2. Hojas ──────────────────────────────────────────────────
            // Decisión creativa: hojas salen del TALLO (parte baja del cono)
            // en forma de abanico a la izquierda, dando sensación de ramo real
            if (etapa >= 2) {
                val tX = conoCx - 30f * s    // punto del tallo izquierdo
                val tY = conoBot - 20f * s   // altura del tallo

                // Hoja grande trasera — apunta arriba-izquierda
                drawHoja(tX, tY, 145f, 105f, 42f, s, GreenLime,  hojaAnims[0].value)
                // Hoja mediana trasera — más horizontal
                drawHoja(tX - 8f*s, tY + 10f*s, 120f, 90f, 36f, s, GreenLime,  hojaAnims[1].value)
                // Hoja delantera grande — ángulo medio
                drawHoja(tX + 5f*s, tY - 5f*s,  135f, 95f, 38f, s, GreenLawn,  hojaAnims[2].value)
                // Hoja delantera pequeña — más arriba
                drawHoja(tX - 5f*s, tY + 20f*s, 105f, 78f, 30f, s, GreenLawn,  hojaAnims[3].value)
                // Hoja extra — acento oscuro para profundidad
                drawHoja(tX + 12f*s, tY + 8f*s, 155f, 70f, 28f, s, GreenMid,   hojaAnims[4].value)
            }

            // ── 3. Flores (6 en disposición natural de ramo) ──────────────
            if (etapa >= 3) {
                val fCx = conoCx + 8f * s
                val fCy = conoTop - 8f * s

                // Disposición: triángulo — 3 abajo, 2 medias, 1 arriba
                val florPos = listOf(
                    Offset(fCx - 72f * s, fCy + 10f * s),   // inf izq
                    Offset(fCx + 72f * s, fCy + 10f * s),   // inf der
                    Offset(fCx,           fCy + 0f  * s),   // inf centro
                    Offset(fCx - 38f * s, fCy - 45f * s),   // med izq
                    Offset(fCx + 38f * s, fCy - 45f * s),   // med der
                    Offset(fCx,           fCy - 82f * s),   // sup centro
                )
                florPos.forEachIndexed { i, pos ->
                    drawFlor(pos.x, pos.y, s, florAnims[i].value)
                }
            }

            // ── 4. Moño ───────────────────────────────────────────────────
            drawMonyo(conoCx, conoBot - 28f * s, s, monyoAnim.value)
        }

        // ── Botones ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible     = etapa >= 5,
                message     = "💐 Ten un bonito día 💐",
                subMessage  = "¡Que todo te salga bien!",
                repeatEmoji = "🌸",
                accentColor = PinkDark,
                onRepeat    = { repetir++ },
                onBack      = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color.White)
        }
    }
}