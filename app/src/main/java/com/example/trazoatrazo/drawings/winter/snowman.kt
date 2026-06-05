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
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ── Paleta de Colores ─────────────────────────────────────────────────────────
private val SnowWhite = Color(0xFFFFFFFF)
private val SnowShadow = Color(0xFFE1F5FE)
private val CarrotOrange = Color(0xFFFF9800)
private val CoalBlack = Color(0xFF212121)
private val ScarfRed = Color(0xFFD32F2F)
private val ScarfDark = Color(0xFFB71C1C)
private val StickBrown = Color(0xFF5D4037)
private val SkyBlue = Color(0xFFB3E5FC)
private val GroundSnow = Color(0xFFF0F4F8)

@Composable
fun SnowmanScreen(onBack: () -> Unit) {
    var etapa by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val baseAnim = remember { Animatable(0f) }
    val middleAnim = remember { Animatable(0f) }
    val headAnim = remember { Animatable(0f) }
    val detailsAnim = remember { Animatable(0f) }

    // Animación de nieve cayendo
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val snowflakes = List(25) { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000 + i * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "snowflake_$i"
        )
    }

    LaunchedEffect(repetir) {
        etapa = 0
        baseAnim.snapTo(0f)
        middleAnim.snapTo(0f)
        headAnim.snapTo(0f)
        detailsAnim.snapTo(0f)
        delay(500)
        
        etapa = 1
        baseAnim.animateTo(1f, tween(1000, easing = EaseOutBack))
        
        etapa = 2
        middleAnim.animateTo(1f, tween(1000, easing = EaseOutBack))
        
        etapa = 3
        headAnim.animateTo(1f, tween(1000, easing = EaseOutBack))
        
        etapa = 4
        detailsAnim.animateTo(1f, tween(1500, easing = LinearOutSlowInEasing))
        
        etapa = 5
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SkyBlue)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val cx = size.width / 2f
                    val cy = size.height * 0.85f
                    val scale = size.width / 400f
                    
                    onDrawBehind {
                        // 1. Suelo nevado
                        drawRect(
                            color = GroundSnow,
                            topLeft = Offset(0f, cy - 20f * scale),
                            size = Size(size.width, size.height - cy + 20f * scale)
                        )

                        // 2. Base (Bola grande)
                        if (etapa >= 1) {
                            val p = baseAnim.value
                            val radius = 80f * scale * p
                            drawCircle(
                                color = SnowWhite,
                                radius = radius,
                                center = Offset(cx, cy - radius)
                            )
                            drawCircle(
                                color = SnowShadow,
                                radius = radius,
                                center = Offset(cx, cy - radius),
                                style = Stroke(2f * scale)
                            )
                        }

                        // 3. Cuerpo (Bola mediana)
                        if (etapa >= 2) {
                            val p = middleAnim.value
                            val baseRadius = 80f * scale
                            val radius = 60f * scale * p
                            val centerY = cy - baseRadius * 2 + 10f * scale - radius
                            drawCircle(
                                color = SnowWhite,
                                radius = radius,
                                center = Offset(cx, centerY)
                            )
                            drawCircle(
                                color = SnowShadow,
                                radius = radius,
                                center = Offset(cx, centerY),
                                style = Stroke(2f * scale)
                            )
                            
                            // Botones de carbón
                            if (etapa >= 4) {
                                val dP = detailsAnim.value
                                for (i in 0 until 3) {
                                    if (dP > i * 0.2f) {
                                        drawCircle(
                                            color = CoalBlack,
                                            radius = 4f * scale,
                                            center = Offset(cx, centerY - 20f * scale + i * 25f * scale)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Cabeza (Bola pequeña)
                        if (etapa >= 3) {
                            val p = headAnim.value
                            val baseRadius = 80f * scale
                            val midRadius = 60f * scale
                            val radius = 45f * scale * p
                            val centerY = cy - baseRadius * 2 - midRadius * 2 + 25f * scale - radius
                            
                            drawCircle(
                                color = SnowWhite,
                                radius = radius,
                                center = Offset(cx, centerY)
                            )
                            drawCircle(
                                color = SnowShadow,
                                radius = radius,
                                center = Offset(cx, centerY),
                                style = Stroke(2f * scale)
                            )

                            // Rostro
                            if (etapa >= 4) {
                                val dP = detailsAnim.value
                                // Ojos
                                if (dP > 0.1f) {
                                    drawCircle(CoalBlack, 4f * scale, Offset(cx - 15f * scale, centerY - 10f * scale))
                                    drawCircle(CoalBlack, 4f * scale, Offset(cx + 15f * scale, centerY - 10f * scale))
                                }
                                // Nariz (Zanahoria)
                                if (dP > 0.3f) {
                                    val carrotPath = Path().apply {
                                        moveTo(cx, centerY)
                                        lineTo(cx + 35f * scale * dP, centerY + 5f * scale)
                                        lineTo(cx, centerY + 10f * scale)
                                        close()
                                    }
                                    drawPath(carrotPath, color = CarrotOrange)
                                }
                                // Sonrisa (Puntos)
                                if (dP > 0.5f) {
                                    for (i in -2..2) {
                                        val angle = i * 20f
                                        val rad = Math.toRadians(angle.toDouble())
                                        drawCircle(
                                            color = CoalBlack,
                                            radius = 2.5f * scale,
                                            center = Offset(
                                                cx + (sin(rad) * 25f * scale).toFloat(),
                                                centerY + (cos(rad) * 25f * scale).toFloat()
                                            )
                                        )
                                    }
                                }
                                
                                // Sombrero
                                if (dP > 0.7f) {
                                    val hatP = (dP - 0.7f) * 3.3f
                                    val hatWidth = 60f * scale
                                    val hatHeight = 45f * scale
                                    // Ala
                                    drawRoundRect(
                                        color = CoalBlack,
                                        topLeft = Offset(cx - hatWidth, centerY - radius - 5f * scale),
                                        size = Size(hatWidth * 2, 10f * scale),
                                        cornerRadius = CornerRadius(5f, 5f)
                                    )
                                    // Copa
                                    drawRect(
                                        color = CoalBlack,
                                        topLeft = Offset(cx - hatWidth * 0.6f, centerY - radius - hatHeight * hatP - 5f * scale),
                                        size = Size(hatWidth * 1.2f, hatHeight * hatP)
                                    )
                                    // Cinta roja
                                    if (hatP > 0.8f) {
                                        drawRect(
                                            color = ScarfRed,
                                            topLeft = Offset(cx - hatWidth * 0.6f, centerY - radius - 15f * scale),
                                            size = Size(hatWidth * 1.2f, 8f * scale)
                                        )
                                    }
                                }
                            }
                        }

                        // 5. Brazos y Bufanda
                        if (etapa >= 4) {
                            val dP = detailsAnim.value
                            val baseRadius = 80f * scale
                            val midRadius = 60f * scale
                            val midY = cy - baseRadius * 2 + 10f * scale - midRadius

                            // Brazos (Palitos)
                            if (dP > 0.2f) {
                                val armP = (dP - 0.2f) * 1.25f
                                // Izquierdo
                                drawLine(
                                    color = StickBrown,
                                    start = Offset(cx - 55f * scale, midY),
                                    end = Offset(cx - 130f * scale * armP, midY - 40f * scale * armP),
                                    strokeWidth = 6f * scale,
                                    cap = StrokeCap.Round
                                )
                                // Derecho
                                drawLine(
                                    color = StickBrown,
                                    start = Offset(cx + 55f * scale, midY),
                                    end = Offset(cx + 130f * scale * armP, midY - 30f * scale * armP),
                                    strokeWidth = 6f * scale,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Bufanda
                            if (dP > 0.4f) {
                                val scarfP = (dP - 0.4f) * 1.6f
                                val neckY = cy - baseRadius * 2 + 15f * scale
                                // Parte del cuello
                                drawRoundRect(
                                    color = ScarfRed,
                                    topLeft = Offset(cx - 55f * scale * scarfP, neckY - 10f * scale),
                                    size = Size(110f * scale * scarfP, 20f * scale),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
                                // Colgante de la bufanda
                                if (scarfP > 0.8f) {
                                    drawRect(
                                        color = ScarfDark,
                                        topLeft = Offset(cx + 20f * scale, neckY),
                                        size = Size(25f * scale, 60f * scale * (scarfP - 0.8f) * 5f)
                                    )
                                }
                            }
                        }

                        // 6. Nieve cayendo
                        snowflakes.forEachIndexed { i, anim ->
                            val prog = anim.value
                            val startX = (size.width / snowflakes.size) * i + (sin(prog * PI.toFloat()) * 20f)
                            val currentY = prog * size.height
                            
                            drawCircle(
                                color = SnowWhite.copy(alpha = 0.8f),
                                radius = 3f * scale,
                                center = Offset(startX, currentY)
                            )
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
                visible = etapa >= 5,
                message = "☃️ ¡Un Muñeco de Nieve! ☃️",
                subMessage = "Abrígate bien que hace frío",
                repeatEmoji = "❄️",
                accentColor = Color(0xFF0288D1),
                onRepeat = { repetir++ },
                onBack = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = Color(0xFF0288D1))
        }
    }
}
