package com.example.trazoatrazo.ui.background

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ══════════════════════════════════════════════════════════════════════════════
// MODELOS DE DATOS — generados una sola vez con remember en el Composable
// ══════════════════════════════════════════════════════════════════════════════

// ══════════════════════════════════════════════════════════════════════════════
// TIPO DE PARTÍCULA ESPECIAL POR TEMA
// ══════════════════════════════════════════════════════════════════════════════
enum class SpecialParticleType {
    NONE,           // partícula circular normal
    SNOWFLAKE,      // copo de nieve (invierno)
    PETAL,          // pétalo oval (sakura, primavera)
    LEAF,           // hoja (otoño, bosque)
    SPARKLE,        // destello de 4 puntas (especial, JJK)
    SUN_RAY,        // mini sol / rayo (verano, ámbar)
    BUBBLE,         // círculo con borde (bosque, primavera)
    STAR_OUTLINE,    // estrella de 5 puntas con solo borde — JJK, Crimson
    RAINDROP,        // gota de lluvia — Midnight Blue
    FIREFLY,         // círculo con halo luminoso — Forest, Sakura Night
    EMBER,           // chispa pequeña irregular — Amber, Autumn
    CRYSTAL,         // rombo/diamante — Winter, Spring
    HEART_S,         // corazón pequeño — Valentín
    SQUARE_DOT,      // cuadrado / pixel — Cyberpunk
    SHINE_STARDUST,  // punto con destello — Noche de Oro
    WAVE,            // onda / curva — Océano
    EMOJI            // personalizada (usuario o evento)
}

fun specialParticleTypeFor(theme: AppTheme): SpecialParticleType = when (theme) {
    AppTheme.WINTER_SNOW   -> SpecialParticleType.SNOWFLAKE
    AppTheme.SAKURA_NIGHT  -> SpecialParticleType.FIREFLY
    AppTheme.SPRING_GARDEN -> SpecialParticleType.CRYSTAL
    AppTheme.AUTUMN_LEAVES -> SpecialParticleType.LEAF
    AppTheme.SUMMER_SUN    -> SpecialParticleType.SUN_RAY
    AppTheme.AMBER         -> SpecialParticleType.EMBER
    AppTheme.FOREST        -> SpecialParticleType.FIREFLY
    AppTheme.JJK_DARK      -> SpecialParticleType.STAR_OUTLINE
    AppTheme.CRIMSON       -> SpecialParticleType.SPARKLE
    AppTheme.MIDNIGHT_BLUE -> SpecialParticleType.RAINDROP
    AppTheme.VALENTINE     -> SpecialParticleType.HEART_S
    AppTheme.CYBERPUNK     -> SpecialParticleType.SQUARE_DOT
    AppTheme.GOLDEN_NIGHT  -> SpecialParticleType.SHINE_STARDUST
    AppTheme.OCEAN_TIDE    -> SpecialParticleType.WAVE
    AppTheme.RUBY_RED      -> SpecialParticleType.SPARKLE
    AppTheme.SUNSET_PARTY  -> SpecialParticleType.SUN_RAY
}

// ══════════════════════════════════════════════════════════════════════════════
// MODELOS DE DATOS
// ══════════════════════════════════════════════════════════════════════════════

@Immutable
data class ParticleData(
    val xFrac:        Float,
    val yFrac:        Float,
    val radius:       Float,
    val speedY:       Float,
    val speedX:       Float,
    val phase:        Float,
    val driftFreq:    Float,
    val baseAlpha:    Float,
    val rotSpeed:     Float  = 0f,
    val type:         SpecialParticleType = SpecialParticleType.NONE,
    val emoji:        String? = null
)

@Immutable
data class StarData(
    val xFrac:         Float,
    val yFrac:         Float,
    val radius:        Float,
    val twinklePhase:  Float,
    val twinkleFreq:   Float
)

// ══════════════════════════════════════════════════════════════════════════════
// GENERADORES
// ══════════════════════════════════════════════════════════════════════════════

fun generateParticles(
    count: Int  = 45,
    seed:  Long = 77L,
    theme: AppTheme = AppTheme.JJK_DARK,
    activeTypes: List<SpecialParticleType> = listOf(SpecialParticleType.NONE),
    emojis: List<String> = emptyList()
): List<ParticleData> {
    val rng = Random(seed)
    return List(count) { i ->
        val type = if (activeTypes.isEmpty()) SpecialParticleType.NONE 
                   else activeTypes[i % activeTypes.size]
        
        val emoji = if (type == SpecialParticleType.EMOJI && emojis.isNotEmpty()) {
            emojis[rng.nextInt(emojis.size)]
        } else null

        ParticleData(
            xFrac     = rng.nextFloat(),
            yFrac     = rng.nextFloat(),
            radius    = 3.5f + rng.nextFloat() * 5.5f,
            speedY    = 0.022f + rng.nextFloat() * 0.040f,
            speedX    = (rng.nextFloat() - 0.5f) * 0.014f,
            phase     = rng.nextFloat() * 2f * PI.toFloat(),
            driftFreq = 0.4f + rng.nextFloat() * 0.8f,
            baseAlpha = 0.45f + rng.nextFloat() * 0.50f,
            rotSpeed  = 0.2f + rng.nextFloat() * 1.2f,
            type      = type,
            emoji     = emoji
        )
    }
}

fun generateStars(
    count: Int  = 65,
    seed:  Long = 13L
): List<StarData> {
    val rng = Random(seed)
    return List(count) {
        StarData(
            xFrac        = rng.nextFloat(),
            yFrac        = rng.nextFloat() * 0.85f,
            radius       = 1.0f + rng.nextFloat() * 2.5f,   // más grandes
            twinklePhase = rng.nextFloat() * 2f * PI.toFloat(),
            twinkleFreq  = 0.3f + rng.nextFloat() * 1.2f
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CALCULADORES DE POSICIÓN
// ══════════════════════════════════════════════════════════════════════════════

fun particlePosition(
    p:      ParticleData,
    time:   Float,
    width:  Float,
    height: Float
): Pair<Float, Float> {
    val yProgress = (p.yFrac - time * p.speedY * 8f).mod(1f)
    val y = yProgress * height
    val drift = sin(time * p.driftFreq * 2f * PI.toFloat() + p.phase) *
            p.speedX * width * 12f
    val x = (p.xFrac * width + drift).coerceIn(0f, width)
    return Pair(x, y)
}

fun particleRotation(
    p:    ParticleData,
    time: Float
): Float = (time * p.rotSpeed * 360f + p.phase * 57.3f).mod(360f)

fun starAlpha(
    s:             StarData,
    time:          Float,
    baseIntensity: Float
): Float {
    val twinkle = (sin(time * s.twinkleFreq * 2f * PI.toFloat() + s.twinklePhase) + 1f) / 2f
    // Rango más visible: 0.25..0.85
    return (0.25f + twinkle * 0.60f) * baseIntensity
}
