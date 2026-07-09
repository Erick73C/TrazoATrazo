package com.example.trazoatrazo.presentation.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack:    () -> Unit
) {
    val selectedTheme    by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val backgroundConfig by viewModel.backgroundConfig.collectAsStateWithLifecycle()
    val selectedFont     by viewModel.selectedFont.collectAsStateWithLifecycle()
    val selectedMessageStyle by viewModel.selectedMessageStyle.collectAsStateWithLifecycle()
    val immersiveMode    by viewModel.immersiveMode.collectAsStateWithLifecycle()

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
                // ── SECCIÓN TEMAS ──
                item {
                    MinimalSectionHeader(title = "Temas", emoji = "🎨")
                    Spacer(Modifier.height(16.dp))
                    ThemeRow(selectedTheme = selectedTheme, onThemeSelect = viewModel::selectTheme)
                }

                // ── SECCIÓN EFECTOS (REDISEÑO FINAL) ──
                item {
                    MinimalSectionHeader(title = "Fondo y Efectos", emoji = "✨")
                    Spacer(Modifier.height(16.dp))
                    BackgroundEffectsSectionDashboard(
                        config = backgroundConfig,
                        viewModel = viewModel
                    )
                }

                // ── SECCIÓN TIPOGRAFÍA ──
                item {
                    MinimalSectionHeader(title = "Tipografía", emoji = "🔤")
                    Spacer(Modifier.height(16.dp))
                    FontRow(selectedFont = selectedFont, onFontSelect = viewModel::selectFont)
                    
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Estilo de mensajes de Inicio",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Eco,
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    )
                    MessageStyleRow(
                        selectedStyle = selectedMessageStyle,
                        onStyleSelect = viewModel::selectMessageStyle
                    )
                }

                item {
                    MinimalSectionHeader(title = "Sistema", emoji = "⚙️")
                    Spacer(Modifier.height(16.dp))
                    SystemSettingsSection(
                        immersiveMode = immersiveMode,
                        onImmersiveModeChange = viewModel::setImmersiveMode
                    )
                    Spacer(Modifier.height(24.dp))
                    ResetBackgroundButton(onClick = viewModel::resetBackgroundToThemeDefault)
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    ComingSoonCard()
                }
            }
        }
    }
}

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

// ── REDISEÑO DE EFECTOS: DASHBOARD COMPACTO ──────────────────────────────────

@Composable
private fun BackgroundEffectsSectionDashboard(
    config: BackgroundConfig,
    viewModel: SettingsViewModel
) {
    val effects = remember(config) {
        listOf(
            EffectData("⭐", "Estrellas", "stars", config.stars.enabled, config.stars.intensity, viewModel::setStarsEnabled, viewModel::setStarsIntensity),
            EffectData("🌟", "Brillo", "glow", config.glow.enabled, config.glow.intensity, viewModel::setGlowEnabled, viewModel::setGlowIntensity),
            EffectData("🎞️", "Grain", "grain", config.grain.enabled, config.grain.intensity, viewModel::setGrainEnabled, viewModel::setGrainIntensity),
            EffectData("🎭", "Viñeta", "vignette", config.vignette.enabled, config.vignette.intensity, viewModel::setVignetteEnabled, viewModel::setVignetteIntensity),
            EffectData("🌈", "Aberración", "chromatic", config.chromatic.enabled, config.chromatic.intensity, viewModel::setChromaticEnabled, viewModel::setChromaticIntensity),
            EffectData("📺", "Retro TV", "scanlines", config.scanlines.enabled, config.scanlines.intensity, viewModel::setScanlinesEnabled, viewModel::setScanlinesIntensity),
            EffectData("🌀", "Espejo", "kaleidoscope", config.kaleidoscope.enabled, config.kaleidoscope.intensity, viewModel::setKaleidoscopeEnabled, viewModel::setKaleidoscopeIntensity),
            EffectData("🌊", "Ondas", "waves", config.waves.enabled, config.waves.intensity, viewModel::setWavesEnabled, viewModel::setWavesIntensity)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // 1. Partículas (Card Principal)
        ParticleCatalogCard(
            activeTypes = config.activeTypes,
            enabled     = config.particles.enabled,
            intensity   = config.particles.intensity,
            onToggle    = viewModel::setParticlesEnabled,
            onSlider    = viewModel::setParticlesIntensity,
            onTypeClick = viewModel::toggleParticleType,
            particleSize = config.particleSize,
            onSizeSlider = viewModel::setParticleSize
        )

        // 2. Lista de Efectos en una sola tarjeta elegante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.Sombra)
                .padding(vertical = 8.dp)
        ) {
            Column {
                effects.forEachIndexed { index, effect ->
                    EffectCompactRow(
                        effect = effect,
                        showDivider = index < effects.lastIndex
                    )
                }
            }
        }

        // 3. Velocidad Global
        SpeedSliderCard(speed = config.speed, onSlider = viewModel::setBackgroundSpeed)
    }
}

@Composable
private fun EffectCompactRow(
    effect: EffectData,
    showDivider: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(effect.emoji, fontSize = 20.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = effect.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (effect.enabled) Color.White else AppColors.Eco
                )
                if (effect.enabled) {
                    Text("Intensidad: ${(effect.intensity * 100).toInt()}%", fontSize = 10.sp, color = AppColors.Tecnica)
                }
            }
            BgToggle(checked = effect.enabled, onChecked = effect.onToggle)
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = expanded && effect.enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Slider(
                    value = effect.intensity,
                    onValueChange = effect.onSlider,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.Tecnica,
                        activeTrackColor = AppColors.Maldicion
                    )
                )
            }
        }
        
        if (showDivider) {
            HorizontalDivider(color = AppColors.Dominio.copy(alpha = 0.5f), thickness = 0.5.dp)
        }
    }
}

// ── COMPONENTES REVERTIDOS A FILAS (LazyRow) ────────────────────────────────

@Composable
private fun ThemeRow(selectedTheme: AppTheme, onThemeSelect: (AppTheme) -> Unit) {
    val themes = AppTheme.entries.toList()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(themes) { theme ->
            ThemeCard(
                modifier = Modifier.width(160.dp),
                theme = theme,
                isSelected = theme == selectedTheme,
                onClick = { onThemeSelect(theme) }
            )
        }
    }
}

@Composable
private fun FontRow(selectedFont: AppFont, onFontSelect: (AppFont) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(AppFont.entries) { font ->
            FontCard(
                modifier = Modifier.width(180.dp),
                font = font,
                isSelected = font == selectedFont,
                onClick = { onFontSelect(font) }
            )
        }
    }
}

@Composable
private fun MessageStyleRow(selectedStyle: MessageStyle, onStyleSelect: (MessageStyle) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(MessageStyle.entries) { style ->
            MessageStyleCard(
                modifier = Modifier.width(130.dp),
                style = style,
                isSelected = style == selectedStyle,
                onClick = { onStyleSelect(style) }
            )
        }
    }
}

// ── CARDS INDIVIDUALES (ESTILO LIMPIO) ────────────────────────────────────────

@Composable
private fun ThemeCard(modifier: Modifier, theme: AppTheme, isSelected: Boolean, onClick: () -> Unit) {
    val scheme = themeColorSchemeFor(theme)
    Box(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.maldicion.copy(alpha = 0.8f),
                        scheme.dominio
                    )
                )
            )
            .border(
                2.5.dp, 
                if (isSelected) scheme.tecnica else Color.White.copy(alpha = 0.1f), 
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(theme.emoji, fontSize = 28.sp)
            Spacer(Modifier.weight(1f))
            Text(
                theme.displayName, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = Color.White
            )
            Text(
                theme.description, 
                fontSize = 10.sp, 
                color = Color.White.copy(alpha = 0.7f), 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FontCard(modifier: Modifier, font: AppFont, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(85.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Sombra)
            .border(2.dp, if (isSelected) AppColors.Tecnica else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(font.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = font.displayName, 
                    fontFamily = fontFamilyFor(font), 
                    fontSize = 17.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = AppColors.Reversa,
                    maxLines = 1
                )
                Text(
                    text = font.description, 
                    fontSize = 10.sp, 
                    color = AppColors.Eco,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MessageStyleCard(modifier: Modifier, style: MessageStyle, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Sombra)
            .border(2.dp, if (isSelected) AppColors.Tecnica else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(style.displayName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White else AppColors.Eco)
    }
}

// ── COMPONENTES AUXILIARES ───────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParticleCatalogCard(
    activeTypes: List<SpecialParticleType>,
    enabled:     Boolean,
    intensity:   Float,
    onToggle:    (Boolean) -> Unit,
    onSlider:    (Float) -> Unit,
    onTypeClick: (SpecialParticleType) -> Unit,
    particleSize: Float,
    onSizeSlider: (Float) -> Unit
) {
    val cardAlpha by animateFloatAsState(if (enabled) 1f else 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha }
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.Sombra)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(AppColors.Maldicion.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("🌌", fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Partículas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)
                Text("Estilo y comportamiento", fontSize = 11.sp, color = AppColors.Eco)
            }
            BgToggle(checked = enabled, onChecked = onToggle)
        }

        if (enabled) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cantidad", fontSize = 11.sp, color = AppColors.Eco, modifier = Modifier.width(70.dp))
                    Slider(value = intensity, onValueChange = onSlider, colors = SliderDefaults.colors(thumbColor = AppColors.Tecnica, activeTrackColor = AppColors.Maldicion))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tamaño", fontSize = 11.sp, color = AppColors.Eco, modifier = Modifier.width(70.dp))
                    Slider(value = particleSize, onValueChange = onSizeSlider, valueRange = 0.5f..2.5f, colors = SliderDefaults.colors(thumbColor = AppColors.KiEspiritual, activeTrackColor = AppColors.Maldicion))
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecialParticleType.entries.forEach { type ->
                    val isSelected = activeTypes.contains(type)
                    ParticleCatalogItem(type = type, isSelected = isSelected, onClick = { onTypeClick(type) })
                }
            }
        }
    }
}

@Composable
private fun ParticleCatalogItem(type: SpecialParticleType, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(if (isSelected) AppColors.Maldicion.copy(alpha = 0.25f) else AppColors.Dominio)
    val borderColor by animateColorAsState(if (isSelected) AppColors.Maldicion else Color.Transparent)

    Column(
        modifier = Modifier
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
        Text(type.displayName, fontSize = 8.sp, color = if (isSelected) Color.White else AppColors.Eco, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SpeedSliderCard(speed: Float, onSlider: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(AppColors.Sombra).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌀", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Velocidad Global", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)
                Text("Ritmo de la animación", fontSize = 11.sp, color = AppColors.Eco)
            }
        }
        Slider(
            value = speed, 
            onValueChange = onSlider, 
            valueRange = 0.25f..2.0f,
            colors = SliderDefaults.colors(thumbColor = AppColors.Tecnica, activeTrackColor = AppColors.Maldicion)
        )
    }
}

@Composable
private fun BgToggle(checked: Boolean, onChecked: (Boolean) -> Unit) {
    Switch(
        checked = checked, 
        onCheckedChange = onChecked,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AppColors.Maldicion,
            uncheckedThumbColor = AppColors.Eco,
            uncheckedTrackColor = AppColors.Dominio
        )
    )
}

@Composable
private fun SystemSettingsSection(immersiveMode: Boolean, onImmersiveModeChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(AppColors.Sombra).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📱", fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Modo Inmersivo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)
            Text("Ocultar barras de sistema", fontSize = 11.sp, color = AppColors.Eco)
        }
        BgToggle(checked = immersiveMode, onChecked = onImmersiveModeChange)
    }
}

@Composable
private fun ResetBackgroundButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Dominio)
    ) {
        Text("Restaurar defaults del tema", color = AppColors.Eco, fontSize = 12.sp)
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).clip(RoundedCornerShape(12.dp)).background(AppColors.Sombra)) {
            Text("←", color = AppColors.Reversa)
        }
        Text("Ajustes", modifier = Modifier.align(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Reversa)
    }
}

private data class EffectData(
    val emoji: String,
    val title: String,
    val id: String,
    val enabled: Boolean,
    val intensity: Float,
    val onToggle: (Boolean) -> Unit,
    val onSlider: (Float) -> Unit
)

@Composable
private fun ComingSoonCard() {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(AppColors.Sombra.copy(alpha = 0.5f)).padding(20.dp)) {
        Text("Próximamente: Sonidos, Notificaciones y más...", fontSize = 11.sp, color = AppColors.Eco, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

// Extensiones útiles para SpecialParticleType
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
    SpecialParticleType.WAVE -> "〰️"
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
    SpecialParticleType.SHINE_STARDUST -> "Polvo"
    SpecialParticleType.WAVE -> "Ondas"
}
