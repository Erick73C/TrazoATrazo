package com.example.trazoatrazo.presentation.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.theme.*

// ── SettingsScreen ────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack:    () -> Unit
) {
    val selectedTheme    by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val backgroundConfig by viewModel.backgroundConfig.collectAsStateWithLifecycle()
    val selectedFont     by viewModel.selectedFont.collectAsStateWithLifecycle()

    val screenAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnim.animateTo(1f, tween(600, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(onBack = onBack)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = screenAnim.value
                        translationY = (1f - screenAnim.value) * 15.dp.toPx()
                    },
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // ── SECCIÓN 1: TEMAS ──────────────────────────────────────────
                item {
                    MinimalSectionHeader(title = "Temas", emoji = "🎨")
                    Spacer(Modifier.height(16.dp))
                    ThemeGrid(selectedTheme = selectedTheme, onThemeSelect = viewModel::selectTheme)
                }

                // ── SECCIÓN 2: TIPOGRAFÍA ─────────────────────────────────────
                item {
                    MinimalSectionHeader(title = "Tipografía", emoji = "🔤")
                    Spacer(Modifier.height(16.dp))
                    FontGrid(selectedFont = selectedFont, onFontSelect = viewModel::selectFont)
                }

                // ── SECCIÓN 3: PARTÍCULAS Y EFECTOS ───────────────────────────
                item {
                    MinimalSectionHeader(title = "Partículas y Efectos", emoji = "✨")
                    Spacer(Modifier.height(16.dp))
                    BackgroundEffectsSection(
                        config = backgroundConfig,
                        viewModel = viewModel
                    )
                }

                // ── SECCIÓN 4: SISTEMA ────────────────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    ResetBackgroundButton(onClick = viewModel::resetBackgroundToThemeDefault)
                }
            }
        }
    }
}

/**
 * Cabecera de sección minimalista sin cajas pesadas.
 */
@Composable
private fun MinimalSectionHeader(title: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Reversa,
            letterSpacing = (-0.5).sp
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// ── BACKGROUND EFFECTS SECTION ────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BackgroundEffectsSection(
    config: BackgroundConfig,
    viewModel: SettingsViewModel
) {
    val effects = remember(config) {
        listOf(
            EffectData("⭐", "Estrellas", config.stars.enabled, config.stars.intensity, viewModel::setStarsEnabled, viewModel::setStarsIntensity),
            EffectData("🌸", "Pétalos", config.petals.enabled, config.petals.intensity, viewModel::setPetalsEnabled, viewModel::setPetalsIntensity),
            EffectData("🌟", "Brillo", config.glow.enabled, config.glow.intensity, viewModel::setGlowEnabled, viewModel::setGlowIntensity),
            EffectData("🎞️", "Grain", config.grain.enabled, config.grain.intensity, viewModel::setGrainEnabled, viewModel::setGrainIntensity)
        )
    }

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CATÁLOGO DE PARTÍCULAS (Nuevo Diseño)
        ParticleCatalogCard(
            activeTypes = config.activeTypes,
            enabled     = config.particles.enabled,
            intensity   = config.particles.intensity,
            onToggle    = viewModel::setParticlesEnabled,
            onSlider    = viewModel::setParticlesIntensity,
            onTypeClick = viewModel::toggleParticleType
        )

        effects.forEach { effect ->
            EffectToggleCard(
                emoji = effect.emoji,
                title = effect.title,
                subtitle = "", 
                enabled = effect.enabled,
                intensity = effect.intensity,
                onToggle = effect.onToggle,
                onSlider = effect.onSlider
            )
        }

        // Velocidad global
        SpeedSliderCard(
            speed    = config.speed,
            onSlider = viewModel::setBackgroundSpeed
        )
    }
}

private data class EffectData(
    val emoji: String,
    val title: String,
    val enabled: Boolean,
    val intensity: Float,
    val onToggle: (Boolean) -> Unit,
    val onSlider: (Float) -> Unit
)


// ─────────────────────────────────────────────────────────────────────────────
// ── PARTICLE CATALOG CARD ─────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParticleCatalogCard(
    activeTypes: List<SpecialParticleType>,
    enabled:     Boolean,
    intensity:   Float,
    onToggle:    (Boolean) -> Unit,
    onSlider:    (Float) -> Unit,
    onTypeClick: (SpecialParticleType) -> Unit
) {
    val cardAlpha by animateFloatAsState(if (enabled) 1f else 0.5f, label = "alpha")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha }
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Sombra)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.Maldicion.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌌", fontSize = 20.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Catálogo de Partículas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)
                Text("Mezcla tus formas favoritas", fontSize = 11.sp, color = AppColors.Eco)
            }
            BgToggle(checked = enabled, onChecked = onToggle)
        }

        if (enabled) {
            // Slider
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Intensidad", fontSize = 11.sp, color = AppColors.Eco, modifier = Modifier.width(70.dp))
                    Slider(
                        value = intensity,
                        onValueChange = onSlider,
                        colors = SliderDefaults.colors(thumbColor = AppColors.Tecnica, activeTrackColor = AppColors.Maldicion)
                    )
                }
            }

            // Grid de Catálogo
            Text("Selecciona una o varias formas:", fontSize = 10.sp, color = AppColors.ReversaSuave, fontWeight = FontWeight.SemiBold)

            // Usamos un simple Row de LazyRow para que quepa en el LazyColumn madre
            // O una cuadrícula manual si son pocos. Hagamos un LazyRow de Grid para scroll horizontal
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecialParticleType.entries.forEach { type ->
                    val isSelected = activeTypes.contains(type)
                    ParticleCatalogItem(
                        type       = type,
                        isSelected = isSelected,
                        onClick    = { onTypeClick(type) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParticleCatalogItem(
    type: SpecialParticleType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(if (isSelected) AppColors.Maldicion.copy(alpha = 0.25f) else AppColors.Dominio, label = "bg")
    val borderColor by animateColorAsState(if (isSelected) AppColors.Maldicion else Color.Transparent, label = "border")
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(type.emoji, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = type.displayName,
            fontSize = 8.sp,
            color = if (isSelected) Color.White else AppColors.Eco,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── EFFECT TOGGLE CARD ────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EffectToggleCard(
    emoji:     String,
    title:     String,
    subtitle:  String,
    enabled:   Boolean,
    intensity: Float,
    onToggle:  (Boolean) -> Unit,
    onSlider:  (Float) -> Unit,
    content:   @Composable (() -> Unit)? = null
) {
    val cardAlpha by animateFloatAsState(
        targetValue   = if (enabled) 1f else 0.5f,
        animationSpec = tween(250),
        label         = "cardAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha }
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Sombra)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Fila superior: emoji + texto + toggle
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppColors.Reversa
                )
                Text(
                    text     = subtitle,
                    fontSize = 11.sp,
                    color    = AppColors.Eco,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            // Toggle personalizado
            BgToggle(
                checked   = enabled,
                onChecked = onToggle
            )
        }

        // Slider de intensidad — solo visible si está activado
        if (enabled) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text     = "Intensidad",
                    fontSize = 11.sp,
                    color    = AppColors.Eco,
                    modifier = Modifier.width(72.dp)
                )
                Slider(
                    value         = intensity,
                    onValueChange = onSlider,
                    valueRange    = 0f..1f,
                    modifier      = Modifier.weight(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor       = AppColors.Tecnica,
                        activeTrackColor = AppColors.Maldicion,
                        inactiveTrackColor = AppColors.Dominio
                    )
                )
                Text(
                    text     = "${(intensity * 100).toInt()}%",
                    fontSize = 11.sp,
                    color    = AppColors.KiEspiritual,
                    modifier = Modifier
                        .width(36.dp)
                        .padding(start = 8.dp)
                )
            }
            // Contenido adicional si existe
            content?.invoke()
        }
    }
}

// Extensiones para SpecialParticleType
private val SpecialParticleType.emoji: String get() = when(this) {
    SpecialParticleType.NONE -> "🔮"
    SpecialParticleType.SNOWFLAKE -> "❄️"
    SpecialParticleType.PETAL -> "🌸"
    SpecialParticleType.LEAF -> "🍃"
    SpecialParticleType.SPARKLE -> "✨"
    SpecialParticleType.SUN_RAY -> "☀️"
    SpecialParticleType.BUBBLE -> "🫧"
    SpecialParticleType.STAR_OUTLINE -> "⭐"
    SpecialParticleType.RAINDROP -> "💧"
    SpecialParticleType.FIREFLY -> "🏮"
    SpecialParticleType.EMBER -> "🔥"
    SpecialParticleType.CRYSTAL -> "💎"
    SpecialParticleType.HEART_S -> "❤️"
    SpecialParticleType.SQUARE_DOT -> "🟦"
    SpecialParticleType.SHINE_STARDUST -> "✨"
}

private val SpecialParticleType.displayName: String get() = when(this) {
    SpecialParticleType.NONE -> "Puntos"
    SpecialParticleType.SNOWFLAKE -> "Nieve"
    SpecialParticleType.PETAL -> "Pétalos"
    SpecialParticleType.LEAF -> "Hojas"
    SpecialParticleType.SPARKLE -> "Destellos"
    SpecialParticleType.SUN_RAY -> "Sol"
    SpecialParticleType.BUBBLE -> "Burbujas"
    SpecialParticleType.STAR_OUTLINE -> "Estrellas"
    SpecialParticleType.RAINDROP -> "Lluvia"
    SpecialParticleType.FIREFLY -> "Luz"
    SpecialParticleType.EMBER -> "Chispas"
    SpecialParticleType.CRYSTAL -> "Cristal"
    SpecialParticleType.HEART_S -> "Corazones"
    SpecialParticleType.SQUARE_DOT -> "Pixeles"
    SpecialParticleType.SHINE_STARDUST -> "Polvo de Oro"
}

// ─────────────────────────────────────────────────────────────────────────────
// ── SPEED SLIDER CARD ─────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpeedSliderCard(
    speed:    Float,
    onSlider: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Sombra)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌀", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text       = "Velocidad",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppColors.Reversa
                )
                Text(
                    text     = "Qué tan rápido se mueven los efectos",
                    fontSize = 11.sp,
                    color    = AppColors.Eco,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = "🐌",
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Slider(
                value         = speed,
                onValueChange = onSlider,
                valueRange    = 0.25f..2.0f,
                modifier      = Modifier.weight(1f),
                colors        = SliderDefaults.colors(
                    thumbColor         = AppColors.Tecnica,
                    activeTrackColor   = AppColors.Maldicion,
                    inactiveTrackColor = AppColors.Dominio
                )
            )
            Text(
                text     = "🐇",
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Etiqueta textual de velocidad
        val speedLabel = when {
            speed < 0.6f  -> "Muy lento"
            speed < 0.9f  -> "Lento"
            speed < 1.1f  -> "Normal"
            speed < 1.5f  -> "Rápido"
            else          -> "Muy rápido"
        }
        Text(
            text      = speedLabel,
            fontSize  = 11.sp,
            color     = AppColors.KiEspiritual,
            modifier  = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── BG TOGGLE ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BgToggle(
    checked:   Boolean,
    onChecked: (Boolean) -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue   = if (checked) AppColors.Maldicion else AppColors.Dominio,
        animationSpec = tween(250),
        label         = "trackColor"
    )
    val thumbOffset by animateFloatAsState(
        targetValue   = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "thumbOffset"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = { onChecked(!checked) }
            )
    ) {
        Box(
            modifier = Modifier
                .padding(3.dp)
                .size(20.dp)
                .offset { IntOffset((thumbOffset * 22.dp.toPx()).toInt(), 0) }
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── RESET BUTTON ──────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ResetBackgroundButton(onClick: () -> Unit) {
    Box(
        modifier          = Modifier.fillMaxWidth(),
        contentAlignment  = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.Dominio)
                .border(
                    width = 1.dp,
                    color = AppColors.Expansion.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onClick
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text      = "↺  Restaurar defaults del tema",
                fontSize  = 12.sp,
                color     = AppColors.Eco,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// ── HEADER ────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
    ) {
        // Botón volver
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Sombra)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("← Atrás", fontSize = 13.sp, color = AppColors.ReversaSuave)
        }

        // Título centrado
        Row(
            modifier             = Modifier.align(Alignment.Center),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⚙️", fontSize = 20.sp)
            Text(
                text       = "Ajustes",
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = AppColors.Reversa
            )
        }
    }
}



// ─────────────────────────────────────────────────────────────────────────────
// ── THEME GRID ────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ThemeGrid(
    selectedTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit
) {
    val themes = AppTheme.entries.toList()
    val pairs  = themes.chunked(2)   // Filas de 2 columnas

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pairs.forEach { pair ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { theme ->
                    ThemeCard(
                        modifier      = Modifier.weight(1f),
                        theme         = theme,
                        isSelected    = theme == selectedTheme,
                        onClick       = { onThemeSelect(theme) }
                    )
                }
                // Si la fila tiene solo 1 elemento, rellena el espacio
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── COLOR SWATCH ──────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// ── THEME CARD ────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ThemeCard(
    modifier:   Modifier,
    theme:      AppTheme,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val scheme = themeColorSchemeFor(theme)

    // Animación de escala al seleccionar
    val cardScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .aspectRatio(0.88f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        scheme.sombra,
                        scheme.dominio,
                        scheme.expansion
                    )
                )
            )
            .then(
                if (isSelected)
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(scheme.tecnica, scheme.kiEspiritual)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                else
                    Modifier.border(
                        width = 1.dp,
                        color = scheme.eco.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(18.dp)
                    )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Emoji grande
            Text(theme.emoji, fontSize = 30.sp)

            Spacer(Modifier.weight(1f))

            // Swatches de colores (mini preview)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ColorSwatch(color = scheme.maldicion)
                ColorSwatch(color = scheme.tecnica)
                ColorSwatch(color = scheme.kiEspiritual)
            }

            Spacer(Modifier.height(8.dp))

            // Nombre del tema
            Text(
                text       = " ${theme.displayName}",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = scheme.reversa
            )

            // Descripción
            Text(
                text     = theme.description,
                fontSize = 9.5.sp,
                color    = scheme.eco,
                lineHeight = 13.sp
            )
        }

        // ✓ Indicador de selección
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(scheme.maldicion, scheme.tecnica)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = "✓",
                    fontSize = 13.sp,
                    color    = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── COMING SOON CARD ──────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ComingSoonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Sombra)
            .padding(20.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🚧", fontSize = 32.sp)
            Text(
                text      = "Más opciones próximamente",
                fontSize  = 14.sp,
                color     = AppColors.Reversa,
                fontWeight = FontWeight.Medium,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "Notificaciones, tamaño de texto,\nsonidos y más están en camino.",
                fontSize  = 12.sp,
                color     = AppColors.Eco,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── FONT GRID ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FontGrid(
    selectedFont: AppFont,
    onFontSelect: (AppFont) -> Unit
) {
    val fonts = AppFont.values().toList()
    val pairs = fonts.chunked(2)

    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pairs.forEach { pair ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { font ->
                    FontCard(
                        modifier   = Modifier.weight(1f),
                        font       = font,
                        isSelected = font == selectedFont,
                        onClick    = { onFontSelect(font) }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── FONT CARD ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FontCard(
    modifier:   Modifier,
    font:       AppFont,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "fontCardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Sombra)
            .then(
                if (isSelected)
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(AppColors.Tecnica, AppColors.KiEspiritual)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                else
                    Modifier.border(
                        width = 1.dp,
                        color = AppColors.Eco.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(font.emoji, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))

            // Preview real con la tipografía de la opción
            Text(
                text       = font.displayName,
                fontFamily = fontFamilyFor(font),
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = AppColors.Reversa
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text       = font.description,
                fontSize   = 10.sp,
                color      = AppColors.Eco,
                lineHeight = 13.sp
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Maldicion, AppColors.Tecnica)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}