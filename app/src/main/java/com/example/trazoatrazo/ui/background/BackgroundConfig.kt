package com.example.trazoatrazo.ui.background

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import com.example.trazoatrazo.ui.theme.AppTheme

// ── Configuración de un efecto individual ─────────────────────────────────────
@Immutable
data class EffectConfig(
    val enabled:   Boolean = true,
    val intensity: Float   = 0.5f    // 0f = invisible, 1f = máximo
)

// ── Configuración completa del fondo dinámico ─────────────────────────────────
@Immutable
data class BackgroundConfig(
    val particles:    EffectConfig = EffectConfig(),
    val activeTypes:  List<SpecialParticleType> = listOf(SpecialParticleType.NONE),
    val stars:        EffectConfig = EffectConfig(),
    val grain:        EffectConfig = EffectConfig(),
    val glow:         EffectConfig = EffectConfig(),
    val vignette:     EffectConfig = EffectConfig(enabled = false),
    val chromatic:    EffectConfig = EffectConfig(enabled = false),
    val scanlines:    EffectConfig = EffectConfig(enabled = false),
    val kaleidoscope: EffectConfig = EffectConfig(enabled = false), // NUEVO
    val waves:        EffectConfig = EffectConfig(enabled = false), // NUEVO
    val speed:        Float        = 1.0f,   
    val particleSize: Float        = 1.0f
)

// ── CompositionLocal para proveer el config al árbol de Compose ───────────────
val LocalBackgroundConfig = compositionLocalOf { BackgroundConfig() }

// ── Configs por defecto para cada tema ───────────────────────────────────────
fun defaultBackgroundConfigFor(theme: AppTheme): BackgroundConfig = when (theme) {

    AppTheme.JJK_DARK -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.45f),
        activeTypes  = listOf(SpecialParticleType.STAR_OUTLINE, SpecialParticleType.SPARKLE),
        stars        = EffectConfig(enabled = true,  intensity = 0.50f),
        grain        = EffectConfig(enabled = true,  intensity = 0.30f),
        glow         = EffectConfig(enabled = true,  intensity = 0.40f),
        vignette     = EffectConfig(enabled = true,  intensity = 0.35f)
    )

    AppTheme.MIDNIGHT_BLUE -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.45f),
        activeTypes  = listOf(SpecialParticleType.RAINDROP, SpecialParticleType.BUBBLE),
        stars        = EffectConfig(enabled = true,  intensity = 0.60f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.45f),
        vignette     = EffectConfig(enabled = true,  intensity = 0.40f)
    )

    AppTheme.FOREST -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.40f),
        activeTypes  = listOf(SpecialParticleType.FIREFLY, SpecialParticleType.LEAF, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.35f)
    )

    AppTheme.AMBER -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.35f),
        activeTypes  = listOf(SpecialParticleType.EMBER, SpecialParticleType.SUN_RAY, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = true,  intensity = 0.35f),
        glow         = EffectConfig(enabled = true,  intensity = 0.50f)
    )

    AppTheme.CRIMSON -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.45f),
        activeTypes  = listOf(SpecialParticleType.SPARKLE, SpecialParticleType.STAR_OUTLINE),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = true,  intensity = 0.40f),
        glow         = EffectConfig(enabled = true,  intensity = 0.45f),
        scanlines    = EffectConfig(enabled = true,  intensity = 0.15f)
    )

    AppTheme.SAKURA_NIGHT -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.30f),
        activeTypes  = listOf(SpecialParticleType.FIREFLY, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = true,  intensity = 0.40f),
        grain        = EffectConfig(enabled = false, intensity = 0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.50f)
    )

    AppTheme.SPRING_GARDEN -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.35f),
        activeTypes  = listOf(SpecialParticleType.CRYSTAL, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.35f)
    )

    AppTheme.SUMMER_SUN -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.30f),
        activeTypes  = listOf(SpecialParticleType.SUN_RAY, SpecialParticleType.BUBBLE, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.55f)
    )

    AppTheme.AUTUMN_LEAVES -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.40f),
        activeTypes  = listOf(SpecialParticleType.LEAF, SpecialParticleType.EMBER, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = true,  intensity = 0.35f),
        glow         = EffectConfig(enabled = true,  intensity = 0.45f)
    )

    AppTheme.WINTER_SNOW -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.50f),
        activeTypes  = listOf(SpecialParticleType.SNOWFLAKE, SpecialParticleType.CRYSTAL),
        stars        = EffectConfig(enabled = true,  intensity = 0.45f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.40f)
    )

    AppTheme.VALENTINE -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.45f),
        activeTypes  = listOf(SpecialParticleType.HEART_S, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = true,  intensity = 0.35f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.50f)
    )

    AppTheme.CYBERPUNK -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.55f),
        activeTypes  = listOf(SpecialParticleType.SQUARE_DOT, SpecialParticleType.SPARKLE),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = true,  intensity = 0.45f),
        glow         = EffectConfig(enabled = true,  intensity = 0.60f),
        scanlines    = EffectConfig(enabled = true,  intensity = 0.40f),
        chromatic    = EffectConfig(enabled = true,  intensity = 0.30f)
    )

    AppTheme.GOLDEN_NIGHT -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.50f),
        activeTypes  = listOf(SpecialParticleType.SHINE_STARDUST, SpecialParticleType.SPARKLE),
        stars        = EffectConfig(enabled = true,  intensity = 0.40f),
        grain        = EffectConfig(enabled = true,  intensity = 0.25f),
        glow         = EffectConfig(enabled = true,  intensity = 0.55f)
    )

    AppTheme.OCEAN_TIDE -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.50f),
        activeTypes  = listOf(SpecialParticleType.BUBBLE, SpecialParticleType.WAVE),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.40f)
    )

    AppTheme.RUBY_RED -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.50f),
        activeTypes  = listOf(SpecialParticleType.SPARKLE, SpecialParticleType.EMBER),
        stars        = EffectConfig(enabled = false, intensity = 0.0f),
        grain        = EffectConfig(enabled = true,  intensity = 0.30f),
        glow         = EffectConfig(enabled = true,  intensity = 0.45f)
    )

    AppTheme.SUNSET_PARTY -> BackgroundConfig(
        particles    = EffectConfig(enabled = true,  intensity = 0.55f),
        activeTypes  = listOf(SpecialParticleType.SUN_RAY, SpecialParticleType.PETAL),
        stars        = EffectConfig(enabled = true,  intensity = 0.30f),
        grain        = EffectConfig(enabled = false, intensity = 0.0f),
        glow         = EffectConfig(enabled = true,  intensity = 0.60f)
    )
}
