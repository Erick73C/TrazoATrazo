package com.example.trazoatrazo.drawings.flowers

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
import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Paleta ────────────────────────────────────────────────────────────────────
private val BgDark       = Color(0xFF121212)     // Fondo carbón oscuro: hace que los rojos y blancos "brillen"
private val RosaRed      = Color(0xFFD32F2F)     // Rojo intenso y vibrante
private val RosaRedDark  = Color(0xFF7B0000)     // Rojo profundo para sombras de pétalos interiores
private val RosaRedLight = Color(0xFFFF5252)     // Rojo coral brillante para los bordes iluminados
private val RosaWhite    = Color(0xFFFFFFFF)
private val RosaWhiteShadow = Color(0xFFD1C4E9)  // Sombra lavanda suave para la variante blanca
private val RosaGreen       = Color(0xFF388E3C)  // Verde para el tallo y hojas
private val RosaGreenDark   = Color(0xFF1B5E20)  // Verde oscuro para detalles

// ── Un pétalo = una curva cerrada tipo "cuchara redondeada" en una capa de la espiral ─
data class PetalSpec(
    val layer: Int,        // 0 = centro/capullo, N = capa externa
    val angle: Float,       // ángulo de posición alrededor del centro (grados)
    val distance: Float,    // distancia del origen del pétalo al centro de la flor
    val length: Float,      // largo del pétalo
    val width: Float,       // ancho máximo del pétalo
    val curl: Float,        // curvatura hacia el centro (0 = plano, 1 = muy enrollado)
    val tilt: Float         // grados extra de apertura respecto a la dirección radial
)

// ── Genera la espiral de una rosa realista: 7 capas, centro enrollado, apertura progresiva ─
// Geometría verificada visualmente antes de portar (ver prototipo): capullo compacto al centro,
// cada capa hacia afuera es más grande, menos curvada (curl) y más "abierta" (tilt mayor).
private fun generatePetals(): List<PetalSpec> {
    val petals = mutableListOf<PetalSpec>()

    data class LayerDef(
        val count: Int, val distance: Float, val length: Float,
        val width: Float, val curl: Float, val offset: Float, val tilt: Float
    )

    val layers = listOf(
        LayerDef(count = 1, distance = 0.0f,   length = 6.5f,  width = 7.5f,  curl = 0.9f,  offset = 0f,  tilt = 0f),
        LayerDef(count = 3, distance = 1.2f,   length = 8.5f,  width = 9.5f,  curl = 0.8f,  offset = 60f, tilt = 8f),
        LayerDef(count = 4, distance = 3.0f,   length = 11.0f, width = 13.0f, curl = 0.65f, offset = 25f, tilt = 16f),
        LayerDef(count = 5, distance = 5.5f,   length = 14.5f, width = 17.0f, curl = 0.52f, offset = 45f, tilt = 24f),
        LayerDef(count = 6, distance = 8.8f,   length = 18.0f, width = 21.0f, curl = 0.40f, offset = 15f, tilt = 34f),
        LayerDef(count = 7, distance = 12.5f,  length = 22.0f, width = 25.5f, curl = 0.28f, offset = 35f, tilt = 45f),
        LayerDef(count = 8, distance = 16.5f,  length = 25.0f, width = 30.0f, curl = 0.15f, offset = 0f,  tilt = 58f)
    )

    layers.forEachIndexed { layerIndex, def ->
        for (i in 0 until def.count) {
            val angle = (360f / def.count) * i + def.offset
            petals.add(
                PetalSpec(
                    layer = layerIndex,
                    angle = angle,
                    distance = def.distance,
                    length = def.length,
                    width = def.width,
                    curl = def.curl,
                    tilt = def.tilt
                )
            )
        }
    }

    return petals
}

/**
 * Construye el Path de un pétalo con forma de "cuchara redondeada": ancho máximo
 * a media altura y punta curva sin picos, con curvatura (curl) hacia el centro.
 * Coordenadas locales: (0,0) = base del pétalo, la punta queda en y = -length.
 */
private fun buildPetalPath(length: Float, width: Float, curl: Float, steps: Int = 20): Path {
    val path = Path()
    val halfW = width / 2f

    val leftPoints = mutableListOf<Offset>()
    val rightPoints = mutableListOf<Offset>()

    for (s in 0..steps) {
        val t = s / steps.toFloat()
        // Perfil elíptico: se ensancha hasta t≈0.5 y se cierra redondeado hacia t=1 (sin pico)
        val widthFactor = sqrt(max(0f, 1f - ((t - 0.5f) / 0.62f).pow(2)))
        val w = halfW * widthFactor
        val y = -length * t
        val curlOffset = -curl * length * 0.22f * t.pow(1.7f)

        leftPoints.add(Offset(-w + curlOffset, y))
        rightPoints.add(Offset(w + curlOffset, y))
    }

    path.moveTo(leftPoints.first().x, leftPoints.first().y)
    for (p in leftPoints.drop(1)) path.lineTo(p.x, p.y)
    for (p in rightPoints.asReversed()) path.lineTo(p.x, p.y)
    path.close()

    return path
}

/** Dibuja un solo pétalo ya posicionado, rotado y con su color/sombra. */
private fun DrawScope.drawPetalShape(
    center: Offset,
    spec: PetalSpec,
    scale: Float,
    growProgress: Float,   // 0..1 qué tan "crecido" está este pétalo puntual
    baseColor: Color,
    shadowColor: Color,
    highlightColor: Color,
    strokeOnly: Boolean,
    strokeColor: Color = Color.Black
) {
    if (growProgress <= 0f) return

    val rad = Math.toRadians(spec.angle.toDouble()).toFloat()
    val dist = spec.distance * scale
    val originX = center.x + cos(rad) * dist
    val originY = center.y + sin(rad) * dist

    val len = spec.length * scale * growProgress
    val wid = spec.width * scale
    val path = buildPetalPath(len, wid, spec.curl)

    // El pétalo apunta hacia afuera desde el centro (radial) + su apertura (tilt) propia de capa
    val rotationDeg = spec.angle + 90f + spec.tilt

    withTransform({
        translate(originX, originY)
        rotate(rotationDeg, pivot = Offset.Zero)
    }) {
        if (strokeOnly) {
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = max(1.5f, scale * 0.12f), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        } else {
            // Relleno con degradado radial para simular profundidad esférica en la flor
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(highlightColor, baseColor, shadowColor),
                    startY = -len,
                    endY = 0f
                )
            )
            // Brillo en el borde superior para dar realismo 3D
            drawPath(
                path = path,
                color = highlightColor.copy(alpha = 0.4f),
                style = Stroke(width = max(0.5f, scale * 0.03f))
            )
            // Línea central sutil que marca el pliegue del pétalo
            drawLine(
                color = shadowColor.copy(alpha = 0.3f),
                start = Offset(0f, -len * 0.15f),
                end = Offset(0f, -len * 0.8f),
                strokeWidth = max(0.8f, scale * 0.04f)
            )
        }
    }
}

/** Tallo con espinas y hojas de rosa (la parte verde) */
private fun DrawScope.drawRosaStemAndLeaves(center: Offset, scale: Float, progress: Float) {
    if (progress <= 0f) return
    val s = scale
    val stemLength = 65f * s * progress
    val strokeW = max(2.5f, s * 0.12f)
    val color = RosaGreen

    // Tallo principal
    drawLine(
        color = color,
        start = center,
        end = Offset(center.x, center.y + stemLength),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )

    // Sépalos (hojitas verdes que asoman bajo la flor)
    if (progress > 0.1f) {
        val sepT = ((progress - 0.1f) / 0.3f).coerceIn(0f, 1f)
        for (i in 0 until 5) {
            val angle = i * 72f + 15f
            withTransform({
                rotate(angle, pivot = center)
            }) {
                val sepalPath = Path().apply {
                    moveTo(center.x, center.y)
                    // Hojitas significativamente más largas para que sobresalgan de los pétalos externos
                    cubicTo(
                        center.x - 6f * s * sepT, center.y + 15f * s * sepT,
                        center.x + 6f * s * sepT, center.y + 15f * s * sepT,
                        center.x, center.y + 38f * s * sepT
                    )
                    close()
                }
                drawPath(sepalPath, color = color)
                drawPath(sepalPath, color = RosaGreenDark.copy(alpha = 0.5f), style = Stroke(width = 0.8f * s))
            }
        }
    }

    // Hojas laterales y espinas
    if (progress > 0.4f) {
        val leafT = ((progress - 0.4f) / 0.6f).coerceIn(0f, 1f)
        
        // Espinas icónicas de la rosa
        val thornY = listOf(20f, 45f)
        thornY.forEachIndexed { index, y ->
            val side = if (index % 2 == 0) 1 else -1
            val thornPath = Path().apply {
                moveTo(center.x, center.y + y * s)
                lineTo(center.x + 3.5f * s * side * leafT, center.y + (y + 2f) * s)
                lineTo(center.x, center.y + (y + 4f) * s)
                close()
            }
            drawPath(thornPath, color = RosaGreenDark.copy(alpha = 0.8f * leafT))
        }

        // Hojas dentadas más abajo en el tallo
        for (side in listOf(-1, 1)) {
            val yPos = if (side == 1) 42f else 58f
            val leafPath = Path().apply {
                moveTo(center.x, center.y + yPos * s)
                cubicTo(
                    center.x + 25f * s * side * leafT, center.y + (yPos - 15f) * s,
                    center.x + 32f * s * side * leafT, center.y + (yPos + 10f) * s,
                    center.x + 6f * s * side * leafT, center.y + (yPos + 18f) * s
                )
                close()
            }
            drawPath(leafPath, color = color.copy(alpha = leafT))
            drawPath(leafPath, color = RosaGreenDark.copy(alpha = 0.5f * leafT), style = Stroke(width = 1.2f * s))
        }
    }
}

/**
 * Dibuja la rosa completa en un estado de crecimiento dado.
 * drawT: 0..1 progreso global de "cuántos pétalos ya se trazaron"
 * fillT: 0..1 opacidad del relleno una vez trazado
 * stemT: 0..1 progreso de la parte verde (tallo/hojas)
 */
private fun DrawScope.drawRosaAtProgress(
    center: Offset,
    scale: Float,
    beatScale: Float,
    drawT: Float,
    fillT: Float,
    stemT: Float,
    petals: List<PetalSpec>,
    fillColor: Color,
    shadowColor: Color,
    highlightColor: Color,
    strokeColor: Color
) {
    val s = scale * beatScale

    // Dibujar primero la parte verde (atrás de los pétalos)
    drawRosaStemAndLeaves(center, s, stemT)

    // Ordenamos de capa alta (externa) a capa baja (interna) para el TRAZO,
    // pero para el RELLENO dibujamos de adentro hacia afuera para que se superpongan bien.
    val orderedForFill = petals.sortedBy { it.layer }       // interior primero → exterior encima
    val orderedForStroke = petals.sortedByDescending { it.layer } // exterior primero → aparecen antes en la animación

    // ── Trazo progresivo: capa por capa se va "revelando" el contorno ──────────
    val strokeCount = (orderedForStroke.size * drawT).toInt().coerceIn(0, orderedForStroke.size)
    for (i in 0 until strokeCount) {
        val petal = orderedForStroke[i]
        // Progreso individual de este pétalo dentro de su ventana de aparición (efecto cascada suave)
        val windowSize = 1f / orderedForStroke.size
        val localT = ((drawT - i * windowSize) / windowSize).coerceIn(0f, 1f)
        drawPetalShape(
            center = center, spec = petal, scale = s, growProgress = localT.coerceAtLeast(0.15f),
            baseColor = fillColor, shadowColor = shadowColor, highlightColor = highlightColor,
            strokeOnly = true, strokeColor = strokeColor
        )
    }

    // ── Relleno: una vez que el trazo avanza, se rellena con degradado ─────────
    if (fillT > 0f) {
        for (petal in orderedForFill) {
            drawPetalShape(
                center = center, spec = petal, scale = s, growProgress = 1f,
                baseColor = fillColor.copy(alpha = fillT),
                shadowColor = shadowColor.copy(alpha = fillT),
                highlightColor = highlightColor.copy(alpha = fillT),
                strokeOnly = false
            )
        }
        // Recontorneamos encima para que los bordes se vean nítidos sobre el relleno
        for (petal in orderedForStroke) {
            drawPetalShape(
                center = center, spec = petal, scale = s, growProgress = 1f,
                baseColor = fillColor, shadowColor = shadowColor, highlightColor = highlightColor,
                strokeOnly = true, strokeColor = strokeColor.copy(alpha = 0.5f * fillT)
            )
        }
    }
}

// ── Pantalla de Rosa Interactiva ───────────────────────────────────────────────
@Composable
fun RosaScreen(onBack: () -> Unit) {

    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }
    var isWhiteMode by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val drawProgress = remember { Animatable(0f) }
    val fillAlpha = remember { Animatable(0f) }
    val stemProgress = remember { Animatable(0f) }
    val entryScale = remember { Animatable(0.8f) }

    // Pulso continuo cuando termina
    val pulso = rememberInfiniteTransition(label = "pulso_rosa")
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
        label = "beat_rosa"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "press_scale"
    )

    val petals = remember { generatePetals() }

    // Motor de animación principal — se reinicia cada vez que `repetir` cambia
    LaunchedEffect(repetir) {
        etapa = 0
        drawProgress.snapTo(0f)
        fillAlpha.snapTo(0f)
        stemProgress.snapTo(0f)
        entryScale.snapTo(0.8f)

        delay(400L)

        // 0. Animación de entrada
        launch {
            entryScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        // 1. Tallo y sépalos primero
        etapa = 1
        stemProgress.animateTo(1f, tween(1000, easing = EaseOutCubic))

        delay(150L)

        // 2. Trazo de pétalos
        etapa = 2
        drawProgress.animateTo(1f, tween(2600, easing = LinearEasing))

        delay(200L)

        // 3. Relleno de color
        etapa = 3
        fillAlpha.animateTo(1f, tween(800, easing = EaseOutCubic))

        delay(300L)
        etapa = 4 // completo — botones visibles, pulso activo
    }

    // ── Colores según el modo actual (SIN animación de interpolación: se define
    //    directamente por isWhiteMode, así el cambio siempre es instantáneo y correcto) ──
    val fillColor      = if (isWhiteMode) RosaWhite else RosaRed
    val shadowColor    = if (isWhiteMode) RosaWhiteShadow else RosaRedDark
    val highlightColor = if (isWhiteMode) RosaWhite else RosaRedLight
    val strokeColor    = if (isWhiteMode) Color(0xFFBFA8AE) else RosaRedDark

    val bgColor = BgDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Toggle rojo/blanco al tocar la rosa — solo cuando ya terminó de dibujarse
                if (etapa >= 4) {
                    isWhiteMode = !isWhiteMode
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

        // Halo pulsante cuando está completo
        if (etapa >= 4) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val haloAlpha = (beatScale - 1f) * 3f
                drawCircle(
                    color = fillColor.copy(alpha = 0.06f + haloAlpha * 0.05f),
                    radius = size.width * 0.32f,
                    center = Offset(size.width / 2f, size.height * 0.35f)
                )
            }
        }

        // Canvas principal de la rosa
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pressScale * entryScale.value
                    scaleY = pressScale * entryScale.value
                }
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.35f
            val scale = size.width / 100f

            val beat = if (etapa >= 4) beatScale else 1f

            drawRosaAtProgress(
                center = Offset(cx, cy),
                scale = scale,
                beatScale = beat,
                drawT = drawProgress.value,
                fillT = fillAlpha.value,
                stemT = stemProgress.value,
                petals = petals,
                fillColor = fillColor,
                shadowColor = shadowColor,
                highlightColor = highlightColor,
                strokeColor = strokeColor
            )
        }

        // ── Indicador de modo de color (arriba a la derecha) — también sirve de botón ─
        if (etapa >= 4) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 24.dp)
                    .size(48.dp)
                    .background(color = fillColor, shape = CircleShape)
                    .border(2.dp, Color(0xFFDDDDDD), CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { isWhiteMode = !isWhiteMode },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isWhiteMode) "⚪" else "🔴",
                    fontSize = 22.sp
                )
            }
        }

        // ── Botones ──────────────────────────────────────────────────────
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = etapa >= 4,
                message = "🌹 Rosa para ti 🌹",
                subMessage = if (isWhiteMode) "Blanca y pura" else "Roja de pasión",
                repeatEmoji = "🌹",
                accentColor = if (isWhiteMode) Color(0xFF9E9E9E) else RosaRed,
                onRepeat = { repetir++ },
                onBack = onBack,
                onSave = { includeText ->
                    saveRosaAsImage(
                        context = context,
                        message = "🌹 Rosa para ti 🌹",
                        subMessage = if (isWhiteMode) "Blanca y pura" else "Roja de pasión",
                        bgColor = bgColor,
                        fillColor = fillColor,
                        shadowColor = shadowColor,
                        highlightColor = highlightColor,
                        strokeColor = strokeColor,
                        includeText = includeText,
                        petals = petals
                    )
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color(0xFFEAEAEA))
        }
    }
}

// ── Función de guardado en galería ─────────────────────────────────────────────
fun saveRosaAsImage(
    context: android.content.Context,
    message: String,
    subMessage: String,
    bgColor: Color,
    fillColor: Color,
    shadowColor: Color,
    highlightColor: Color,
    strokeColor: Color,
    includeText: Boolean,
    petals: List<PetalSpec>
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
            val cy = artSize * 0.35f
            val scale = artSize / 100f

            drawRosaAtProgress(
                center = Offset(cx, cy),
                scale = scale,
                beatScale = 1f,
                drawT = 1f,
                fillT = 1f,
                stemT = 1f,
                petals = petals,
                fillColor = fillColor,
                shadowColor = shadowColor,
                highlightColor = highlightColor,
                strokeColor = strokeColor
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

// ── PREVIEW para ver en Android Studio sin ejecutar ─────────────────────────────
@Preview(
    showBackground = true,
    widthDp = 400,
    heightDp = 800,
    backgroundColor = 0xFF121212
)
@Composable
fun RosaScreenPreview() {
    RosaScreen(onBack = {})
}