package com.example.trazoatrazo.ui.background

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun DynamicBackground(
    theme:    AppTheme,
    config:   BackgroundConfig,
    bgColor:  Color,
    modifier: Modifier = Modifier,
    eventOverride: SpecialEvent? = null,
    content:  @Composable () -> Unit
) {
    val particles = remember(theme, config.activeTypes) {
        generateParticles(theme = theme, activeTypes = config.activeTypes)
    }
    val stars     = remember { generateStars() }
    val grainPoints = remember { generateGrain() }
    val glows       = remember { defaultGlowsFor(theme) }

    // Partículas extra, exclusivas del evento activo — no se persisten,
    // no reemplazan las normales, solo se suman encima mientras dura.
    val eventParticles = remember(eventOverride?.id) {
        eventOverride?.let { event ->
            generateParticles(
                count       = 18,
                seed        = event.id.hashCode().toLong(),
                theme       = theme,
                activeTypes = listOf(event.particleType)
            )
        } ?: emptyList()
    }

    val pColor = remember(theme, config.particles.intensity, bgColor) {
        particleColor(theme, config.particles.intensity, bgColor)
    }
    val sColor = remember(theme, bgColor) { starColor(theme, bgColor) }

    val inf = rememberInfiniteTransition(label = "bg")

    val floatT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(
                durationMillis = (12000f / config.speed).toInt().coerceIn(4000, 40000),
                easing         = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatT"
    )

    val twinkleT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(
                durationMillis = (4000f / config.speed).toInt().coerceIn(1500, 12000),
                easing         = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkleT"
    )

    val glowT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(
                durationMillis = (6000f / config.speed).toInt().coerceIn(2000, 20000),
                easing         = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowT"
    )

    val grainT by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(
                durationMillis = 3000,
                easing         = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "grainT"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val W = size.width
                val H = size.height

                onDrawBehind {
                    drawRect(color = bgColor)

                    // Wash ambiental de evento (sutil, cubre toda la pantalla)
                    if (eventOverride != null) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    eventOverride.accentColor.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    // ── 2. Glow ──────────────────────────────
                    if (config.glow.enabled) {
                        glows.forEach { g ->
                            val cx     = g.xFrac * W
                            val cy     = g.yFrac * H
                            val radius = g.radiusFrac * W
                            val baseColor = glowColor(theme, glowT, g, config.glow.intensity, bgColor)
                            // Si hay evento activo, se mezcla el glow del tema con el acento del evento
                            val color = if (eventOverride != null) {
                                lerp(baseColor, eventOverride.accentColor.copy(alpha = baseColor.alpha), 0.4f)
                            } else baseColor
                            drawCircle(
                                brush = Brush.radialGradient(listOf(color, Color.Transparent), Offset(cx, cy), radius),
                                radius = radius,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // ── 3. Estrellas ──────────────────────────────────────────
                    if (config.stars.enabled) {
                        stars.forEach { s ->
                            val alpha = starAlpha(s, twinkleT, config.stars.intensity)
                            if (alpha > 0.01f) {
                                drawCircle(sColor.copy(alpha = alpha), s.radius, Offset(s.xFrac * W, s.yFrac * H))
                            }
                        }
                    }

                    // ── 4. Partículas (con Efectos Especiales) ──────────────────
                    if (config.particles.enabled) {
                        val kSymmetry = if (config.kaleidoscope.enabled) (2 + (config.kaleidoscope.intensity * 6).toInt()) else 1

                        particles.forEach { p ->
                            var (x, y) = particlePosition(p, floatT, W, H)

                            // ── Efecto Waves (Desplazamiento Ondulado)
                            if (config.waves.enabled) {
                                val waveAmp = 40f * config.waves.intensity
                                x += sin(y * 0.01f + floatT * 10f) * waveAmp
                                y += cos(x * 0.01f + floatT * 8f) * waveAmp
                            }

                            val alpha = p.baseAlpha * config.particles.intensity
                            if (alpha > 0.01f) {
                                val radius = p.radius * config.particleSize
                                val color  = pColor.copy(alpha = alpha)

                                // ── Renderizado con Simetría (Kaleidoscope) ──
                                for (i in 0 until kSymmetry) {
                                    val angle = (360f / kSymmetry) * i
                                    withTransform({
                                        if (kSymmetry > 1) rotate(angle, Offset(W / 2, H / 2))
                                    }) {
                                        val center = Offset(x, y)
                                        renderParticleForm(p, center, radius, color, alpha, floatT, config)
                                    }
                                }
                            }
                        }

                        // ── 4.5 Partículas exclusivas del evento activo ────────────
                        if (eventOverride != null && eventParticles.isNotEmpty()) {
                            val eventColor = eventOverride.accentColor
                            eventParticles.forEach { p ->
                                val (x, y) = particlePosition(p, floatT, W, H)
                                val alpha  = p.baseAlpha * 0.55f
                                if (alpha > 0.01f) {
                                    val radius = p.radius * config.particleSize
                                    val color  = eventColor.copy(alpha = alpha)
                                    renderParticleForm(p, Offset(x, y), radius, color, alpha, floatT, config)
                                }
                            }
                        }
                    }

                    // ── 5. Grain ──────────────────────────────────────────────
                    if (config.grain.enabled) {
                        grainPoints.forEach { g ->
                            val alpha = grainAlpha(g, grainT, config.grain.intensity)
                            if (alpha > 0.005f) {
                                drawCircle(Color.White.copy(alpha = alpha), g.radius, Offset(g.xFrac * W, g.yFrac * H))
                            }
                        }
                    }

                    // ── 6. Scanlines ───────────────────────────
                    if (config.scanlines.enabled) {
                        val gap = 8f
                        val intensity = config.scanlines.intensity * 0.3f
                        for (yPos in 0..(H.toInt()) step gap.toInt()) {
                            drawLine(Color.Black.copy(alpha = intensity), Offset(0f, yPos.toFloat()), Offset(W, yPos.toFloat()), 1f)
                        }
                    }

                    // ── 7. Viñeta ──────────────────────────
                    if (config.vignette.enabled) {
                        val vIntensity = config.vignette.intensity * 0.6f
                        drawRect(
                            brush = Brush.radialGradient(
                                0.0f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = vIntensity),
                                center = Offset(W / 2, H / 2),
                                radius = W.coerceAtLeast(H) * 0.8f
                            )
                        )
                    }
                }
            }
    ) {
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderParticleForm(
    p: ParticleData,
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float,
    time: Float,
    config: BackgroundConfig
) {
    val x = center.x
    val y = center.y

    when (p.type) {
        SpecialParticleType.SNOWFLAKE -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                for (arm in 0 until 3) {
                    withTransform({ rotate(arm * 60f, center) }) {
                        drawLine(color, Offset(x, y - radius * 1.6f), Offset(x, y + radius * 1.6f), radius * 0.35f)
                        drawLine(color, Offset(x - radius * 0.5f, y - radius * 0.8f), Offset(x + radius * 0.5f, y - radius * 0.8f), radius * 0.25f)
                    }
                }
            }
        }
        SpecialParticleType.SPARKLE -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                drawLine(color, Offset(x, y - radius * 1.8f), Offset(x, y + radius * 1.8f), radius * 0.3f)
                drawLine(color, Offset(x - radius * 1.8f, y), Offset(x + radius * 1.8f, y), radius * 0.3f)
            }
        }
        SpecialParticleType.LEAF -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                drawOval(color, Offset(x - radius * 0.5f, y - radius * 1.2f), androidx.compose.ui.geometry.Size(radius, radius * 2.4f))
            }
        }
        SpecialParticleType.SUN_RAY -> {
            val rot = particleRotation(p, time)
            drawCircle(color, radius * 0.7f, center)
            withTransform({ rotate(rot, center) }) {
                for (ray in 0 until 8) {
                    withTransform({ rotate(ray * 45f, center) }) {
                        drawLine(color.copy(alpha = alpha * 0.7f), Offset(x, y - radius * 1.0f), Offset(x, y - radius * 1.8f), radius * 0.25f)
                    }
                }
            }
        }
        SpecialParticleType.BUBBLE -> {
            drawCircle(color.copy(alpha = alpha * 0.3f), radius * 1.2f, center)
            drawCircle(color, radius * 1.2f, center, style = Stroke(width = radius * 0.25f))
        }
        SpecialParticleType.PETAL -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                drawOval(color, Offset(x - radius * 0.55f, y - radius * 1.1f), androidx.compose.ui.geometry.Size(radius * 1.1f, radius * 2.2f))
            }
        }
        SpecialParticleType.STAR_OUTLINE -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                val path = Path()
                val outerR = radius * 1.6f
                val innerR = radius * 0.65f
                for (i in 0 until 10) {
                    val angle = (i * PI.toFloat() / 5f) - PI.toFloat() / 2f
                    val r = if (i % 2 == 0) outerR else innerR
                    val px = x + r * kotlin.math.cos(angle)
                    val py = y + r * kotlin.math.sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path, color, style = Stroke(width = radius * 0.22f))
            }
        }
        SpecialParticleType.FIREFLY -> {
            drawCircle(color.copy(alpha = alpha * 0.15f), radius * 3.0f, center)
            drawCircle(color.copy(alpha = alpha * 0.30f), radius * 1.8f, center)
            drawCircle(color.copy(alpha = alpha * 0.90f), radius * 0.7f, center)
        }
        SpecialParticleType.HEART_S -> {
            val rot = particleRotation(p, time) * 0.4f
            withTransform({ rotate(rot, center) }) {
                val h = radius * 1.5f
                val path = Path().apply {
                    moveTo(x, y + h * 0.4f)
                    cubicTo(x - h, y - h * 0.6f, x - h * 0.5f, y - h * 1.2f, x, y - h * 0.4f)
                    cubicTo(x + h * 0.5f, y - h * 1.2f, x + h, y - h * 0.6f, x, y + h * 0.4f)
                }
                drawPath(path, color)
            }
        }
        SpecialParticleType.SQUARE_DOT -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                drawRect(color, Offset(x - radius * 0.8f, y - radius * 0.8f), androidx.compose.ui.geometry.Size(radius * 1.6f, radius * 1.6f))
            }
        }
        SpecialParticleType.WAVE -> {
            val rot = particleRotation(p, time)
            withTransform({ rotate(rot, center) }) {
                val path = Path().apply {
                    moveTo(x - radius * 1.5f, y)
                    cubicTo(x - radius * 0.5f, y - radius * 1.5f, x + radius * 0.5f, y + radius * 1.5f, x + radius * 1.5f, y)
                }
                drawPath(path, color, style = Stroke(width = radius * 0.35f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
        }
        else -> drawCircle(color = color, radius = radius, center = center)
    }

    if (config.chromatic.enabled) {
        val offset = 2f * config.chromatic.intensity
        drawCircle(Color.Red.copy(alpha = alpha * 0.3f), radius, Offset(x - offset, y))
        drawCircle(Color.Blue.copy(alpha = alpha * 0.3f), radius, Offset(x + offset, y))
    }
}

@Composable
fun DrawingBackground(
    theme:    AppTheme,
    bgColor:  Color,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit
) {
    val config = LocalBackgroundConfig.current
    DynamicBackground(theme, config, bgColor, modifier, content = content)  // ← ajustado: content ahora se pasa nombrado
}