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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlin.math.PI

// ══════════════════════════════════════════════════════════════════════════════
// DynamicBackground — Composable principal
// Envuelve cualquier pantalla con la capa de efectos de fondo
//
// Uso:
//   DynamicBackground(
//       theme  = currentTheme,
//       config = LocalBackgroundConfig.current,
//       bgColor = AppColors.Vacio
//   ) {
//       // contenido de la pantalla
//   }
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun DynamicBackground(
    theme:    AppTheme,
    config:   BackgroundConfig,
    bgColor:  Color,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit
) {
    // ── Datos generados una sola vez ──────────────────────────────────────────
    val particles = remember(theme, config.activeTypes) { 
        generateParticles(theme = theme, activeTypes = config.activeTypes)
    }
    val stars     = remember { generateStars() }
    val petals    = remember {
        generatePetals(petalColor = petalBaseColor(theme))
    }
    val grainPoints = remember { generateGrain() }
    val glows       = remember { defaultGlowsFor(theme) }

    // ── Colores derivados del tema — recalculados solo si cambia el tema ───────
    val pColor = remember(theme, config.particles.intensity, bgColor) {
        particleColor(theme, config.particles.intensity, bgColor)
    }
    val sColor = remember(theme, bgColor) { starColor(theme, bgColor) }

    // ── Temporizadores de animación ───────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "bg")

    // Tiempo maestro de partículas y pétalos (ciclo de 12 segundos)
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

    // Tiempo de parpadeo de estrellas (ciclo de 4 segundos)
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

    // Tiempo de pulso del glow (ciclo de 6 segundos)
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

    // Tiempo del grain (ciclo muy corto para shimmer cinematográfico)
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

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val W = size.width
                val H = size.height

                onDrawBehind {

                    // ── 1. Fondo sólido base ──────────────────────────────────
                    drawRect(color = bgColor)

                    // ── 2. Glow (halos radiales) ──────────────────────────────
                    if (config.glow.enabled) {
                        glows.forEach { g ->
                            val cx     = g.xFrac * W
                            val cy     = g.yFrac * H
                            val radius = g.radiusFrac * W
                            val color  = glowColor(
                                theme         = theme,
                                time          = glowT,
                                glowData      = g,
                                baseIntensity = config.glow.intensity,
                                bgColor       = bgColor
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color, Color.Transparent),
                                    center = Offset(cx, cy),
                                    radius = radius
                                ),
                                radius = radius,
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    // ── 3. Estrellas ──────────────────────────────────────────
                    if (config.stars.enabled) {
                        stars.forEach { s ->
                            val alpha = starAlpha(
                                s             = s,
                                time          = twinkleT,
                                baseIntensity = config.stars.intensity
                            )
                            if (alpha > 0.01f) {
                                drawCircle(
                                    color  = sColor.copy(alpha = alpha),
                                    radius = s.radius,
                                    center = Offset(s.xFrac * W, s.yFrac * H)
                                )
                            }
                        }
                    }

                    // ── 4. Partículas flotantes ───────────────────────────────────────────
                    if (config.particles.enabled) {
                        particles.forEach { p ->
                            val (x, y) = particlePosition(
                                p = p, time = floatT, width = W, height = H
                            )
                            val alpha = p.baseAlpha * config.particles.intensity
                            if (alpha > 0.01f) {
                                val center = Offset(x, y)
                                val color  = pColor.copy(alpha = alpha)
                                when (p.type) {

                                    SpecialParticleType.SNOWFLAKE -> {
                                        // 6 brazos cruzados
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            for (arm in 0 until 3) {
                                                withTransform({ rotate(arm * 60f, center) }) {
                                                    drawLine(
                                                        color       = color,
                                                        start       = Offset(x, y - p.radius * 1.6f),
                                                        end         = Offset(x, y + p.radius * 1.6f),
                                                        strokeWidth = p.radius * 0.35f
                                                    )
                                                    // Ramas del copo
                                                    val branchY = y - p.radius * 0.8f
                                                    drawLine(
                                                        color       = color,
                                                        start       = Offset(x - p.radius * 0.5f, branchY),
                                                        end         = Offset(x + p.radius * 0.5f, branchY),
                                                        strokeWidth = p.radius * 0.25f
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    SpecialParticleType.SPARKLE -> {
                                        // Destello de 4 puntas
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            drawLine(color,
                                                Offset(x, y - p.radius * 1.8f),
                                                Offset(x, y + p.radius * 1.8f),
                                                p.radius * 0.3f
                                            )
                                            drawLine(color,
                                                Offset(x - p.radius * 1.8f, y),
                                                Offset(x + p.radius * 1.8f, y),
                                                p.radius * 0.3f
                                            )
                                            // Diagonales más cortas
                                            drawLine(color,
                                                Offset(x - p.radius * 0.9f, y - p.radius * 0.9f),
                                                Offset(x + p.radius * 0.9f, y + p.radius * 0.9f),
                                                p.radius * 0.2f
                                            )
                                            drawLine(color,
                                                Offset(x + p.radius * 0.9f, y - p.radius * 0.9f),
                                                Offset(x - p.radius * 0.9f, y + p.radius * 0.9f),
                                                p.radius * 0.2f
                                            )
                                        }
                                    }

                                    SpecialParticleType.LEAF -> {
                                        // Hoja oval rotada
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            drawOval(
                                                color   = color,
                                                topLeft = Offset(x - p.radius * 0.5f, y - p.radius * 1.2f),
                                                size    = androidx.compose.ui.geometry.Size(
                                                    p.radius, p.radius * 2.4f
                                                )
                                            )
                                            // Nervio central
                                            drawLine(
                                                color       = color.copy(alpha = alpha * 0.6f),
                                                start       = Offset(x, y - p.radius * 1.1f),
                                                end         = Offset(x, y + p.radius * 1.1f),
                                                strokeWidth = p.radius * 0.2f
                                            )
                                        }
                                    }

                                    SpecialParticleType.SUN_RAY -> {
                                        // Mini sol con rayos cortos
                                        val rot = particleRotation(p, floatT)
                                        drawCircle(color, p.radius * 0.7f, center)
                                        withTransform({ rotate(rot, center) }) {
                                            for (ray in 0 until 8) {
                                                val angle = ray * 45f
                                                withTransform({ rotate(angle, center) }) {
                                                    drawLine(
                                                        color       = color.copy(alpha = alpha * 0.7f),
                                                        start       = Offset(x, y - p.radius * 1.0f),
                                                        end         = Offset(x, y - p.radius * 1.8f),
                                                        strokeWidth = p.radius * 0.25f
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    SpecialParticleType.BUBBLE -> {
                                        // Círculo con solo borde
                                        drawCircle(
                                            color       = color.copy(alpha = alpha * 0.3f),
                                            radius      = p.radius * 1.2f,
                                            center      = center
                                        )
                                        drawCircle(
                                            color       = color,
                                            radius      = p.radius * 1.2f,
                                            center      = center,
                                            style       = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = p.radius * 0.25f
                                            )
                                        )
                                    }

                                    SpecialParticleType.PETAL -> {
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            drawOval(
                                                color   = color,
                                                topLeft = Offset(x - p.radius * 0.55f, y - p.radius * 1.1f),
                                                size    = androidx.compose.ui.geometry.Size(
                                                    p.radius * 1.1f, p.radius * 2.2f
                                                )
                                            )
                                        }
                                    }

                                    SpecialParticleType.STAR_OUTLINE -> {
                                        // Estrella de 5 puntas — solo borde, elegante
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            val path = androidx.compose.ui.graphics.Path()
                                            val outerR = p.radius * 1.6f
                                            val innerR = p.radius * 0.65f
                                            val points = 5
                                            for (i in 0 until points * 2) {
                                                val angle = (i * PI.toFloat() / points) - PI.toFloat() / 2f
                                                val r     = if (i % 2 == 0) outerR else innerR
                                                val px    = x + r * kotlin.math.cos(angle)
                                                val py    = y + r * kotlin.math.sin(angle)
                                                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                            }
                                            path.close()
                                            drawPath(
                                                path  = path,
                                                color = color,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = p.radius * 0.22f
                                                )
                                            )
                                        }
                                    }

                                    SpecialParticleType.RAINDROP -> {
                                        // Gota de lluvia — óvalo alargado con punta arriba
                                        val rot = particleRotation(p, floatT) * 0.3f  // rotación muy suave
                                        withTransform({ rotate(rot, center) }) {
                                            val path = androidx.compose.ui.graphics.Path()
                                            // Cuerpo oval
                                            path.addOval(
                                                androidx.compose.ui.geometry.Rect(
                                                    left   = x - p.radius * 0.5f,
                                                    top    = y - p.radius * 0.5f,
                                                    right  = x + p.radius * 0.5f,
                                                    bottom = y + p.radius * 1.2f
                                                )
                                            )
                                            drawPath(path, color.copy(alpha = alpha * 0.55f))
                                            // Punta superior
                                            val tipPath = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(x, y - p.radius * 1.3f)
                                                lineTo(x - p.radius * 0.45f, y - p.radius * 0.4f)
                                                lineTo(x + p.radius * 0.45f, y - p.radius * 0.4f)
                                                close()
                                            }
                                            drawPath(tipPath, color.copy(alpha = alpha * 0.55f))
                                            // Reflejo interno
                                            drawCircle(
                                                color  = color.copy(alpha = alpha * 0.35f),
                                                radius = p.radius * 0.2f,
                                                center = Offset(x - p.radius * 0.15f, y - p.radius * 0.1f)
                                            )
                                        }
                                    }

                                    SpecialParticleType.FIREFLY -> {
                                        // Círculo con halo luminoso — efecto de luciérnaga
                                        // Halo exterior difuso
                                        drawCircle(
                                            color  = color.copy(alpha = alpha * 0.15f),
                                            radius = p.radius * 3.0f,
                                            center = center
                                        )
                                        // Halo medio
                                        drawCircle(
                                            color  = color.copy(alpha = alpha * 0.30f),
                                            radius = p.radius * 1.8f,
                                            center = center
                                        )
                                        // Núcleo brillante
                                        drawCircle(
                                            color  = color.copy(alpha = alpha * 0.90f),
                                            radius = p.radius * 0.7f,
                                            center = center
                                        )
                                        // Punto central blanco
                                        drawCircle(
                                            color  = androidx.compose.ui.graphics.Color.White.copy(alpha = alpha * 0.70f),
                                            radius = p.radius * 0.3f,
                                            center = Offset(x - p.radius * 0.15f, y - p.radius * 0.15f)
                                        )
                                    }

                                    SpecialParticleType.EMBER -> {
                                        // Chispa irregular — 3 líneas cortas en ángulos distintos
                                        val rot = particleRotation(p, floatT)
                                        withTransform({ rotate(rot, center) }) {
                                            // Línea principal
                                            drawLine(
                                                color       = color,
                                                start       = Offset(x, y - p.radius * 1.4f),
                                                end         = Offset(x + p.radius * 0.4f, y + p.radius * 1.0f),
                                                strokeWidth = p.radius * 0.4f,
                                                cap         = androidx.compose.ui.graphics.drawscope.DrawScope
                                                    .let { androidx.compose.ui.graphics.StrokeCap.Round }
                                            )
                                            // Línea secundaria — más corta y en otro ángulo
                                            drawLine(
                                                color       = color.copy(alpha = alpha * 0.6f),
                                                start       = Offset(x - p.radius * 0.6f, y - p.radius * 0.8f),
                                                end         = Offset(x + p.radius * 0.8f, y + p.radius * 0.3f),
                                                strokeWidth = p.radius * 0.28f,
                                                cap         = androidx.compose.ui.graphics.StrokeCap.Round
                                            )
                                            // Punto brillante en la punta
                                            drawCircle(
                                                color  = color.copy(alpha = alpha * 0.85f),
                                                radius = p.radius * 0.35f,
                                                center = Offset(x, y - p.radius * 1.3f)
                                            )
                                        }
                                    }

                                    SpecialParticleType.CRYSTAL -> {
                                        // Rombo / diamante con borde y reflejo
                                        val rot = particleRotation(p, floatT) * 0.5f
                                        withTransform({ rotate(rot, center) }) {
                                            val path = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(x,                    y - p.radius * 1.5f)  // arriba
                                                lineTo(x + p.radius * 0.9f, y)                     // derecha
                                                lineTo(x,                    y + p.radius * 1.5f)  // abajo
                                                lineTo(x - p.radius * 0.9f, y)                     // izquierda
                                                close()
                                            }
                                            // Relleno semitransparente
                                            drawPath(path, color.copy(alpha = alpha * 0.25f))
                                            // Borde
                                            drawPath(
                                                path  = path,
                                                color = color,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = p.radius * 0.25f
                                                )
                                            )
                                            // Reflejo interior — triángulo superior
                                            val reflectPath = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(x,                    y - p.radius * 1.5f)
                                                lineTo(x + p.radius * 0.9f, y)
                                                lineTo(x - p.radius * 0.9f, y)
                                                close()
                                            }
                                            drawPath(
                                                reflectPath,
                                                color.copy(alpha = alpha * 0.20f)
                                            )
                                        }
                                    }

                                    SpecialParticleType.NONE -> {
                                        drawCircle(color = color, radius = p.radius, center = center)
                                    }
                                }
                            }
                        }
                    }

                    // ── 5. Pétalos flotantes ──────────────────────────────────
                    if (config.petals.enabled) {
                        petals.forEach { p ->
                            val (x, y, rotation) = petalTransform(
                                p      = p,
                                time   = floatT,
                                width  = W,
                                height = H
                            )
                            val alpha = petalAlpha(
                                p             = p,
                                time          = floatT,
                                baseIntensity = config.petals.intensity
                            )
                            if (alpha > 0.01f) {
                                withTransform({
                                    rotate(rotation, Offset(x, y))
                                }) {
                                    // Pétalo como óvalo rotado — minimalista
                                    drawOval(
                                        color   = Color(
                                            p.colorR, p.colorG, p.colorB, alpha
                                        ),
                                        topLeft = Offset(
                                            x - p.radius * 0.6f,
                                            y - p.radius
                                        ),
                                        size    = androidx.compose.ui.geometry.Size(
                                            p.radius * 1.2f,
                                            p.radius * 2.0f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // ── 6. Grain cinematográfico ──────────────────────────────
                    if (config.grain.enabled) {
                        grainPoints.forEach { g ->
                            val alpha = grainAlpha(
                                g             = g,
                                time          = grainT,
                                baseIntensity = config.grain.intensity
                            )
                            if (alpha > 0.005f) {
                                drawCircle(
                                    color  = Color.White.copy(alpha = alpha),
                                    radius = g.radius,
                                    center = Offset(g.xFrac * W, g.yFrac * H)
                                )
                            }
                        }
                    }


                }
            }
    ) {
        content()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Versión simplificada para pantallas de dibujo
// Lee automáticamente el config y el tema del CompositionLocal
// ══════════════════════════════════════════════════════════════════════════════
@Composable
fun DrawingBackground(
    theme:    AppTheme,
    bgColor:  Color,
    modifier: Modifier = Modifier,
    content:  @Composable () -> Unit
) {
    val config = LocalBackgroundConfig.current

    DynamicBackground(
        theme    = theme,
        config   = config,
        bgColor  = bgColor,
        modifier = modifier,
        content  = content
    )
}
