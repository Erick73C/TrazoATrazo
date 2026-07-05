package com.example.trazoatrazo.drawings.winter

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
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

// ── Paleta de Colores Navideños Premium ────────────────────────────────────
private val TreeGreenDark = Color(0xFF082E10)
private val TreeGreenMed = Color(0xFF1B5E20)
private val TreeGreenLight = Color(0xFF2E7D32)
private val TrunkBrown = Color(0xFF3E2723)
private val PotRed = Color(0xFF8E0000)
private val StarGold = Color(0xFFFFD700)
private val StarCore = Color(0xFFFFF9C4)
private val OrnamentRed = Color(0xFFF44336)
private val OrnamentGold = Color(0xFFFFC107)
private val OrnamentBlue = Color(0xFF2196F3)
private val GarlandSilver = Color(0xFFCFD8DC)
private val BgDarkWinter = Color(0xFF01040A)
private val RibbonGold = Color(0xFFFFD600)
private val RibbonWhite = Color(0xFFECEFF1)

@Composable
fun ChristmasTreeScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val baseAnim = remember { Animatable(0f) }
    val treeAnim = remember { Animatable(0f) }
    val ornamentsAnim = remember { Animatable(0f) }
    val starAnim = remember { Animatable(0f) }
    val giftsAnim = remember { Animatable(0f) }

    // Animación de luces parpadeantes
    val infiniteTransition = rememberInfiniteTransition(label = "xmas_vibe")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowPulse"
    )

    // Animación de nieve (más densa)
    val snowflakes = List(60) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500 + i * 80, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "snow_$i"
        )
    }

    // Regalos en la base
    val giftBoxes = remember {
        listOf(
            // Position, Size, MainColor, RibbonColor
            Triple(Offset(-80f, 15f), Size(60f, 50f), Color(0xFFB71C1C) to RibbonGold),
            Triple(Offset(40f, 20f), Size(50f, 55f), Color(0xFF0D47A1) to RibbonWhite),
            Triple(Offset(-120f, 35f), Size(45f, 40f), Color(0xFF33691E) to RibbonGold),
            Triple(Offset(100f, 30f), Size(55f, 45f), Color(0xFFE65100) to RibbonWhite)
        )
    }

    // Esferas optimizadas para árbol más alto
    val ornaments = remember {
        val list = mutableListOf<Triple<Offset, Float, Color>>()
        val random = Random(88)
        val levels = 5 // Árbol más alto -> más niveles
        for (i in 0 until levels) {
            val levelY = -100f - (i * 95f)
            val spread = (180f - i * 35f)
            val count = 5 - i
            for (j in 0 until count) {
                val x = ((j.toFloat() / (count - 1).coerceAtLeast(1)) - 0.5f) * spread * 0.9f
                val color = when(random.nextInt(3)) {
                    0 -> OrnamentRed
                    1 -> OrnamentGold
                    else -> OrnamentBlue
                }
                list.add(Triple(Offset(x, levelY + (random.nextFloat()-0.5f)*25f), 10f + random.nextFloat() * 4f, color))
            }
        }
        list
    }

    LaunchedEffect(repetir) {
        etapa = 0
        baseAnim.snapTo(0f); treeAnim.snapTo(0f)
        ornamentsAnim.snapTo(0f); starAnim.snapTo(0f); giftsAnim.snapTo(0f)
        delay(400)

        etapa = 1 // Base y Tronco
        baseAnim.animateTo(1f, tween(1000, easing = EaseOutBack))
        
        etapa = 2 // Hojas (Más capas y más alto)
        treeAnim.animateTo(1f, tween(2500, easing = EaseOutQuart))
        
        etapa = 3 // Esferas y Guirnalda
        ornamentsAnim.animateTo(1f, tween(1500))
        
        etapa = 4 // Estrella
        starAnim.animateTo(1f, tween(1000, easing = EaseOutBounce))

        etapa = 5 // Regalos finales
        giftsAnim.animateTo(1f, tween(1200, easing = EaseOutBack))
        
        etapa = 6
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkWinter)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height * 0.82f // Bajamos la base para que sea más alto
                    val scale = size.width / 450f
                    
                    onDrawBehind {
                        drawChristmasTreeComposition(
                            etapa = etapa,
                            baseAnimP = baseAnim.value,
                            treeAnimP = treeAnim.value,
                            ornamentsAnimP = ornamentsAnim.value,
                            starAnimP = starAnim.value,
                            giftsAnimP = giftsAnim.value,
                            glowPulse = glowPulse,
                            snowPositions = snowflakes.map { it.value },
                            giftBoxes = giftBoxes,
                            ornaments = ornaments,
                            cx = cx,
                            cy = cy,
                            scale = scale
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
            val message = "🎄 ¡El Gran Árbol de Navidad! 🎄"
            val subMessage = "Regalos y magia bajo las estrellas"

            DrawingButtons(
                visible = etapa >= 6,
                message = message,
                subMessage = subMessage,
                repeatEmoji = "🎁",
                accentColor = PotRed,
                backgroundColor = BgDarkWinter,
                onRepeat = { repetir++ },
                onBack = onBack,
                onSave = { includeText ->
                    saveChristmasTreeAsImage(
                        context,
                        message,
                        subMessage,
                        BgDarkWinter,
                        includeText,
                        giftBoxes,
                        ornaments
                    )
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = StarGold)
        }
    }
}

private fun DrawScope.drawChristmasTreeComposition(
    etapa: Int,
    baseAnimP: Float,
    treeAnimP: Float,
    ornamentsAnimP: Float,
    starAnimP: Float,
    giftsAnimP: Float,
    glowPulse: Float,
    snowPositions: List<Float>?,
    giftBoxes: List<Triple<Offset, Size, Pair<Color, Color>>>,
    ornaments: List<Triple<Offset, Float, Color>>,
    cx: Float,
    cy: Float,
    scale: Float
) {
    // 1. Efecto de nieve ambiental
    snowPositions?.forEachIndexed { i, p ->
        val x = ((size.width / snowPositions.size.toFloat()) * i.toFloat() + sin((p * 2f * PI.toFloat() + i.toFloat()).toDouble()).toFloat() * 30f) % size.width
        val y = p * size.height
        drawCircle(Color.White.copy(alpha = 0.4f), 2f * scale, Offset(x, y))
    }

    // 2. Base (Maceta) y Tronco
    if (etapa >= 1) {
        val p = baseAnimP
        // Suelo nevado sutil
        drawOval(
            color = Color.White.copy(0.1f),
            topLeft = Offset(cx - 200f * scale, cy + 20f * scale),
            size = Size(440f * scale, 60f * scale)
        )
        // Tronco robusto
        drawRect(
            color = TrunkBrown,
            topLeft = Offset(cx - 25f * scale, cy - 80f * scale * p),
            size = Size(50f * scale, 80f * scale * p)
        )
        // Maceta elegante
        val potPath = Path().apply {
            moveTo(cx - 60f * scale * p, cy)
            lineTo(cx + 60f * scale * p, cy)
            lineTo(cx + 50f * scale * p, cy + 50f * scale * p)
            lineTo(cx - 50f * scale * p, cy + 50f * scale * p)
            close()
        }
        drawPath(potPath, PotRed)
        drawPath(potPath, Color.Black.copy(0.3f), style = Stroke(3f * scale))
    }

    // 3. Follaje Navideño Extra Alto (5 capas)
    if (etapa >= 2) {
        val p = treeAnimP
        val layers = 5
        for (i in 0 until layers) {
            val layerP = (p * layers - i).coerceIn(0f, 1f)
            if (layerP > 0f) {
                val width = (220f - i * 35f) * scale * layerP
                val height = 125f * scale * layerP
                val baseY = cy - 50f * scale - (i * 85f * scale)
                
                val leafPath = Path().apply {
                    moveTo(cx, baseY - height)
                    quadraticTo(cx - width * 0.6f, baseY - height * 0.4f, cx - width, baseY)
                    // Efecto de puntas de pino
                    for (j in 1..8) {
                        val step = (width * 2) / 8
                        val px = cx - width + step * j
                        val py = baseY + (if(j % 2 == 0) 15f else 5f) * scale
                        lineTo(px, py)
                    }
                    lineTo(cx + width, baseY)
                    quadraticTo(cx + width * 0.6f, baseY - height * 0.4f, cx, baseY - height)
                    close()
                }
                
                drawPath(
                    path = leafPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(TreeGreenLight, TreeGreenDark),
                        startY = baseY - height,
                        endY = baseY
                    )
                )
                drawPath(leafPath, TreeGreenMed.copy(0.4f), style = Stroke(2f * scale))
            }
        }
    }

    // 4. Guirnaldas y Esferas
    if (etapa >= 3) {
        val dP = ornamentsAnimP
        
        // Guirnaldas doradas y plateadas
        for (i in 0 until 4) {
            val gP = (dP * 4 - i).coerceIn(0f, 1f)
            if (gP > 0f) {
                val yPos = cy - 120f * scale - (i * 90f * scale)
                val w = (170f - i * 35f) * scale
                val garlandPath = Path().apply {
                    moveTo(cx - w, yPos)
                    quadraticTo(cx, yPos + 50f * scale, cx + w, yPos - 30f * scale)
                }
                drawPath(
                    garlandPath,
                    color = if(i % 2 == 0) GarlandSilver.copy(0.5f * gP) else OrnamentGold.copy(0.4f * gP),
                    style = Stroke(5f * scale, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
                )
            }
        }

        // Esferas con brillo dinámico
        ornaments.forEachIndexed { i, (pos, radius, color) ->
            val ornamentP = (dP * ornaments.size - i).coerceIn(0f, 1f)
            if (ornamentP > 0f) {
                val finalPos = Offset(cx + pos.x * scale, cy + pos.y * scale)
                drawCircle(Color.Black.copy(0.4f), radius * scale * ornamentP, finalPos + Offset(3f, 3f))
                drawCircle(color, radius * scale * ornamentP, finalPos)
                // Luces parpadeantes dentro de las esferas
                drawCircle(
                    Color.White.copy(alpha = 0.7f * glowPulse),
                    radius * 0.25f * scale * ornamentP,
                    finalPos - Offset(radius * 0.3f * scale, radius * 0.3f * scale)
                )
            }
        }
    }

    // 5. Estrella Brillante
    if (etapa >= 4) {
        val sP = starAnimP
        val starY = cy - 520f * scale // Mucho más arriba por el árbol alto
        withTransform({
            translate(cx, starY)
            scale(sP, sP, Offset.Zero)
            rotate(sP * 1080f)
        }) {
            val starPath = Path()
            val points = 10
            for (i in 0 until points) {
                val angle = Math.toRadians(i * (360.0 / points) - 90.0).toFloat()
                val r = if (i % 2 == 0) 38f * scale else 15f * scale
                val x = r * cos(angle); val y = r * sin(angle)
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()
            
            drawCircle(
                brush = Brush.radialGradient(listOf(StarGold.copy(0.7f * glowPulse), Color.Transparent), radius = 80f * scale),
                radius = 80f * scale, center = Offset.Zero
            )
            drawPath(starPath, StarGold)
            drawPath(starPath, StarCore, alpha = 0.8f, style = Fill)
        }
    }

    // 6. Regalos Detallados
    if (etapa >= 5) {
        val gP = giftsAnimP
        giftBoxes.forEachIndexed { i, (pos, sizeVal, colors) ->
            val boxP = (gP * giftBoxes.size - i).coerceIn(0f, 1f)
            if (boxP > 0f) {
                val boxPos = Offset(cx + pos.x * scale, cy + pos.y * scale)
                val (mainColor, ribbonColor) = colors
                
                withTransform({
                    scale(boxP, boxP, boxPos + Offset(sizeVal.width/2, sizeVal.height/2))
                }) {
                    // Caja
                    drawRoundRect(
                        color = mainColor,
                        topLeft = boxPos,
                        size = sizeVal * scale,
                        cornerRadius = CornerRadius(4f * scale)
                    )
                    // Cinta Vertical
                    drawRect(
                        color = ribbonColor,
                        topLeft = Offset(boxPos.x + (sizeVal.width * 0.4f) * scale, boxPos.y),
                        size = Size((sizeVal.width * 0.2f) * scale, sizeVal.height * scale)
                    )
                    // Cinta Horizontal
                    drawRect(
                        color = ribbonColor,
                        topLeft = Offset(boxPos.x, boxPos.y + (sizeVal.height * 0.4f) * scale),
                        size = Size(sizeVal.width * scale, (sizeVal.height * 0.2f) * scale)
                    )
                    // Moño sutil
                    drawCircle(ribbonColor, 8f * scale, Offset(boxPos.x + (sizeVal.width*0.5f)*scale, boxPos.y))
                }
            }
        }
    }
}

fun saveChristmasTreeAsImage(
    context: android.content.Context,
    message: String,
    subMessage: String,
    bgColor: Color,
    includeText: Boolean,
    giftBoxes: List<Triple<Offset, Size, Pair<Color, Color>>>,
    ornaments: List<Triple<Offset, Float, Color>>
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
            val cy = artSize * 0.82f
            val scale = artSize / 450f
            
            drawChristmasTreeComposition(
                etapa = 5,
                baseAnimP = 1f,
                treeAnimP = 1f,
                ornamentsAnimP = 1f,
                starAnimP = 1f,
                giftsAnimP = 1f,
                glowPulse = 1f,
                snowPositions = null, // No dibujamos nieve en el guardado para que sea más limpio
                giftBoxes = giftBoxes,
                ornaments = ornaments,
                cx = cx,
                cy = cy,
                scale = scale
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
