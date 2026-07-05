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
import kotlin.math.*
import kotlinx.coroutines.delay
import kotlin.random.Random

private val PinkColor = Color(0xFFFFB7C5)
private val LightPinkColor = Color(0xFFFCE4EC)
private val DarkPinkColor = Color(0xFFD81B60)
private val TrunkColor = Color(0xFF5D4037)
private val TrunkDark = Color(0xFF3E2723)
private val TrunkLight = Color(0xFF795548)
private val BranchColor = Color(0xFF6D4C41)
private val GrassColor = Color(0xFF7CB342)
private val GrassDark = Color(0xFF558B2F)
private val BgSky = Color(0xFFE3F2FD)
private val CloudColor = Color(0xFFFFFFFF)

data class BloomPosition(
    val offset: Offset,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val opacity: Float
)

@Composable
fun CherryTreeScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val trunkAnim = remember { Animatable(0f) }
    val branchesAnim = remember { Animatable(0f) }
    val foliageAnim = remember { Animatable(0f) }
    val flowersAnim = remember { Animatable(0f) }

    val bloomPositions = remember {
        val random = Random(42)
        List(400) {
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val r = sqrt(random.nextFloat()) * 200f
            val x = (r * cos(angle)).toFloat()
            val y = (r * sin(angle)).toFloat()
            val size = 2f + random.nextFloat() * 6f
            val isDark = random.nextFloat() > 0.65f
            val isLight = random.nextFloat() < 0.25f
            val color = when {
                isDark -> DarkPinkColor
                isLight -> LightPinkColor
                else -> PinkColor
            }
            val rotation = random.nextFloat() * 360f
            val opacity = random.nextFloat() * 0.3f

            BloomPosition(offset = Offset(x, y), size = size, color = color, rotation = rotation, opacity = opacity)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "petals_fall")
    val fallingPetals = List(20) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3500 + i * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "petal_fall_$i"
        )
    }

    LaunchedEffect(repetir) {
        etapa = 0
        trunkAnim.snapTo(0f); branchesAnim.snapTo(0f); foliageAnim.snapTo(0f); flowersAnim.snapTo(0f)
        delay(400)
        etapa = 1; trunkAnim.animateTo(1f, tween(2200, easing = LinearOutSlowInEasing))
        etapa = 2; branchesAnim.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
        etapa = 3; foliageAnim.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))
        delay(300)
        etapa = 4; flowersAnim.animateTo(1f, tween(1800, easing = FastOutSlowInEasing))
        etapa = 5
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE8F4F8), BgSky, Color(0xFFFFF9E6)),
                startY = 0f, endY = Float.POSITIVE_INFINITY
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.85f
            val scale = size.width / 500f

            drawCherryTree(
                cx, cy, scale,
                trunkAnim.value, branchesAnim.value, foliageAnim.value, flowersAnim.value,
                bloomPositions
            )

            if (etapa >= 5) {
                fallingPetals.forEachIndexed { i, anim ->
                    val prog = anim.value
                    val startX = cx + ((i - 10) * 50f) * scale
                    val startY = cy - 450f * scale
                    val sway = sin(prog * 2 * PI.toFloat() + i) * 40f * scale
                    val currentX = startX + sway
                    val currentY = startY + prog * 600f * scale
                    if (currentY < cy + 50f * scale) {
                        val alpha = max(0f, 1f - (prog - 0.8f) * 5f)
                        drawOval(
                            color = PinkColor.copy(alpha = alpha * 0.8f),
                            topLeft = Offset(currentX - 5f * scale, currentY - 8f * scale),
                            size = Size(10f * scale, 16f * scale)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            val context = LocalContext.current
            val message = "🌸 ¡Hermoso Cerezo en Flor! 🌸"
            val subMessage = "La primavera siempre vuelve con belleza"
            val bgColor = BgSky

            DrawingButtons(
                visible = etapa >= 5,
                message = message,
                subMessage = subMessage,
                repeatEmoji = "🌸",
                accentColor = DarkPinkColor,
                backgroundColor = bgColor,
                onRepeat = { repetir++ },
                onBack = onBack,
                onSave = { includeText ->
                    saveCherryTreeAsImage(context, message, subMessage, bgColor, includeText, bloomPositions)
                }
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = DarkPinkColor)
        }
    }
}

private fun DrawScope.drawCherryTree(
    cx: Float, cy: Float, scale: Float,
    trunkP: Float, branchesP: Float, foliageP: Float, flowersP: Float,
    bloomPositions: List<BloomPosition>
) {
    drawCloud(cx - 150f * scale, 80f * scale, 80f * scale, 0.4f)
    drawCloud(cx + 180f * scale, 120f * scale, 60f * scale, 0.3f)

    drawOval(color = GrassColor, topLeft = Offset(cx - 220f * scale, cy - 20f * scale), size = Size(440f * scale, 80f * scale))
    drawOval(color = GrassDark, topLeft = Offset(cx - 220f * scale, cy + 20f * scale), size = Size(440f * scale, 40f * scale))
    drawOval(color = Color.Black.copy(alpha = 0.1f), topLeft = Offset(cx - 180f * scale, cy + 10f * scale), size = Size(360f * scale, 60f * scale))

    if (trunkP > 0f) {
        val trunkPath = Path().apply {
            moveTo(cx - 35f * scale, cy)
            quadraticTo(cx - 20f * scale, cy - 80f * scale * trunkP, cx - 25f * scale, cy - 250f * scale * trunkP)
            quadraticTo(cx - 15f * scale, cy - 380f * scale * trunkP, cx, cy - 420f * scale * trunkP)
            lineTo(cx + 20f * scale, cy - 420f * scale * trunkP)
            quadraticTo(cx + 15f * scale, cy - 380f * scale * trunkP, cx + 25f * scale, cy - 250f * scale * trunkP)
            quadraticTo(cx + 20f * scale, cy - 80f * scale * trunkP, cx + 35f * scale, cy)
            close()
        }
        drawPath(trunkPath, color = TrunkColor)
        drawPath(trunkPath, color = TrunkDark, style = Stroke(4f * scale))

        if (trunkP > 0.3f) {
            val barkP = (trunkP - 0.3f) / 0.7f
            val barkAlpha = min(barkP, 1f)
            drawLine(TrunkLight.copy(alpha = barkAlpha * 0.6f), Offset(cx - 8f * scale, cy - 60f * scale), Offset(cx - 8f * scale, cy - (60f + 150f * barkP) * scale), 3f * scale, cap = StrokeCap.Round)
            drawLine(TrunkDark.copy(alpha = barkAlpha * 0.7f), Offset(cx + 10f * scale, cy - 100f * scale), Offset(cx + 10f * scale, cy - (100f + 140f * barkP) * scale), 2.5f * scale, cap = StrokeCap.Round)
            drawLine(TrunkLight.copy(alpha = barkAlpha * 0.5f), Offset(cx - 2f * scale, cy - 180f * scale), Offset(cx - 2f * scale, cy - (180f + 120f * barkP) * scale), 2f * scale, cap = StrokeCap.Round)
        }
    }

    if (branchesP > 0f) {
        val branches = listOf(
            Triple(Offset(cx - 15f * scale, cy - 150f * scale), Offset(cx - 140f * scale * branchesP, cy - 320f * scale * branchesP), 10f * scale),
            Triple(Offset(cx + 15f * scale, cy - 150f * scale), Offset(cx + 150f * scale * branchesP, cy - 310f * scale * branchesP), 10f * scale),
            Triple(Offset(cx - 60f * scale * branchesP, cy - 250f * scale), Offset(cx - 130f * scale * branchesP, cy - 430f * scale * branchesP), 7f * scale),
            Triple(Offset(cx + 60f * scale * branchesP, cy - 250f * scale), Offset(cx + 140f * scale * branchesP, cy - 420f * scale * branchesP), 7f * scale),
            Triple(Offset(cx, cy - 350f * scale), Offset(cx - 50f * scale * branchesP, cy - 500f * scale * branchesP), 5f * scale),
            Triple(Offset(cx, cy - 350f * scale), Offset(cx + 60f * scale * branchesP, cy - 490f * scale * branchesP), 5f * scale),
            Triple(Offset(cx - 80f * scale * branchesP, cy - 380f * scale), Offset(cx - 140f * scale * branchesP, cy - 520f * scale * branchesP), 3f * scale),
            Triple(Offset(cx + 90f * scale * branchesP, cy - 370f * scale), Offset(cx + 150f * scale * branchesP, cy - 510f * scale * branchesP), 3f * scale)
        )
        branches.forEach { (start, end, width) ->
            drawLine(BranchColor, start, end, width * branchesP, cap = StrokeCap.Round, pathEffect = PathEffect.cornerPathEffect(2f * scale))
        }
    }

    if (foliageP > 0f) {
        val bloomCenterY = cy - 420f * scale
        val countToDraw = (bloomPositions.size * foliageP).toInt()
        for (i in 0 until countToDraw) {
            val bloom = bloomPositions[i]
            drawCircle(bloom.color.copy(alpha = bloom.color.alpha * (0.7f + bloom.opacity)), bloom.size * scale, Offset(cx + bloom.offset.x * scale, bloomCenterY + bloom.offset.y * scale))
        }
        if (foliageP > 0.5f) {
            val deepFP = (foliageP - 0.5f) * 2f
            val countDeep = (bloomPositions.size * 0.3f * deepFP).toInt()
            for (i in 0 until countDeep) {
                val bloom = bloomPositions[i]
                drawCircle(bloom.color.copy(alpha = 0.3f * deepFP), (bloom.size + 2f) * scale, Offset(cx + bloom.offset.x * scale, bloomCenterY + bloom.offset.y * scale))
            }
        }
    }

    if (flowersP > 0f) {
        val bloomCenterY = cy - 420f * scale
        val countToDraw = (bloomPositions.size * flowersP).toInt()
        for (i in 0 until countToDraw) {
            val bloom = bloomPositions[i]
            val petalSize = bloom.size * 1.5f * scale
            val centerX = cx + bloom.offset.x * scale
            val centerY = bloomCenterY + bloom.offset.y * scale
            repeat(5) { petal ->
                val angle = (petal * 72f + bloom.rotation) * (PI.toFloat() / 180f)
                drawCircle(bloom.color, petalSize * 0.6f, Offset(centerX + cos(angle) * petalSize * 1.2f, centerY + sin(angle) * petalSize * 1.2f))
            }
            drawCircle(Color(0xFFFFD700), petalSize * 0.4f, Offset(centerX, centerY))
        }
    }
}

private fun DrawScope.drawCloud(cx: Float, cy: Float, size: Float, alpha: Float) {
    val cloudColor = CloudColor.copy(alpha = alpha)
    drawCircle(cloudColor, size * 0.6f, Offset(cx - size * 0.3f, cy))
    drawCircle(cloudColor, size * 0.8f, Offset(cx, cy))
    drawCircle(cloudColor, size * 0.5f, Offset(cx + size * 0.4f, cy))
}

fun saveCherryTreeAsImage(
    context: android.content.Context,
    message: String,
    subMessage: String,
    bgColor: Color,
    includeText: Boolean,
    bloomPositions: List<BloomPosition>
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
            val cy = artSize * 0.85f
            val scale = artSize / 500f
            drawCherryTree(cx, cy, scale, 1f, 1f, 1f, 1f, bloomPositions)
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
