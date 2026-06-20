package com.example.trazoatrazo.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.trazoatrazo.R

// ══════════════════════════════════════════════════════════════════════════════
// AppFont — catálogo de tipografías disponibles para personalizar la app
// ══════════════════════════════════════════════════════════════════════════════
enum class AppFont(
    val displayName: String,
    val emoji:       String,
    val description: String
) {
    QUICKSAND(
        displayName = "Quicksand",
        emoji       = "🌤️",
        description = "Suave y redondeada · Por defecto"
    ),
    CAVEAT(
        displayName = "Caveat",
        emoji       = "✍️",
        description = "Manuscrita · Personal y cálida"
    ),
    NUNITO(
        displayName = "Nunito",
        emoji       = "🌿",
        description = "Elegante con itálica suave"
    ),
    PACIFICO(
        displayName = "Pacifico",
        emoji       = "🎨",
        description = "Script casual · Divertida"
    ),
    PLAYFAIR(
        displayName = "Playfair",
        emoji       = "📖",
        description = "Serif editorial · Sofisticada"
    ),
    BITCOUNT(
        displayName = "Bitcount",
        emoji       = "🕹️",
        description = "Retro pixel · Juguetona"
    )
}

// ── FontFamily real de Compose para cada opción ───────────────────────────────
fun fontFamilyFor(font: AppFont): FontFamily = when (font) {
    AppFont.QUICKSAND -> FontFamily(
        Font(R.font.quicksand_variable_font_wght, FontWeight.Normal),
        Font(R.font.quicksand_variable_font_wght, FontWeight.Medium),
        Font(R.font.quicksand_variable_font_wght, FontWeight.Bold)
    )

    AppFont.CAVEAT -> FontFamily(
        Font(R.font.caveat_variable_font_wght, FontWeight.Normal),
        Font(R.font.caveat_variable_font_wght, FontWeight.Bold)
    )

    AppFont.NUNITO -> FontFamily(
        Font(R.font.nunito_italic_variable_font_wght, FontWeight.Normal),
        Font(R.font.nunito_italic_variable_font_wght, FontWeight.Medium),
        Font(R.font.nunito_italic_variable_font_wght, FontWeight.Bold)
    )

    AppFont.PACIFICO -> FontFamily(
        Font(R.font.pacifico_regular, FontWeight.Normal),
        Font(R.font.pacifico_regular, FontWeight.Medium),
        Font(R.font.pacifico_regular, FontWeight.Bold)
    )

    AppFont.PLAYFAIR -> FontFamily(
        Font(R.font.playfair_display_variable_font_wght, FontWeight.Normal),
        Font(R.font.playfair_display_variable_font_wght, FontWeight.Bold)
    )

    AppFont.BITCOUNT -> FontFamily(
        Font(R.font.bitcount_grid_double, FontWeight.Normal),
        Font(R.font.bitcount_grid_double, FontWeight.Bold)
    )
}

// ── CompositionLocal para proveer la fuente activa al árbol de Compose ────────
val LocalAppFont = compositionLocalOf { AppFont.QUICKSAND }