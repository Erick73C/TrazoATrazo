package com.example.trazoatrazo.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.ui.background.SpecialParticleType

/**
// SpecialEvent — transformación temporal y no-persistente de la app
// Se definen por mes/día (recurrentes cada año), no por fecha completa, para
// que "Navidad" se repita automáticamente cada 25/12 sin tocar el catálogo.
//
// [id]                 Identificador único del evento.
// [name]                Nombre visible, ej: "Navidad 🎄"
// [startMonth/Day]      Inicio del rango (inclusive).
// [endMonth/Day]        Fin del rango (inclusive). Puede cruzar fin de año.
// [accentColor]         Color de acento temporal (tarjeta de mensaje, glow, etc).
// [particleType]        Tipo de partícula especial que se mezcla en el fondo.
// [bannerEmoji]         Emoji del banner que aparece en el Home.
// [bannerMessage]       Texto del banner, ej: "🎄 ¡Feliz Navidad!"
// [extraWelcomeMessages] Mensajes que se suman temporalmente a los de HomeViewModel.
// [temporaryDrawingId]  Dibujo que aparece destacado solo durante el evento (opcional).
// [temporaryDrawingCategoryId] Categoría a la que pertenece ese dibujo temporal.
**/
@Immutable
data class SpecialEvent(
    val id:                       String,
    val name:                     String,
    val startMonth:                Int,
    val startDay:                  Int,
    val endMonth:                  Int,
    val endDay:                    Int,
    val accentColor:               Color,
    val particleType:              SpecialParticleType = SpecialParticleType.SPARKLE,
    val bannerEmoji:                String = "✨",
    val bannerMessage:              String,
    val extraWelcomeMessages:       List<String> = emptyList(),
    val temporaryDrawingId:         String? = null,
    val temporaryDrawingCategoryId: String? = null
) {
    /** Devuelve una cadena legible con la vigencia del evento (ej: "12 feb - 16 feb"). */
    val durationText: String
        get() {
            val months = listOf("", "ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic.")
            val start = "$startDay ${months.getOrElse(startMonth) { "" }}"
            val end   = "$endDay ${months.getOrElse(endMonth) { "" }}"
            return "$start - $end"
        }
}
