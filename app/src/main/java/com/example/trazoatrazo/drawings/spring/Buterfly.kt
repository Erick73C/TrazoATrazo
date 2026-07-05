package com.example.trazoatrazo.drawings.spring

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
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
import kotlin.random.Random

// ── Paleta de Colores ─────────────────────────────────────────────────────────
private val WingOrange       = Color(0xFFBF360C)   // Naranja oscuro monarca
private val WingOrangeLight  = Color(0xFFFF6D00)   // Naranja vivo
private val WingBlack        = Color(0xFF0D0D0D)   // Negro ala
private val SpotWhite        = Color(0xFFFFFFFF)   // Manchas blancas
private val BodyDark         = Color(0xFF0D0D0D)   // Cuerpo negro
private val DustGold         = Color(0xFFFFD700)   // Polvo dorado
private val DustPink         = Color(0xFFFF80AB)   // Polvo rosa
private val DustCream        = Color(0xFFFFF176)   // Polvo crema
private val DustPeach        = Color(0xFFFFCCBC)   // Polvo durazno
private val BgTop            = Color(0xFFB3E5FC)   // Cielo azul
private val BgMid            = Color(0xFFE1F5FE)   // Cielo claro
private val BgBottom         = Color(0xFFFFF8E1)   // Amarillo cálido
private val FlowerRed        = Color(0xFFE91E63)   // Flor rosa fuerte
private val FlowerPurple     = Color(0xFFBA68C8)   // Flor morada
private val FlowerYellow     = Color(0xFFFFEB3B)   // Centro de flor
private val GreenLeaf        = Color(0xFF66BB6A)   // Hoja verde
private val GreenStem        = Color(0xFF388E3C)   // Tallo verde oscuro
private val CloudWhite       = Color(0xFFFFFFFF)   // Nubes

// ── Partículas de polvo mágico ────────────────────────────────────────────────
private data class DustParticle(
    val xDrift : Float,
    val yDrift : Float,
    val size   : Float,
    val delay  : Float,
    val colorR : Float,
    val colorG : Float,
    val colorB : Float
)

private val dustParticles: List<DustParticle> = run {
    val rng    = Random(42)
    val palette = listOf(DustGold, DustPink, DustCream, DustPeach)
    List(24) { i ->
        val col = palette[i % palette.size]
        DustParticle(
            xDrift = (rng.nextFloat() - 0.5f) * 80f,
            yDrift = 30f + rng.nextFloat() * 60f,
            size   = 1.5f + rng.nextFloat() * 3.5f,
            delay  = rng.nextFloat(),
            colorR = col.red,
            colorG = col.green,
            colorB = col.blue
        )
    }
}

// ── Composable principal ──────────────────────────────────────────────────────
@Composable
fun ButterflyScreen(onBack: () -> Unit) {
    var etapa   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val bodyAnim    = remember { Animatable(0f) }
    val wingsAnim   = remember { Animatable(0f) }
    val detailsAnim = remember { Animatable(0f) }

    val inf = rememberInfiniteTransition(label = "bf")

    // Aleteo: ángulo 0→360 que alimenta cos() para perspectiva
    val flapAngle by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flap"
    )

    // Vuelo en figura de 8 (Lissajous: x=sin(t), y=sin(2t))
    val flyT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fly"
    )

    // Polvo mágico progreso
    val dustT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dust"
    )

    // Brillo parpadeante en las alas
    val shimmer by inf.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val path = remember { Path() }

    LaunchedEffect(repetir) {
        etapa = 0
        bodyAnim.snapTo(0f)
        wingsAnim.snapTo(0f)
        detailsAnim.snapTo(0f)
        delay(400)

        etapa = 1
        bodyAnim.animateTo(1f, tween(700, easing = EaseOutBack))

        etapa = 2
        wingsAnim.animateTo(1f, tween(1400, easing = EaseOutCubic))

        etapa = 3
        detailsAnim.animateTo(1f, tween(900, easing = FastOutSlowInEasing))

        etapa = 4  // vuelo + polvo activos
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgTop, BgMid, BgBottom))
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind {
                        drawButterflyComposition(
                            scope = this,
                            etapa = etapa,
                            bodyAnimP = bodyAnim.value,
                            wingsAnimP = wingsAnim.value,
                            detailsAnimP = if (etapa >= 3) detailsAnim.value else 0f,
                            flapAngle = flapAngle,
                            flyT = flyT,
                            dustT = dustT,
                            shimmer = shimmer,
                            path = path
                        )
                    }
                }
        ) {}

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            val context = LocalContext.current
            val message = "🦋 ¡Mariposa Monarca! 🦋"
            val subMessage = "Libre como el viento 🌸"

            DrawingButtons(
                visible     = etapa >= 4,
                message     = message,
                subMessage  = subMessage,
                repeatEmoji = "🦋",
                accentColor = WingOrange,
                backgroundColor = BgMid, // Para contraste de texto
                onRepeat    = { repetir++ },
                onBack      = onBack,
                onSave = { includeText ->
                    saveButterflyAsImage(
                        context,
                        message,
                        subMessage,
                        BgMid, // Color representativo del fondo
                        includeText
                    )
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = WingOrangeLight)
        }
    }
}

// ── Composición completa ──────────────────────────────────────────────────────
private fun drawButterflyComposition(
    scope: DrawScope,
    etapa: Int,
    bodyAnimP: Float,
    wingsAnimP: Float,
    detailsAnimP: Float,
    flapAngle: Float,
    flyT: Float,
    dustT: Float,
    shimmer: Float,
    path: Path
) {
    with(scope) {
        val cx     = size.width / 2f
        val baseCy = size.height * 0.42f
        val s      = size.width / 400f

        // ── Nubes ─────────────────────────────────────────────
        bfCloud(cx - 110f * s, 55f  * s, 65f * s, 0.80f)
        bfCloud(cx +  90f * s, 90f  * s, 50f * s, 0.60f)
        bfCloud(cx -  20f * s, 128f * s, 38f * s, 0.40f)

        // ── Flores decorativas ────────────────────────────────
        bfFlower(40f * s,              size.height * 0.87f, 0.9f * s,  FlowerRed,    FlowerYellow)
        bfFlower(size.width - 45f * s, size.height * 0.85f, s,          FlowerPurple, FlowerYellow)
        bfFlower(size.width * 0.22f,   size.height * 0.93f, 0.65f * s, FlowerRed,    FlowerYellow)
        bfFlower(size.width * 0.78f,   size.height * 0.91f, 0.70f * s, FlowerPurple, FlowerYellow)

        // ── Posición con vuelo figura de 8 ───────────────────
        val flyOffX = if (etapa >= 4) sin(flyT)        * 28f * s else 0f
        val flyOffY = if (etapa >= 4) sin(2f * flyT)   * 14f * s else 0f
        val cx2 = cx + flyOffX
        val cy2 = baseCy + flyOffY

        // ── Perspectiva de aleteo ─────────────────────────────
        val flapRad   = flapAngle * PI.toFloat() / 180f
        val flapValue = if (etapa >= 4)
            (0.2f + 0.8f * abs(cos(flapRad))).coerceIn(0.2f, 1f)
        else 1f

        // ── Polvo mágico ──────────────────────────────────────
        if (etapa >= 4) {
            dustParticles.forEach { p ->
                val t     = (dustT + p.delay) % 1f
                val alpha = (sin(t * PI.toFloat()) * 0.85f).coerceIn(0f, 0.85f)
                val radius = p.size * s * (1f - t * 0.4f)
                drawCircle(
                    color  = Color(p.colorR, p.colorG, p.colorB, alpha),
                    radius = radius,
                    center = Offset(
                        cx2 + p.xDrift * s * t,
                        cy2 + p.yDrift * s * t
                    )
                )
            }
        }

        // ── Alas (debajo del cuerpo) ──────────────────────────
        if (etapa >= 2) {
            bfWings(
                cx      = cx2,
                cy      = cy2,
                s       = s,
                flap    = flapValue,
                wp      = wingsAnimP,
                dp      = detailsAnimP,
                shimmer = shimmer,
                path    = path
            )
        }

        // ── Cuerpo (encima de alas) ───────────────────────────
        if (etapa >= 1) {
            bfBody(
                cx    = cx2,
                cy    = cy2,
                s     = s,
                bp    = bodyAnimP,
                dp    = detailsAnimP,
                etapa = etapa,
                path  = path
            )
        }
    }
}

fun saveButterflyAsImage(
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
            // Fondo degradado
            drawRect(
                brush = Brush.verticalGradient(listOf(BgTop, BgMid, BgBottom)),
                size = size
            )
            
            drawButterflyComposition(
                scope = this,
                etapa = 4,
                bodyAnimP = 1f,
                wingsAnimP = 1f,
                detailsAnimP = 1f,
                flapAngle = 0f,
                flyT = 0f,
                dustT = 0f,
                shimmer = 1f,
                path = Path()
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

// ── Alas ──────────────────────────────────────────────────────────────────────
private fun DrawScope.bfWings(
    cx: Float, cy: Float, s: Float,
    flap: Float, wp: Float, dp: Float, shimmer: Float,
    path: Path
) {
    // Dibujamos lado derecho (flip=1) y lado izquierdo (flip=-1) con withTransform
    repeat(2) { side ->
        val flip = if (side == 0) 1f else -1f

        withTransform(
            transformBlock = {
                scale(scaleX = flap * flip, scaleY = 1f, pivot = Offset(cx, cy))
            }
        ) {

            // ── Ala Superior ──────────────────────────────────────────────────
            path.reset()
            path.moveTo(cx,             cy - 8f   * s)
            path.cubicTo(
                cx + 25f  * s * wp,  cy - 110f * s * wp,
                cx + 150f * s * wp,  cy - 100f * s * wp,
                cx + 162f * s * wp,  cy - 18f  * s * wp
            )
            path.cubicTo(
                cx + 140f * s * wp,  cy + 15f  * s,
                cx + 35f  * s * wp,  cy + 26f  * s,
                cx,                  cy + 8f   * s
            )
            path.close()
            drawPath(path, WingOrange)

            // Borde negro ala superior
            path.reset()
            path.moveTo(cx,             cy - 8f   * s)
            path.cubicTo(
                cx + 25f  * s * wp,  cy - 110f * s * wp,
                cx + 150f * s * wp,  cy - 100f * s * wp,
                cx + 162f * s * wp,  cy - 18f  * s * wp
            )
            drawPath(path, WingBlack, style = Stroke(13f * s, cap = StrokeCap.Round))

            // Interior más claro (reflejo de luz)
            path.reset()
            path.moveTo(cx + 12f  * s,      cy)
            path.cubicTo(
                cx + 32f  * s * wp,  cy - 88f  * s * wp,
                cx + 128f * s * wp,  cy - 82f  * s * wp,
                cx + 140f * s * wp,  cy - 16f  * s * wp
            )
            path.cubicTo(
                cx + 118f * s * wp,  cy + 12f  * s,
                cx + 32f  * s * wp,  cy + 20f  * s,
                cx + 12f  * s,       cy + 6f   * s
            )
            path.close()
            drawPath(path, WingOrangeLight.copy(alpha = 0.50f))

            // ── Ala Inferior ──────────────────────────────────────────────────
            path.reset()
            path.moveTo(cx,             cy + 8f   * s)
            path.cubicTo(
                cx + 18f  * s * wp,  cy + 52f  * s * wp,
                cx + 118f * s * wp,  cy + 108f * s * wp,
                cx + 88f  * s * wp,  cy + 148f * s * wp
            )
            path.cubicTo(
                cx + 54f  * s * wp,  cy + 162f * s * wp,
                cx + 14f  * s * wp,  cy + 112f * s * wp,
                cx,                  cy + 18f  * s
            )
            path.close()
            drawPath(path, WingOrangeLight)

            // Borde negro ala inferior
            path.reset()
            path.moveTo(cx,             cy + 8f   * s)
            path.cubicTo(
                cx + 18f  * s * wp,  cy + 52f  * s * wp,
                cx + 118f * s * wp,  cy + 108f * s * wp,
                cx + 88f  * s * wp,  cy + 148f * s * wp
            )
            path.cubicTo(
                cx + 54f  * s * wp,  cy + 162f * s * wp,
                cx + 14f  * s * wp,  cy + 112f * s * wp,
                cx,                  cy + 18f  * s
            )
            drawPath(path, WingBlack, style = Stroke(11f * s, cap = StrokeCap.Round))

            // ── Detalles: venas y manchas ─────────────────────────────────────
            if (dp > 0f) {

                // Venas ala superior
                drawLine(
                    color       = WingBlack.copy(alpha = 0.40f * dp),
                    start       = Offset(cx + 15f  * s,       cy - 5f  * s),
                    end         = Offset(cx + 42f  * s * wp,  cy - 92f * s * wp),
                    strokeWidth = 2f * s, cap = StrokeCap.Round
                )
                drawLine(
                    color       = WingBlack.copy(alpha = 0.40f * dp),
                    start       = Offset(cx + 42f  * s * wp,  cy - 12f * s),
                    end         = Offset(cx + 102f * s * wp,  cy - 92f * s * wp),
                    strokeWidth = 2f * s, cap = StrokeCap.Round
                )
                drawLine(
                    color       = WingBlack.copy(alpha = 0.40f * dp),
                    start       = Offset(cx + 82f  * s * wp,  cy - 5f  * s),
                    end         = Offset(cx + 140f * s * wp,  cy - 58f * s * wp),
                    strokeWidth = 2f * s, cap = StrokeCap.Round
                )

                // Manchas blancas en borde superior
                drawCircle(SpotWhite.copy(alpha = dp), 5.5f * s, Offset(cx + 145f * s, cy - 72f  * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5.5f * s, Offset(cx + 126f * s, cy - 90f  * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5.5f * s, Offset(cx + 104f * s, cy - 99f  * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5.5f * s, Offset(cx +  80f * s, cy - 101f * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5.5f * s, Offset(cx +  56f * s, cy - 98f  * s))

                // Manchas blancas en borde inferior
                drawCircle(SpotWhite.copy(alpha = dp), 5f * s, Offset(cx + 84f  * s, cy + 142f * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5f * s, Offset(cx + 98f  * s, cy + 120f * s))
                drawCircle(SpotWhite.copy(alpha = dp), 5f * s, Offset(cx + 107f * s, cy + 98f  * s))

                // Vena ala inferior
                path.reset()
                path.moveTo(cx + 5f * s,      cy + 15f  * s)
                path.quadraticTo(
                    cx + 55f * s * wp, cy + 82f  * s * wp,
                    cx + 75f * s * wp, cy + 132f * s * wp
                )
                drawPath(path, WingBlack.copy(alpha = 0.35f * dp), style = Stroke(2f * s))

                // Brillo shimmer parpadeante en ala superior
                drawCircle(
                    color  = SpotWhite.copy(alpha = 0.18f * dp * shimmer),
                    radius = 32f * s,
                    center = Offset(cx + 65f * s * wp, cy - 48f * s * wp)
                )
                // Brillo shimmer en ala inferior
                drawCircle(
                    color  = SpotWhite.copy(alpha = 0.12f * dp * shimmer),
                    radius = 22f * s,
                    center = Offset(cx + 45f * s * wp, cy + 68f * s * wp)
                )
            }
        }
    }
}

// ── Cuerpo ────────────────────────────────────────────────────────────────────
private fun DrawScope.bfBody(
    cx: Float, cy: Float, s: Float,
    bp: Float, dp: Float,
    etapa: Int, path: Path
) {
    // Abdomen: 6 segmentos que crecen de arriba a abajo
    val segs = 6
    repeat(segs) { i ->
        val segP = (bp * segs.toFloat() - i.toFloat()).coerceIn(0f, 1f)
        if (segP > 0f) {
            val w = (10f - i * 0.8f).coerceAtLeast(4f) * s
            val y = cy - 6f * s + i * 17f * s
            drawOval(
                color    = BodyDark,
                topLeft  = Offset(cx - w, y),
                size     = Size(w * 2f, 19f * s * segP)
            )
            // Línea de separación entre segmentos
            if (dp > 0f && i < segs - 1) {
                drawLine(
                    color       = Color(1f, 1f, 1f, 0.20f * dp),
                    start       = Offset(cx - w + 2f * s, y + 18f * s),
                    end         = Offset(cx + w - 2f * s, y + 18f * s),
                    strokeWidth = 1.5f * s
                )
            }
        }
    }

    // Tórax (más ancho que el abdomen)
    if (bp > 0.2f) {
        val tp = ((bp - 0.2f) / 0.8f).coerceIn(0f, 1f)
        drawOval(
            color   = BodyDark,
            topLeft = Offset(cx - 10f * s, cy - 24f * s),
            size    = Size(20f * s, 22f * s * tp)
        )
    }

    // Cabeza
    if (bp > 0.65f) {
        val hp = ((bp - 0.65f) / 0.35f).coerceIn(0f, 1f)
        drawCircle(BodyDark, 10f * s * hp, Offset(cx, cy - 30f * s))

        // Ojos con reflejo
        if (dp > 0f) {
            drawCircle(SpotWhite.copy(alpha = dp),        3.5f * s, Offset(cx - 5f * s, cy - 32f * s))
            drawCircle(SpotWhite.copy(alpha = dp),        3.5f * s, Offset(cx + 5f * s, cy - 32f * s))
            drawCircle(BodyDark,                          2f   * s, Offset(cx - 5f * s, cy - 32f * s))
            drawCircle(BodyDark,                          2f   * s, Offset(cx + 5f * s, cy - 32f * s))
            drawCircle(SpotWhite.copy(alpha = dp * 0.7f), 0.8f * s, Offset(cx - 4f * s, cy - 33f * s))
            drawCircle(SpotWhite.copy(alpha = dp * 0.7f), 0.8f * s, Offset(cx + 6f * s, cy - 33f * s))
        }
    }

    // Antenas
    if (etapa >= 3 && dp > 0f) {
        // Antena izquierda
        path.reset()
        path.moveTo(cx - 4f * s, cy - 36f * s)
        path.quadraticTo(
            cx - 22f * s,
            cy - 58f * s * dp,
            cx - 28f * s,
            cy - 72f * s * dp
        )
        drawPath(path, BodyDark.copy(alpha = dp), style = Stroke(2.5f * s, cap = StrokeCap.Round))
        drawCircle(BodyDark.copy(alpha = dp), 4.5f * s * dp, Offset(cx - 28f * s, cy - 72f * s))

        // Antena derecha
        path.reset()
        path.moveTo(cx + 4f * s, cy - 36f * s)
        path.quadraticTo(
            cx + 22f * s,
            cy + (-58f) * s * dp,
            cx + 28f * s,
            cy + (-72f) * s * dp
        )
        drawPath(path, BodyDark.copy(alpha = dp), style = Stroke(2.5f * s, cap = StrokeCap.Round))
        drawCircle(BodyDark.copy(alpha = dp), 4.5f * s * dp, Offset(cx + 28f * s, cy - 72f * s))
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun DrawScope.bfCloud(cx: Float, cy: Float, size: Float, alpha: Float) {
    val c = CloudWhite.copy(alpha = alpha)
    drawCircle(c, size * 0.50f, Offset(cx - size * 0.28f, cy))
    drawCircle(c, size * 0.65f, Offset(cx,                cy))
    drawCircle(c, size * 0.42f, Offset(cx + size * 0.32f, cy))
}

private fun DrawScope.bfFlower(
    cx: Float, cy: Float, s: Float,
    petalColor: Color, centerColor: Color
) {
    val stemH = 38f * s
    // Tallo
    drawLine(GreenStem, Offset(cx, cy), Offset(cx, cy - stemH), 3f * s, cap = StrokeCap.Round)
    // Hoja lateral
    drawLine(
        GreenLeaf,
        Offset(cx, cy - stemH * 0.38f),
        Offset(cx - 11f * s, cy - stemH * 0.52f),
        2.5f * s, cap = StrokeCap.Round
    )
    // 6 pétalos
    repeat(6) { i ->
        val angle = i.toFloat() * 60f * PI.toFloat() / 180f
        drawCircle(
            color  = petalColor,
            radius = 8f * s,
            center = Offset(
                x = cx + cos(angle) * 11f * s,
                y = cy - stemH + sin(angle) * 11f * s
            )
        )
    }
    // Centro de la flor
    drawCircle(centerColor, 7f * s, Offset(cx, cy - stemH))
    drawCircle(Color(0xFFFF8F00).copy(alpha = 0.4f), 3f * s, Offset(cx, cy - stemH))
}
