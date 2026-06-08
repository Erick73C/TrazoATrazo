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
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlin.math.*
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.repeat

private val PinkColor = Color(0xFFFFB7C5)
private val LightPinkColor = Color(0xFFFCE4EC)
private val DarkPinkColor = Color(0xFFD81B60)
private val VeryLightPinkColor = Color(0xFFFFF0F5)
private val TrunkColor = Color(0xFF5D4037)
private val TrunkDark = Color(0xFF3E2723)
private val TrunkLight = Color(0xFF795548)
private val BranchColor = Color(0xFF6D4C41)
private val GrassColor = Color(0xFF7CB342)
private val GrassDark = Color(0xFF558B2F)
private val BgSky = Color(0xFFE3F2FD)
private val CloudColor = Color(0xFFFFFFFF)

// Data class para guardar información de flores
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

    val trunkPath = remember { Path() }

    // Generar posiciones de flores fijas con mejor distribución
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

            BloomPosition(
                offset = Offset(x, y),
                size = size,
                color = color,
                rotation = rotation,
                opacity = opacity
            )
        }
    }

    // Pétalos cayendo con rotación
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
        trunkAnim.snapTo(0f)
        branchesAnim.snapTo(0f)
        foliageAnim.snapTo(0f)
        flowersAnim.snapTo(0f)
        delay(400)

        etapa = 1
        trunkAnim.animateTo(1f, tween(2200, easing = LinearOutSlowInEasing))

        etapa = 2
        branchesAnim.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))

        etapa = 3
        foliageAnim.animateTo(1f, tween(2500, easing = FastOutSlowInEasing))

        delay(300)
        etapa = 4
        flowersAnim.animateTo(1f, tween(1800, easing = FastOutSlowInEasing))

        etapa = 5
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F4F8),
                        BgSky,
                        Color(0xFFFFF9E6)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height * 0.85f
                    val scale = size.width / 500f

                    onDrawBehind {
                        // 🌤️ Nubes decorativas sutiles
                        drawCloud(cx - 150f * scale, 80f * scale, 80f * scale, 0.4f)
                        drawCloud(cx + 180f * scale, 120f * scale, 60f * scale, 0.3f)

                        // 🌱 Pasto/Suelo con degradado
                        drawOval(
                            color = GrassColor,
                            topLeft = Offset(cx - 220f * scale, cy - 20f * scale),
                            size = Size(440f * scale, 80f * scale)
                        )
                        drawOval(
                            color = GrassDark,
                            topLeft = Offset(cx - 220f * scale, cy + 20f * scale),
                            size = Size(440f * scale, 40f * scale)
                        )

                        // Sombra del árbol
                        drawOval(
                            color = Color.Black.copy(alpha = 0.1f),
                            topLeft = Offset(cx - 180f * scale, cy + 10f * scale),
                            size = Size(360f * scale, 60f * scale)
                        )

                        if (etapa >= 1) {
                            val tP = trunkAnim.value

                            // 🌳 Tronco principal (Más realista)
                            trunkPath.reset()

                            // Base más ancha
                            trunkPath.moveTo(cx - 35f * scale, cy)
                            trunkPath.quadraticTo(
                                cx - 20f * scale,
                                cy - 80f * scale * tP,
                                cx - 25f * scale,
                                cy - 250f * scale * tP
                            )
                            trunkPath.quadraticTo(
                                cx - 15f * scale,
                                cy - 380f * scale * tP,
                                cx,
                                cy - 420f * scale * tP
                            )
                            trunkPath.lineTo(cx + 20f * scale, cy - 420f * scale * tP)
                            trunkPath.quadraticTo(
                                cx + 15f * scale,
                                cy - 380f * scale * tP,
                                cx + 25f * scale,
                                cy - 250f * scale * tP
                            )
                            trunkPath.quadraticTo(
                                cx + 20f * scale,
                                cy - 80f * scale * tP,
                                cx + 35f * scale,
                                cy
                            )
                            trunkPath.close()

                            // Tronco principal
                            drawPath(trunkPath, color = TrunkColor)

                            // Sombra del tronco (lado izquierdo)
                            drawPath(
                                trunkPath,
                                color = TrunkDark,
                                style = Stroke(4f * scale)
                            )

                            // 🪵 Corteza con detalle
                            if (tP > 0.3f) {
                                val barkP = (tP - 0.3f) / 0.7f
                                val barkAlpha = min(barkP, 1f)

                                // Líneas de corteza principales
                                drawLine(
                                    TrunkLight.copy(alpha = barkAlpha * 0.6f),
                                    Offset(cx - 8f * scale, cy - 60f * scale),
                                    Offset(cx - 8f * scale, cy - (60f + 150f * barkP) * scale),
                                    strokeWidth = 3f * scale,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    TrunkDark.copy(alpha = barkAlpha * 0.7f),
                                    Offset(cx + 10f * scale, cy - 100f * scale),
                                    Offset(cx + 10f * scale, cy - (100f + 140f * barkP) * scale),
                                    strokeWidth = 2.5f * scale,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    TrunkLight.copy(alpha = barkAlpha * 0.5f),
                                    Offset(cx - 2f * scale, cy - 180f * scale),
                                    Offset(cx - 2f * scale, cy - (180f + 120f * barkP) * scale),
                                    strokeWidth = 2f * scale,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        if (etapa >= 2) {
                            val brP = branchesAnim.value

                            // 🌿 Ramas principales y secundarias
                            val branches = listOf(
                                // Nivel 1 - Ramas gruesas principales
                                Triple(
                                    Offset(cx - 15f * scale, cy - 150f * scale),
                                    Offset(cx - 140f * scale * brP, cy - 320f * scale * brP),
                                    10f * scale
                                ),
                                Triple(
                                    Offset(cx + 15f * scale, cy - 150f * scale),
                                    Offset(cx + 150f * scale * brP, cy - 310f * scale * brP),
                                    10f * scale
                                ),
                                // Nivel 2 - Ramas medianas
                                Triple(
                                    Offset(cx - 60f * scale * brP, cy - 250f * scale),
                                    Offset(cx - 130f * scale * brP, cy - 430f * scale * brP),
                                    7f * scale
                                ),
                                Triple(
                                    Offset(cx + 60f * scale * brP, cy - 250f * scale),
                                    Offset(cx + 140f * scale * brP, cy - 420f * scale * brP),
                                    7f * scale
                                ),
                                // Nivel 3 - Ramas finas
                                Triple(
                                    Offset(cx, cy - 350f * scale),
                                    Offset(cx - 50f * scale * brP, cy - 500f * scale * brP),
                                    5f * scale
                                ),
                                Triple(
                                    Offset(cx, cy - 350f * scale),
                                    Offset(cx + 60f * scale * brP, cy - 490f * scale * brP),
                                    5f * scale
                                ),
                                // Nivel 4 - Ramas muy finas (puntas)
                                Triple(
                                    Offset(cx - 80f * scale * brP, cy - 380f * scale),
                                    Offset(cx - 140f * scale * brP, cy - 520f * scale * brP),
                                    3f * scale
                                ),
                                Triple(
                                    Offset(cx + 90f * scale * brP, cy - 370f * scale),
                                    Offset(cx + 150f * scale * brP, cy - 510f * scale * brP),
                                    3f * scale
                                )
                            )

                            branches.forEach { (start, end, width) ->
                                drawLine(
                                    color = BranchColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = width * brP,
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.cornerPathEffect(2f * scale)
                                )
                            }
                        }

                        if (etapa >= 3) {
                            val fP = foliageAnim.value
                            val bloomCenterY = cy - 420f * scale

                            // 🍃 Follaje en capas
                            val countToDraw = (bloomPositions.size * fP).toInt()

                            for (i in 0 until countToDraw) {
                                val bloom = bloomPositions[i]
                                val actualColor = bloom.color.copy(alpha = bloom.color.alpha * (0.7f + bloom.opacity))

                                // Círculos de follaje
                                drawCircle(
                                    color = actualColor,
                                    radius = bloom.size * scale,
                                    center = Offset(cx + bloom.offset.x * scale, bloomCenterY + bloom.offset.y * scale)
                                )
                            }

                            // Capas adicionales de follaje más claro para profundidad
                            if (fP > 0.5f) {
                                val deepFP = (fP - 0.5f) * 2f
                                val countDeep = (bloomPositions.size * 0.3f * deepFP).toInt()

                                for (i in 0 until countDeep) {
                                    val bloom = bloomPositions[i]
                                    drawCircle(
                                        color = bloom.color.copy(alpha = 0.3f * deepFP),
                                        radius = (bloom.size + 2f) * scale,
                                        center = Offset(cx + bloom.offset.x * scale, bloomCenterY + bloom.offset.y * scale)
                                    )
                                }
                            }
                        }

                        if (etapa >= 4) {
                            val flP = flowersAnim.value
                            val bloomCenterY = cy - 420f * scale
                            val countToDraw = (bloomPositions.size * flP).toInt()

                            // 🌸 Flores de cerezo (5 pétalos)
                            for (i in 0 until countToDraw) {
                                val bloom = bloomPositions[i]

                                // Dibujar flor de 5 pétalos
                                val petalSize = bloom.size * 1.5f * scale
                                val centerX = cx + bloom.offset.x * scale
                                val centerY = bloomCenterY + bloom.offset.y * scale

                                repeat(5) { petal ->
                                    val angle = (petal * 72f + bloom.rotation) * (PI.toFloat() / 180f)
                                    val petalX = centerX + cos(angle) * petalSize * 1.2f
                                    val petalY = centerY + sin(angle) * petalSize * 1.2f

                                    // Cada pétalo es un círculo pequeño
                                    drawCircle(
                                        color = bloom.color,
                                        radius = petalSize * 0.6f,
                                        center = Offset(petalX, petalY)
                                    )
                                }

                                // Centro de la flor (amarillo)
                                drawCircle(
                                    color = Color(0xFFFFD700),
                                    radius = petalSize * 0.4f,
                                    center = Offset(centerX, centerY)
                                )
                            }
                        }

                        if (etapa >= 5) {
                            // 🌺 Pétalos cayendo con rotación
                            fallingPetals.forEachIndexed { i, anim ->
                                val prog = anim.value
                                val startX = cx + ((i - 10) * 50f) * scale
                                val startY = cy - 450f * scale
                                val sway = sin(prog * 2 * PI.toFloat() + i) * 40f * scale
                                val currentX = startX + sway
                                val currentY = startY + prog * 600f * scale

                                val rotation = (prog * 360f * (i % 2 + 1)) % 360f

                                if (currentY < cy + 50f * scale) {
                                    val alpha = max(0f, 1f - (prog - 0.8f) * 5f)

                                    // Dibujar pétalo cayendo (rosa)
                                    drawOval(
                                        color = PinkColor.copy(alpha = alpha * 0.8f),
                                        topLeft = Offset(currentX - 5f * scale, currentY - 8f * scale),
                                        size = Size(10f * scale, 16f * scale)
                                    )
                                }
                            }
                        }
                    }
                }
        ) {}

        // Botones de control
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = etapa >= 5,
                message = "🌸 ¡Hermoso Cerezo en Flor! 🌸",
                subMessage = "La primavera siempre vuelve con belleza",
                repeatEmoji = "🌸",
                accentColor = DarkPinkColor,
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = DarkPinkColor)
        }
    }
}


// Helper function para dibujar nubes
private fun DrawScope.drawCloud(cx: Float, cy: Float, size: Float, alpha: Float) {
    val cloudColor = CloudColor.copy(alpha = alpha)
    drawCircle(cloudColor, size * 0.6f, Offset(cx - size * 0.3f, cy))
    drawCircle(cloudColor, size * 0.8f, Offset(cx, cy))
    drawCircle(cloudColor, size * 0.5f, Offset(cx + size * 0.4f, cy))
}