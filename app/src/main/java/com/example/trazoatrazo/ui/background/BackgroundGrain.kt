package com.example.trazoatrazo.ui.background

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.ui.theme.AppTheme
import com.example.trazoatrazo.utils.luminance
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ══════════════════════════════════════════════════════════════════════════════
// GRAIN — Ruido cinematográfico estático muy tenue
// ══════════════════════════════════════════════════════════════════════════════
// ══════════════════════════════════════════════════════════════════════════════
// GRAIN
// ══════════════════════════════════════════════════════════════════════════════

@Immutable
data class GrainPoint(
    val xFrac:  Float,
    val yFrac:  Float,
    val radius: Float,
    val alpha:  Float,
    val phase:  Float
)

fun generateGrain(
    count: Int  = 150,
    seed:  Long = 99L
): List<GrainPoint> {
    val rng = Random(seed)
    return List(count) {
        GrainPoint(
            xFrac  = rng.nextFloat(),
            yFrac  = rng.nextFloat(),
            radius = 0.5f + rng.nextFloat() * 1.2f,
            alpha  = 0.04f + rng.nextFloat() * 0.10f,
            phase  = rng.nextFloat() * 2f * PI.toFloat()
        )
    }
}

fun grainAlpha(
    g:             GrainPoint,
    time:          Float,
    baseIntensity: Float
): Float {
    val shimmer = (sin(time * 1.8f * 2f * PI.toFloat() + g.phase) + 1f) / 2f
    return (g.alpha * (0.6f + shimmer * 0.4f) * baseIntensity).coerceIn(0f, 1f)
}

// ══════════════════════════════════════════════════════════════════════════════
// GLOW
// ══════════════════════════════════════════════════════════════════════════════

@Immutable
data class GlowData(
    val xFrac:      Float,
    val yFrac:      Float,
    val radiusFrac: Float,
    val pulsePhase: Float,
    val pulseFreq:  Float
)

fun defaultGlowsFor(theme: AppTheme): List<GlowData> = when (theme) {
    AppTheme.JJK_DARK -> listOf(
        GlowData(0.85f, 0.06f, 0.55f, 0.0f, 0.6f),
        GlowData(0.08f, 0.92f, 0.48f, 1.2f, 0.4f)
    )
    AppTheme.MIDNIGHT_BLUE -> listOf(
        GlowData(0.50f, 0.0f,  0.60f, 0.0f, 0.5f),
        GlowData(0.10f, 0.85f, 0.40f, 0.8f, 0.7f)
    )
    AppTheme.FOREST -> listOf(
        GlowData(0.20f, 0.10f, 0.50f, 0.0f, 0.4f),
        GlowData(0.80f, 0.80f, 0.45f, 1.5f, 0.6f)
    )
    AppTheme.AMBER -> listOf(
        GlowData(0.50f, 0.30f, 0.65f, 0.0f, 0.5f)
    )
    AppTheme.CRIMSON -> listOf(
        GlowData(0.15f, 0.08f, 0.55f, 0.0f, 0.7f),
        GlowData(0.85f, 0.90f, 0.42f, 0.9f, 0.5f)
    )
    AppTheme.SAKURA_NIGHT -> listOf(
        GlowData(0.50f, 0.15f, 0.58f, 0.0f, 0.4f),
        GlowData(0.90f, 0.75f, 0.38f, 1.8f, 0.6f)
    )
    AppTheme.SPRING_GARDEN -> listOf(
        GlowData(0.50f, 0.20f, 0.55f, 0.0f, 0.5f)
    )
    AppTheme.SUMMER_SUN -> listOf(
        GlowData(0.50f, 0.05f, 0.70f, 0.0f, 0.4f)
    )
    AppTheme.AUTUMN_LEAVES -> listOf(
        GlowData(0.75f, 0.10f, 0.52f, 0.0f, 0.5f),
        GlowData(0.20f, 0.88f, 0.44f, 1.0f, 0.6f)
    )
    AppTheme.WINTER_SNOW -> listOf(
        GlowData(0.50f, 0.25f, 0.60f, 0.0f, 0.4f)
    )
    AppTheme.VALENTINE -> listOf(
        GlowData(0.50f, 0.12f, 0.62f, 0.0f, 0.5f),
        GlowData(0.88f, 0.80f, 0.40f, 1.4f, 0.6f)
    )
    AppTheme.CYBERPUNK -> listOf(
        GlowData(0.10f, 0.08f, 0.58f, 0.0f, 0.8f),
        GlowData(0.90f, 0.88f, 0.50f, 1.0f, 0.9f)
    )

}

// ── Color del glow adaptivo al fondo ─────────────────────────────────────────
fun glowColor(
    theme:         AppTheme,
    time:          Float,
    glowData:      GlowData,
    baseIntensity: Float,
    bgColor:       Color = Color(0xFF0A0A0A)
): Color {
    val pulse = (sin(
        time * glowData.pulseFreq * 2f * PI.toFloat() + glowData.pulsePhase
    ) + 1f) / 2f

    // En fondos claros el glow es más fuerte para que se note
    val bgLum    = bgColor.luminance()
    val alphaBase = if (bgLum > 0.45f) 0.12f else 0.07f
    val alpha    = (alphaBase + pulse * alphaBase) * baseIntensity

    return when (theme) {
        AppTheme.JJK_DARK      -> Color(0xFF6B21A8).copy(alpha = alpha)
        AppTheme.MIDNIGHT_BLUE -> Color(0xFF3B82F6).copy(alpha = alpha)
        AppTheme.FOREST        -> Color(0xFF16A34A).copy(alpha = alpha)
        AppTheme.AMBER         -> Color(0xFFD97706).copy(alpha = alpha)
        AppTheme.CRIMSON       -> Color(0xFFDC2626).copy(alpha = alpha)
        AppTheme.SAKURA_NIGHT  -> Color(0xFFD11A5E).copy(alpha = alpha)
        AppTheme.SPRING_GARDEN -> Color(0xFF22C55E).copy(alpha = alpha)
        AppTheme.SUMMER_SUN    -> Color(0xFFF59E0B).copy(alpha = alpha)
        AppTheme.AUTUMN_LEAVES -> Color(0xFFC2410C).copy(alpha = alpha)
        AppTheme.WINTER_SNOW   -> Color(0xFF3B82F6).copy(alpha = alpha)
        AppTheme.VALENTINE     -> Color(0xFFE91E63).copy(alpha = alpha)  // Rosa intenso
        AppTheme.CYBERPUNK     -> Color(0xFF00E5FF).copy(alpha = alpha)  // Cian neón
    }
}

// ── Color de partículas adaptivo al fondo ─────────────────────────────────────
fun particleColor(
    theme:         AppTheme,
    baseIntensity: Float,
    bgColor:       Color = Color(0xFF0A0A0A)
): Color {
    val bgLum = bgColor.luminance()
    // Fondo claro → partículas oscuras. Fondo oscuro → partículas claras
    val alpha = if (bgLum > 0.45f)
        (0.45f + baseIntensity * 0.35f).coerceIn(0f, 1f)   // más opaco en claro
    else
        (0.30f + baseIntensity * 0.40f).coerceIn(0f, 1f)

    val baseColor = when (theme) {
        AppTheme.JJK_DARK      -> Color(0xFFC8A8F0)
        AppTheme.MIDNIGHT_BLUE -> Color(0xFFBFDBFE)
        AppTheme.FOREST        -> Color(0xFFBBF7D0)
        AppTheme.AMBER         -> Color(0xFF92400E)   // oscuro para fondo ámbar claro
        AppTheme.CRIMSON       -> Color(0xFFFECACA)
        AppTheme.SAKURA_NIGHT  -> Color(0xFFFFB7D5)
        AppTheme.SPRING_GARDEN -> Color(0xFF166534)   // oscuro para fondo verde claro
        AppTheme.SUMMER_SUN    -> Color(0xFF78350F)   // oscuro para fondo amarillo
        AppTheme.AUTUMN_LEAVES -> Color(0xFF7C2D12)   // oscuro para fondo naranja
        AppTheme.WINTER_SNOW   -> Color(0xFF1E3A5F)   // oscuro para fondo azul claro
        AppTheme.VALENTINE     -> Color(0xFFFFB7C5)  // Rosa pálido
        AppTheme.CYBERPUNK     -> Color(0xFF00E5FF)  // Cian neón
    }

    // Si el fondo es claro y el color base también es claro, lo oscurecemos
    return if (bgLum > 0.45f && baseColor.luminance() > 0.45f) {
        Color(
            red   = (baseColor.red   * 0.4f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.4f).coerceIn(0f, 1f),
            blue  = (baseColor.blue  * 0.4f).coerceIn(0f, 1f),
            alpha = alpha
        )
    } else {
        baseColor.copy(alpha = alpha)
    }
}

// ── Color de estrellas adaptivo al fondo ──────────────────────────────────────
fun starColor(
    theme:   AppTheme,
    bgColor: Color = Color(0xFF0A0A0A)
): Color {
    val bgLum = bgColor.luminance()
    // En fondos claros las estrellas son oscuras
    return if (bgLum > 0.45f) {
        when (theme) {
            AppTheme.FOREST        -> Color(0xFF14532D)
            AppTheme.SPRING_GARDEN -> Color(0xFF14532D)
            AppTheme.SUMMER_SUN    -> Color(0xFF713F12)
            AppTheme.AUTUMN_LEAVES -> Color(0xFF7C2D12)
            AppTheme.WINTER_SNOW   -> Color(0xFF1E3A5F)
            AppTheme.AMBER         -> Color(0xFF78350F)
            else                   -> Color(0xFF1A1A2E)
        }
    } else {
        when (theme) {
            AppTheme.JJK_DARK,
            AppTheme.MIDNIGHT_BLUE,
            AppTheme.CRIMSON,
            AppTheme.SAKURA_NIGHT,
            AppTheme.WINTER_SNOW   -> Color.White
            AppTheme.FOREST        -> Color(0xFFBBF7D0)
            AppTheme.AMBER         -> Color(0xFFFDE68A)
            AppTheme.SPRING_GARDEN -> Color(0xFF4ADE80)
            AppTheme.SUMMER_SUN    -> Color(0xFFFBBF24)
            AppTheme.AUTUMN_LEAVES -> Color(0xFFFFED4A)
            AppTheme.VALENTINE     -> Color(0xFF880E4F)
            AppTheme.CYBERPUNK     -> Color(0xFF006064)
//            else                   -> Color(0xFF1A1A2E)
        }
    }
}

// ── Color base de pétalos por tema ────────────────────────────────────────────
fun petalBaseColor(theme: AppTheme): Color = when (theme) {
    AppTheme.JJK_DARK      -> Color(0xFFC8A8F0)
    AppTheme.MIDNIGHT_BLUE -> Color(0xFF93C5FD)
    AppTheme.FOREST        -> Color(0xFF86EFAC)
    AppTheme.AMBER         -> Color(0xFFFDE68A)
    AppTheme.CRIMSON       -> Color(0xFFFCA5A5)
    AppTheme.SAKURA_NIGHT  -> Color(0xFFFFB7D5)
    AppTheme.SPRING_GARDEN -> Color(0xFF6EE7B7)
    AppTheme.SUMMER_SUN    -> Color(0xFFFDE68A)
    AppTheme.AUTUMN_LEAVES -> Color(0xFFFA6F02)
    AppTheme.WINTER_SNOW   -> Color(0xFFBAE6FD)
    AppTheme.VALENTINE     -> Color(0xFFFF80AB)
    AppTheme.CYBERPUNK     -> Color(0xFF18FFFF)
}