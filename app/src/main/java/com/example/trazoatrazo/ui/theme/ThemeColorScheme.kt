package com.example.trazoatrazo.ui.theme

import androidx.compose.ui.graphics.Color

// ── Modelo de esquema de colores ──────────────────────────────────────────────
data class ThemeColorScheme(
    // Fondos
    val vacio:        Color,
    val sombra:       Color,
    val dominio:      Color,
    // Primarios
    val maldicion:    Color,
    val tecnica:      Color,
    val kiEspiritual: Color,
    // Secundarios
    val expansion:    Color,
    val eco:          Color,
    val sukuna:       Color,
    // Texto
    val reversa:      Color,
    val reversaSuave: Color,
    // Categorías
    val flowersBg:      Color,
    val flowersAccent:  Color,
    val cartoonsBg:     Color,
    val cartoonsAccent: Color,
    val animalsBg:      Color,
    val animalsAccent:  Color
)

// ── Función helper ────────────────────────────────────────────────────────────
fun themeColorSchemeFor(theme: AppTheme): ThemeColorScheme = when (theme) {
    AppTheme.JJK_DARK      -> jjkDarkScheme
    AppTheme.MIDNIGHT_BLUE -> midnightBlueScheme
    AppTheme.FOREST        -> forestScheme
    AppTheme.AMBER         -> amberScheme
    AppTheme.CRIMSON       -> crimsonScheme
}

// ─────────────────────────────────────────────────────────────────────────────
// ── 1. JJK OSCURO (morado · default actual) ───────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
val jjkDarkScheme = ThemeColorScheme(
    vacio        = Color(0xFF0A0A0A),
    sombra       = Color(0xFF141414),
    dominio      = Color(0xFF1E1E2E),
    maldicion    = Color(0xFF6B21A8),
    tecnica      = Color(0xFF9333EA),
    kiEspiritual = Color(0xFFC8A8F0),
    expansion    = Color(0xFF2D1B69),
    eco          = Color(0xFF7B6A9A),
    sukuna       = Color(0xFFB91C1C),
    reversa      = Color(0xFFEAEAEA),
    reversaSuave = Color(0xFFAAAAAA),
    flowersBg      = Color(0xFF1A1225),
    flowersAccent  = Color(0xFF9333EA),
    cartoonsBg     = Color(0xFF1A1A10),
    cartoonsAccent = Color(0xFFB45309),
    animalsBg      = Color(0xFF0F1A12),
    animalsAccent  = Color(0xFF16A34A)
)

// ─────────────────────────────────────────────────────────────────────────────
// ── 2. AZUL NOCHE ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
val midnightBlueScheme = ThemeColorScheme(
    vacio        = Color(0xFF060B14),
    sombra       = Color(0xFF0D1525),
    dominio      = Color(0xFF162040),
    maldicion    = Color(0xFF1D4ED8),
    tecnica      = Color(0xFF3B82F6),
    kiEspiritual = Color(0xFFBFDBFE),
    expansion    = Color(0xFF1E3A6E),
    eco          = Color(0xFF5B7A99),
    sukuna       = Color(0xFFDC2626),
    reversa      = Color(0xFFE2EEFF),
    reversaSuave = Color(0xFF8AADC8),
    flowersBg      = Color(0xFF0A1430),
    flowersAccent  = Color(0xFF3B82F6),
    cartoonsBg     = Color(0xFF080E20),
    cartoonsAccent = Color(0xFF60A5FA),
    animalsBg      = Color(0xFF060C18),
    animalsAccent  = Color(0xFF38BDF8)
)

// ─────────────────────────────────────────────────────────────────────────────
// ── 3. BOSQUE ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
val forestScheme = ThemeColorScheme(
    vacio        = Color(0xFF050E07),
    sombra       = Color(0xFF0C1A0E),
    dominio      = Color(0xFF142818),
    maldicion    = Color(0xFF166534),
    tecnica      = Color(0xFF16A34A),
    kiEspiritual = Color(0xFFBBF7D0),
    expansion    = Color(0xFF14532D),
    eco          = Color(0xFF5E7A62),
    sukuna       = Color(0xFFDC2626),
    reversa      = Color(0xFFECFDF5),
    reversaSuave = Color(0xFF86EFAC),
    flowersBg      = Color(0xFF081A0C),
    flowersAccent  = Color(0xFF16A34A),
    cartoonsBg     = Color(0xFF061208),
    cartoonsAccent = Color(0xFF4ADE80),
    animalsBg      = Color(0xFF050E06),
    animalsAccent  = Color(0xFF22C55E)
)

// ─────────────────────────────────────────────────────────────────────────────
// ── 4. ÁMBAR ──────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
val amberScheme = ThemeColorScheme(
    vacio        = Color(0xFF0D0904),
    sombra       = Color(0xFF1C1408),
    dominio      = Color(0xFF2A1E0A),
    maldicion    = Color(0xFF92400E),
    tecnica      = Color(0xFFD97706),
    kiEspiritual = Color(0xFFFDE68A),
    expansion    = Color(0xFF451A03),
    eco          = Color(0xFF9A7A45),
    sukuna       = Color(0xFFDC2626),
    reversa      = Color(0xFFFFFBEB),
    reversaSuave = Color(0xFFF59E0B),
    flowersBg      = Color(0xFF1A1000),
    flowersAccent  = Color(0xFFD97706),
    cartoonsBg     = Color(0xFF140C00),
    cartoonsAccent = Color(0xFFF59E0B),
    animalsBg      = Color(0xFF100A00),
    animalsAccent  = Color(0xFFEAB308)
)

// ─────────────────────────────────────────────────────────────────────────────
// ── 5. CRIMSON ────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
val crimsonScheme = ThemeColorScheme(
    vacio        = Color(0xFF0D0505),
    sombra       = Color(0xFF1A0808),
    dominio      = Color(0xFF2A0E0E),
    maldicion    = Color(0xFF991B1B),
    tecnica      = Color(0xFFDC2626),
    kiEspiritual = Color(0xFFFECACA),
    expansion    = Color(0xFF450A0A),
    eco          = Color(0xFF9A6A6A),
    sukuna       = Color(0xFFB91C1C),
    reversa      = Color(0xFFFFF1F1),
    reversaSuave = Color(0xFFFCA5A5),
    flowersBg      = Color(0xFF1A0A10),
    flowersAccent  = Color(0xFFDC2626),
    cartoonsBg     = Color(0xFF140606),
    cartoonsAccent = Color(0xFFF87171),
    animalsBg      = Color(0xFF100505),
    animalsAccent  = Color(0xFFEF4444)
)