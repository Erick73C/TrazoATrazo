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
    ),
    BANGERS(
        displayName = "Bangers",
        emoji       = "💥",
        description = "Estilo cómic · Energía y acción"
    ),
    BEBAS_NEUE(
        displayName = "Bebas Neue",
        emoji       = "🏗️",
        description = "Condensada · Moderna y minimalista"
    ),
    ROWDIES(
        displayName = "Rowdies",
        emoji       = "⚡",
        description = "Urbana · Fuerte y audaz"
    ),
    SHADOWS_INTO_LIGHT(
        displayName = "Shadows Into Light",
        emoji       = "☁️",
        description = "Manuscrita · Ligera y soñadora"
    ),
    UNCIAL_ANTIQUA(
        displayName = "Uncial Antiqua",
        emoji       = "📜",
        description = "Celta · Clásica y legendaria"
    ),
    YUYU(
        displayName = "YuYu",
        emoji       = "✨",
        description = "Adorable · Amigable y suave"
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

    AppFont.BANGERS -> FontFamily(
        Font(R.font.bangers_regular, FontWeight.Normal)
    )

    AppFont.BEBAS_NEUE -> FontFamily(
        Font(R.font.bebas_neue_regular, FontWeight.Normal)
    )

    AppFont.ROWDIES -> FontFamily(
        Font(R.font.rowdies_regular, FontWeight.Normal)
    )

    AppFont.SHADOWS_INTO_LIGHT -> FontFamily(
        Font(R.font.shadows_into_light_regular, FontWeight.Normal)
    )

    AppFont.UNCIAL_ANTIQUA -> FontFamily(
        Font(R.font.uncial_antiqua_regular, FontWeight.Normal)
    )

    AppFont.YUYU -> FontFamily(
        Font(R.font.yuyu_regular, FontWeight.Normal)
    )
}

// ── CompositionLocal para proveer la fuente activa al árbol de Compose ────────
val LocalAppFont = compositionLocalOf { AppFont.QUICKSAND }

// ── Estilos de mensaje (Negrita, Cursiva, etc.) ──────────────────────────────
enum class MessageStyle(val displayName: String, val emoji: String) {
    NORMAL("Normal", "📄"),
    BOLD("Negrita", "B"),
    ITALIC("Cursiva", "I"),
    UNDERLINE("Subrayado", "U")
}

val LocalMessageStyle = compositionLocalOf { MessageStyle.NORMAL }
