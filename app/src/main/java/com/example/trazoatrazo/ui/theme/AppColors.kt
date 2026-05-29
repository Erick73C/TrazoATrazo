package com.example.trazoatrazo.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Paleta principal · Trazo a Trazo ─────────────────────────────────────────

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val LocalAppColors = compositionLocalOf<ThemeColorScheme> { jjkDarkScheme }

object AppColors {

    // ── Estado reactivo ── el único punto de verdad del tema activo ───────────
    private var _scheme: ThemeColorScheme by mutableStateOf(jjkDarkScheme)

    // ── Aplica un tema (llamado desde SettingsViewModel) ──────────────────────
    fun applyTheme(theme: AppTheme) {
        _scheme = themeColorSchemeFor(theme)
    }

    // ── Fondos ────────────────────────────────────────────────────────────────
    val Vacio:   Color get() = _scheme.vacio
    val Sombra:  Color get() = _scheme.sombra
    val Dominio: Color get() = _scheme.dominio

    // ── Primarios ─────────────────────────────────────────────────────────────
    val Maldicion:    Color get() = _scheme.maldicion
    val Tecnica:      Color get() = _scheme.tecnica
    val KiEspiritual: Color get() = _scheme.kiEspiritual

    // ── Secundarios ───────────────────────────────────────────────────────────
    val Expansion: Color get() = _scheme.expansion
    val Eco:       Color get() = _scheme.eco
    val Sukuna:    Color get() = _scheme.sukuna

    // ── Texto ─────────────────────────────────────────────────────────────────
    val Reversa:      Color get() = _scheme.reversa
    val ReversaSuave: Color get() = _scheme.reversaSuave

    // ── Categorías ────────────────────────────────────────────────────────────
    val FlowersBg:      Color get() = _scheme.flowersBg
    val FlowersAccent:  Color get() = _scheme.flowersAccent
    val CartoonsBg:     Color get() = _scheme.cartoonsBg
    val CartoonsAccent: Color get() = _scheme.cartoonsAccent
    val AnimalsBg:      Color get() = _scheme.animalsBg
    val AnimalsAccent:  Color get() = _scheme.animalsAccent
}