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
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ── Colores ───────────────────────────────────────────────────────────────────
private val IceWhite   = Color(0xFFFFFFFF)
private val IceBlue    = Color(0xFFB3E5FC)
private val IceCyan    = Color(0xFF80DEEA)
private val IceDeep    = Color(0xFF4FC3F7)
private val BgDark     = Color(0xFF020912)
private val BgMid      = Color(0xFF071428)
private val AuroraTeal = Color(0xFF1DE9B6)
private val AuroraBlue = Color(0xFF40C4FF)

// ── Modelo de copo cayendo ────────────────────────────────────────────────────
private data class FallingFlake(
    val xFrac     : Float,   // posición X (0..1 fracción del ancho)
    val speed     : Float,   // velocidad de caída
    val radiusFrac: Float,   // radio como fracción de min(w,h)
    val rotSpeed  : Float,   // rotaciones por ciclo completo
    val delay     : Float,   // desfase de fase (0..1)
    val driftW    : Float,   // amplitud del vaivén lateral (fracción del ancho)
    val driftF    : Float,   // frecuencia del vaivén
    val baseAlpha : Float    // opacidad base (efecto de profundidad)
)

// ── Copos generados una sola vez ──────────────────────────────────────────────
private val fallingFlakes: List<FallingFlake> = run {
    val rng = Random(42)
    val raw = List(32) {
        val rf = 0.022f + rng.nextFloat() * 0.095f
        // Copos más grandes son más lentos y semitransparentes (fondo)
        // Copos más pequeños son rápidos y opacos (primer plano)
        val depthAlpha = 0.45f + (1f - (rf - 0.022f) / 0.095f) * 0.55f
        FallingFlake(
            xFrac      = rng.nextFloat(),
            speed      = 0.35f + rng.nextFloat() * 1.5f,
            radiusFrac = rf,
            rotSpeed   = 0.25f + rng.nextFloat() * 1.8f,
            delay      = rng.nextFloat(),
            driftW     = 0.015f + rng.nextFloat() * 0.055f,
            driftF     = 0.6f   + rng.nextFloat() * 1.6f,
            baseAlpha  = depthAlpha
        )
    }
    // Grandes primero → se dibujan detrás; pequeños encima
    raw.sortedByDescending { it.radiusFrac }
}

// ── Estrellas de fondo ────────────────────────────────────────────────────────
private data class StarPoint(val xFrac: Float, val yFrac: Float, val size: Float, val phase: Float)
private val starPoints: List<StarPoint> = run {
    val rng = Random(13)
    List(65) {
        StarPoint(rng.nextFloat(), rng.nextFloat() * 0.75f, 0.4f + rng.nextFloat() * 1.6f, rng.nextFloat())
    }
}

// ── Composable ────────────────────────────────────────────────────────────────
@Composable
fun SnowflakeScreen(onBack: () -> Unit) {
    var etapa   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    val enterAnim = remember { Animatable(0f) }

    val inf = rememberInfiniteTransition(label = "snow_inf")

    // Temporizador maestro de caída (0→1 en 7 segundos)
    val fallT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fall"
    )

    // Parpadeo de estrellas
    val twinkleT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle"
    )

    // Pulso de la aurora
    val glowPulse by inf.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(repetir) {
        etapa = 0
        enterAnim.snapTo(0f)
        delay(300)

        etapa = 1
        // Los copos aparecen fundidos desde arriba
        enterAnim.animateTo(1f, tween(1800, easing = LinearOutSlowInEasing))

        etapa = 2  // Cascade activa, botones visibles
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgDark, BgMid, BgDark))
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val W  = size.width
                    val H  = size.height
                    val cx = W / 2f

                    onDrawBehind {

                        // ── Aurora boreal ──────────────────────────────────────
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AuroraTeal.copy(alpha = 0.08f * glowPulse),
                                    AuroraBlue.copy(alpha = 0.05f * glowPulse),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY   = H * 0.55f
                            ),
                            topLeft = Offset.Zero,
                            size    = size
                        )

                        // ── Estrellas parpadeantes ─────────────────────────────
                        starPoints.forEach { star ->
                            val twinkle = (sin((twinkleT + star.phase) * 2f * PI.toFloat()) + 1f) / 2f
                            drawCircle(
                                color  = IceWhite.copy(alpha = 0.20f + 0.80f * twinkle),
                                radius = star.size,
                                center = Offset(star.xFrac * W, star.yFrac * H)
                            )
                        }

                        // ── Resplandor suave en el centro-superior ─────────────
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    IceDeep.copy(alpha = 0.06f * glowPulse),
                                    Color.Transparent
                                ),
                                center = Offset(cx, H * 0.25f),
                                radius = W * 0.7f
                            ),
                            radius = W * 0.7f,
                            center = Offset(cx, H * 0.25f)
                        )

                        // ── Copos cayendo en cascada ───────────────────────────
                        val ep = enterAnim.value

                        fallingFlakes.forEach { flake ->
                            // Progreso individual de cada copo en su ciclo
                            val p = (fallT * flake.speed + flake.delay) % 1f

                            // Posición Y: de -radio a alto+radio
                            val r  = flake.radiusFrac * min(W, H)
                            val yRaw = p * (H + r * 2.5f) - r * 1.2f

                            // Vaivén lateral suave
                            val drift = flake.driftW * W
                            val x = flake.xFrac * W +
                                    sin(p * PI.toFloat() * 2f * flake.driftF) * drift

                            // Alpha: aparece al entrar, desaparece al salir de pantalla
                            val edgeFade = when {
                                p < 0.08f -> p / 0.08f
                                p > 0.88f -> (1f - p) / 0.12f
                                else -> 1f
                            }
                            val alpha = (flake.baseAlpha * edgeFade * ep).coerceIn(0f, 1f)

                            // Ángulo de rotación
                            val angleDeg = p * 360f * flake.rotSpeed

                            if (alpha > 0.01f) {
                                drawSnowCrystal(x, yRaw, r, angleDeg, alpha)
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
                visible     = etapa >= 2,
                message     = "❄️ ¡Nevada en cascada! ❄️",
                subMessage  = "Cada copo es único",
                repeatEmoji = "❄️",
                accentColor = IceDeep,
                onRepeat    = { repetir++ },
                onBack      = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = IceBlue)
        }
    }
}

// ── Dibujo de un copo de nieve pequeño ───────────────────────────────────────
// Sin Path — solo drawRoundRect y drawCircle, ambos rápidos y sin ambigüedad.
private fun DrawScope.drawSnowCrystal(
    cx: Float, cy: Float,
    r: Float, angleDeg: Float, alpha: Float
) {
    val armW  = (r * 0.18f).coerceAtLeast(1.2f)
    val cr    = CornerRadius(armW / 2f)
    val colB  = IceBlue.copy(alpha  = alpha)
    val colC  = IceCyan.copy(alpha  = alpha * 0.85f)
    val colW  = IceWhite.copy(alpha = alpha * 0.90f)

    withTransform({
        translate(cx, cy)
        rotate(angleDeg, Offset.Zero)
    }) {

        // 6 brazos del copo
        repeat(6) { i ->
            withTransform({ rotate(i.toFloat() * 60f, Offset.Zero) }) {

                // Brazo principal (de centro hacia arriba)
                drawRoundRect(
                    color        = colB,
                    topLeft      = Offset(-armW / 2f, -r),
                    size         = Size(armW, r),
                    cornerRadius = cr
                )

                // Sub-brazos a 62% del brazo (±45°)
                val subPos = -r * 0.62f
                val subLen = r * 0.32f
                val subW   = (armW * 0.72f).coerceAtLeast(0.8f)
                val subCr  = CornerRadius(subW / 2f)

                withTransform({ translate(0f, subPos) }) {
                    withTransform({ rotate(45f, Offset.Zero) }) {
                        drawRoundRect(colC, Offset(-subW / 2f, -subLen), Size(subW, subLen), subCr)
                    }
                    withTransform({ rotate(-45f, Offset.Zero) }) {
                        drawRoundRect(colC, Offset(-subW / 2f, -subLen), Size(subW, subLen), subCr)
                    }
                }

                // Sub-brazos interiores a 35% del brazo (±55°) — solo si es grande
                if (r > 12f) {
                    val sub2Pos = -r * 0.35f
                    val sub2Len = r * 0.20f
                    val sub2W   = (armW * 0.55f).coerceAtLeast(0.8f)
                    val sub2Cr  = CornerRadius(sub2W / 2f)
                    withTransform({ translate(0f, sub2Pos) }) {
                        withTransform({ rotate(55f, Offset.Zero) }) {
                            drawRoundRect(colC, Offset(-sub2W / 2f, -sub2Len), Size(sub2W, sub2Len), sub2Cr)
                        }
                        withTransform({ rotate(-55f, Offset.Zero) }) {
                            drawRoundRect(colC, Offset(-sub2W / 2f, -sub2Len), Size(sub2W, sub2Len), sub2Cr)
                        }
                    }
                }

                // Punto luminoso en la punta del brazo
                drawCircle(colW, (armW * 0.9f).coerceAtLeast(1f), Offset(0f, -r))
            }
        }

        // Centro del copo
        drawCircle(colW, (r * 0.14f).coerceAtLeast(1.5f), Offset.Zero)
    }
}