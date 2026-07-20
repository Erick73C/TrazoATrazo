package com.example.trazoatrazo.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Paleta principal · Trazo a Trazo ─────────────────────────────────────────

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.trazoatrazo.ui.background.BackgroundConfig

val LocalAppColors = compositionLocalOf<ThemeColorScheme> { jjkDarkScheme }
val LocalUiTransparency = compositionLocalOf { 0f }

// ─────────────────────────────────────────────────────────────────────────────
// Grupos de estado internos — cada uno es un mutableStateOf independiente.
// Un composable que solo lee, por ejemplo, AppColors.FlowersAccent queda.
// "suscrito" solo al grupo Categories, no a los 20 valores del tema entero.
// Esto no elimina la recomposición al cambiar de tema (todos los grupos
// cambian a la vez en un swap completo), pero sí acota qué composables se
// re-evalúan cuando en el futuro se quiera actualizar solo una parte del
// tema (ej. solo acentos de categoría) sin tocar fondo/texto.
// ─────────────────────────────────────────────────────────────────────────────

private data class BackgroundGroup(val vacio: Color, val sombra: Color, val dominio: Color)
private data class PrimaryGroup(val maldicion: Color, val tecnica: Color, val kiEspiritual: Color)
private data class SecondaryGroup(val expansion: Color, val eco: Color, val sukuna: Color)
private data class TextGroup(val reversa: Color, val reversaSuave: Color)
private data class CategoriesGroup(
    val flowersBg: Color, val flowersAccent: Color,
    val cartoonsBg: Color, val cartoonsAccent: Color,
    val animalsBg: Color, val animalsAccent: Color,
    val springBg: Color, val springAccent: Color,
    val winterBg: Color, val winterAccent: Color
)

object AppColors {

    // ── Estado reactivo, ahora dividido por grupo ──────────────────────────
    private var _currentThemeId: String by mutableStateOf(jjkDarkScheme.appTheme.name)
    private var _background by mutableStateOf(toBackgroundGroup(jjkDarkScheme))
    private var _primary    by mutableStateOf(toPrimaryGroup(jjkDarkScheme))
    private var _secondary  by mutableStateOf(toSecondaryGroup(jjkDarkScheme))
    private var _text       by mutableStateOf(toTextGroup(jjkDarkScheme))
    private var _categories by mutableStateOf(toCategoriesGroup(jjkDarkScheme))

    // ── Aplica un tema (llamado desde SettingsViewModel) ──────────────────────
    fun applyTheme(theme: AppTheme) {
        val scheme = themeColorSchemeFor(theme)
        _currentThemeId = scheme.appTheme.name
        _background = toBackgroundGroup(scheme)
        _primary    = toPrimaryGroup(scheme)
        _secondary  = toSecondaryGroup(scheme)
        _text       = toTextGroup(scheme)
        _categories = toCategoriesGroup(scheme)
    }

    // ── Propiedades de estado ─────────────────────────────────────────────────
    val currentThemeId: String get() = _currentThemeId

    // ── Fondos ────────────────────────────────────────────────────────────────
    val Vacio:   Color get() = _background.vacio
    val Sombra:  Color get() = _background.sombra
    val Dominio: Color get() = _background.dominio

    // ── Primarios ─────────────────────────────────────────────────────────────
    val Maldicion:    Color get() = _primary.maldicion
    val Tecnica:      Color get() = _primary.tecnica
    val KiEspiritual: Color get() = _primary.kiEspiritual

    // ── Secundarios ───────────────────────────────────────────────────────────
    val Expansion: Color get() = _secondary.expansion
    val Eco:       Color get() = _secondary.eco
    val Sukuna:    Color get() = _secondary.sukuna

    // ── Texto ─────────────────────────────────────────────────────────────────
    val Reversa:      Color get() = _text.reversa
    val ReversaSuave: Color get() = _text.reversaSuave

    // ── Categorías ────────────────────────────────────────────────────────────
    val FlowersBg:      Color get() = _categories.flowersBg
    val FlowersAccent:  Color get() = _categories.flowersAccent
    val CartoonsBg:     Color get() = _categories.cartoonsBg
    val CartoonsAccent: Color get() = _categories.cartoonsAccent
    val AnimalsBg:      Color get() = _categories.animalsBg
    val AnimalsAccent:  Color get() = _categories.animalsAccent
    val SpringBg:       Color get() = _categories.springBg
    val SpringAccent:   Color get() = _categories.springAccent
    val WinterBg:       Color get() = _categories.winterBg
    val WinterAccent:   Color get() = _categories.winterAccent
}


// ── Mappers privados: ThemeColorScheme → grupo ────────────────────────────────
private fun toBackgroundGroup(s: ThemeColorScheme) =
    BackgroundGroup(s.vacio, s.sombra, s.dominio)

private fun toPrimaryGroup(s: ThemeColorScheme) =
    PrimaryGroup(s.maldicion, s.tecnica, s.kiEspiritual)

private fun toSecondaryGroup(s: ThemeColorScheme) =
    SecondaryGroup(s.expansion, s.eco, s.sukuna)

private fun toTextGroup(s: ThemeColorScheme) =
    TextGroup(s.reversa, s.reversaSuave)

private fun toCategoriesGroup(s: ThemeColorScheme) =
    CategoriesGroup(
        s.flowersBg, s.flowersAccent,
        s.cartoonsBg, s.cartoonsAccent,
        s.animalsBg, s.animalsAccent,
        s.springBg, s.springAccent,
        s.winterBg, s.winterAccent
    )