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
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ── Paleta de Colores ─────────────────────────────────────────────────────────
private val PinkColor = Color(0xFFFFB7C5)
private val LightPinkColor = Color(0xFFFCE4EC)
private val DarkPinkColor = Color(0xFFFF91A4)
private val TrunkColor = Color(0xFF4E342E)
private val TrunkDark = Color(0xFF3E2723)
private val GrassColor = Color(0xFF81C784)
private val BgSky = Color(0xFFE1F5FE) // Un azul cielo muy claro

@Composable
fun CherryTreeScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val trunkAnim = remember { Animatable(0f) }
    val foliageAnim = remember { Animatable(0f) }

    val trunkPath = remember { Path() }
    
    // Generar posiciones de flores fijas
    val bloomPositions = remember {
        val random = Random(42)
        List(350) { // Aumentado a 250 flores
            val angle = random.nextFloat() * 2 * PI
            val r = sqrt(random.nextFloat()) * 180f // Radio aumentado
            val x = (r * cos(angle)).toFloat()
            val y = (r * sin(angle)).toFloat()
            val size = 3f + random.nextFloat() * 7f
            val isDark = random.nextFloat() > 0.7f
            val isLight = random.nextFloat() < 0.2f
            val color = when {
                isDark -> DarkPinkColor
                isLight -> LightPinkColor
                else -> PinkColor
            }
            Triple(Offset(x, y), size, color)
        }
    }

    // Animación de pétalos cayendo
    val infiniteTransition = rememberInfiniteTransition(label = "petals")
    val fallingPetals = List(15) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000 + i * 200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "petal_$i"
        )
    }

    LaunchedEffect(repetir) {
        etapa = 0
        trunkAnim.snapTo(0f)
        foliageAnim.snapTo(0f)
        delay(500)
        
        etapa = 1
        trunkAnim.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing))
        
        etapa = 2
        foliageAnim.animateTo(1f, tween(3000, easing = FastOutSlowInEasing))
        
        etapa = 3
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgSky)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height * 0.9f // Más abajo para que sea más alto
                    val scale = size.width / 450f
                    
                    onDrawBehind {
                        // 1. Dibujar Pasto/Suelo
                        drawOval(
                            color = GrassColor,
                            topLeft = Offset(cx - 200f * scale, cy - 30f * scale),
                            size = Size(400f * scale, 100f * scale)
                        )

                        if (etapa >= 1) {
                            val p = trunkAnim.value
                            
                            // 2. Dibujar Tronco (Más alto y con detalle)
                            trunkPath.reset()
                            trunkPath.moveTo(cx - 25f * scale, cy)
                            // Curvas para un tronco más orgánico
                            trunkPath.quadraticTo(cx - 10f * scale, cy - 150f * scale * p, cx - 15f * scale, cy - 350f * scale * p)
                            trunkPath.lineTo(cx + 15f * scale, cy - 350f * scale * p)
                            trunkPath.quadraticTo(cx + 10f * scale, cy - 150f * scale * p, cx + 25f * scale, cy)
                            trunkPath.close()
                            
                            drawPath(trunkPath, color = TrunkColor)
                            
                            // Detalles de "corteza"
                            if (p > 0.5f) {
                                val barkP = (p - 0.5f) * 2f
                                drawLine(TrunkDark, Offset(cx - 5f * scale, cy - 50f * scale), Offset(cx - 5f * scale, cy - (50f + 100f * barkP) * scale), strokeWidth = 2f * scale)
                                drawLine(TrunkDark, Offset(cx + 8f * scale, cy - 120f * scale), Offset(cx + 8f * scale, cy - (120f + 80f * barkP) * scale), strokeWidth = 2f * scale)
                            }

                            // 3. Ramas (Más niveles y detalles)
                            val branches = listOf(
                                // Nivel 1
                                Triple(Offset(cx, cy - 180f * scale * p), Offset(cx - 100f * scale * p, cy - 320f * scale * p), 12f),
                                Triple(Offset(cx, cy - 220f * scale * p), Offset(cx + 110f * scale * p, cy - 300f * scale * p), 10f),
                                // Nivel 2
                                Triple(Offset(cx - 40f * scale * p, cy - 260f * scale * p), Offset(cx - 120f * scale * p, cy - 420f * scale * p), 7f),
                                Triple(Offset(cx + 50f * scale * p, cy - 240f * scale * p), Offset(cx + 130f * scale * p, cy - 400f * scale * p), 7f),
                                // Nivel 3 (Puntas)
                                Triple(Offset(cx, cy - 350f * scale * p), Offset(cx - 30f * scale * p, cy - 480f * scale * p), 6f),
                                Triple(Offset(cx, cy - 350f * scale * p), Offset(cx + 40f * scale * p, cy - 460f * scale * p), 6f)
                            )
                            
                            branches.forEach { (start, end, width) ->
                                drawLine(
                                    color = TrunkColor,
                                    start = start,
                                    end = end,
                                    strokeWidth = width * scale * p,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        
                        if (etapa >= 2) {
                            val fP = foliageAnim.value
                            val bloomCenterY = cy - 400f * scale // Movido hacia arriba
                            
                            // 4. Dibujar follaje
                            val countToDraw = (bloomPositions.size * fP).toInt()
                            for (i in 0 until countToDraw) {
                                val (offset, sizeVal, color) = bloomPositions[i]
                                drawCircle(
                                    color = color,
                                    radius = sizeVal * scale,
                                    center = Offset(cx + offset.x * scale, bloomCenterY + offset.y * scale)
                                )
                                // Pequeño borde para detalle
                                drawCircle(
                                    color = color.copy(alpha = 0.3f),
                                    radius = (sizeVal + 1f) * scale,
                                    center = Offset(cx + offset.x * scale, bloomCenterY + offset.y * scale),
                                    style = Stroke(1f * scale)
                                )
                            }

                            // 5. Pétalos cayendo (Solo cuando el árbol está florecido)
                            if (etapa >= 3) {
                                fallingPetals.forEachIndexed { i, anim ->
                                    val prog = anim.value
                                    val startX = cx + ((i - 7) * 40f) * scale
                                    val startY = cy - 450f * scale
                                    val currentX = startX + sin(prog * 2 * PI.toFloat() + i) * 30f * scale
                                    val currentY = startY + prog * 500f * scale
                                    
                                    if (currentY < cy + 20f * scale) {
                                        drawCircle(
                                            color = PinkColor.copy(alpha = 1f - prog),
                                            radius = 4f * scale,
                                            center = Offset(currentX, currentY)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        ) {}

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible = etapa >= 3,
                message = "🌸 ¡Un hermoso Cerezo en flor! 🌸",
                subMessage = "La primavera siempre vuelve",
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
