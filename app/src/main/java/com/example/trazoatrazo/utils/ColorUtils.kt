package com.example.trazoatrazo.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

/**
 * Calcula la luminancia relativa de un color (estándar WCAG).
 */
fun Color.luminance(): Float =
    red * 0.299f + green * 0.587f + blue * 0.114f

/**
 * Retorna un color contrastante (negro o blanco suave) para un fondo dado.
 */
fun textColorFor(bg: Color): Color =
    if (bg.luminance() > 0.45f) Color(0xFF1A1A1A) else Color(0xFFEAEAEA)

/**
 * Retorna un color de subtítulo contrastante para un fondo dado.
 */
fun subtitleColorFor(bg: Color): Color =
    if (bg.luminance() > 0.45f) Color(0xFF555555) else Color(0xFF9E9E9E)

/**
 * Ajusta un color para que tenga mejor visibilidad sobre un fondo determinado.
 * Si el fondo es oscuro, asegura que el color no sea demasiado oscuro.
 * Si el fondo es claro, asegura que el color no sea demasiado claro.
 */
fun Color.adaptiveColorFor(bg: Color): Color {
    val bgLum = bg.luminance()
    val isDarkBg = bgLum <= 0.45f
    
    // Si el fondo es oscuro, queremos que el color sea brillante (alta luminancia)
    if (isDarkBg) {
        return if (this.luminance() < 0.5f) {
            // Aclarar color: subir componentes proporcionalmente
            val factor = 1.5f
            Color(
                red = min(1f, red * factor),
                green = min(1f, green * factor),
                blue = min(1f, blue * factor),
                alpha = alpha
            )
        } else this
    } else {
        // Si el fondo es claro, queremos que el color sea oscuro
        return if (this.luminance() > 0.5f) {
            // Oscurecer color
            val factor = 0.6f
            Color(
                red = red * factor,
                green = green * factor,
                blue = blue * factor,
                alpha = alpha
            )
        } else this
    }
}
