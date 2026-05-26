package com.example.trazoatrazo.presentation.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.*

// ── SettingsScreen ────────────────────────────────────────────────────────────
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onBack:    () -> Unit
) {
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()

    // Animación de entrada
    val screenAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnim.animateTo(1f, tween(550, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        // ── Fondo decorativo ──────────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.Maldicion.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.08f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.15f, size.height * 0.08f)
            )
        }

        LazyColumn(
            modifier        = Modifier
                .fillMaxSize()
                .alpha(screenAnim.value)
                .offset(y = ((1f - screenAnim.value) * 30).dp),
            contentPadding  = PaddingValues(bottom = 60.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                SettingsHeader(onBack = onBack)
                HorizontalDivider(
                    color     = AppColors.Maldicion.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Sección: Tema ─────────────────────────────────────────────────
            item {
                SectionTitle(
                    emoji = "🎨",
                    title = "Tema de la app",
                    subtitle = "Elige el estilo visual · Se guarda automáticamente"
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Grid 2×N de temas ─────────────────────────────────────────────
            item {
                ThemeGrid(
                    selectedTheme = selectedTheme,
                    onThemeSelect = viewModel::selectTheme
                )
            }

            // ── Sección: Próximamente ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(32.dp))
                SectionTitle(
                    emoji    = "⚙️",
                    title    = "Más ajustes",
                    subtitle = "Próximamente más opciones aquí"
                )
                Spacer(Modifier.height(16.dp))
                ComingSoonCard()
            }
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
// ── SECTION TITLE ─────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionTitle(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(
                text       = title,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = AppColors.Reversa
            )
        }
        Text(
            text     = subtitle,
            fontSize = 11.sp,
            color    = AppColors.Eco,
            modifier = Modifier.padding(top = 3.dp, start = 26.dp)
        )
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
    val themes = AppTheme.values().toList()
    val pairs  = themes.chunked(2)   // Filas de 2 columnas

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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

    // Animación de borde al seleccionar
    val borderAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label         = "borderAlpha"
    )
    val cardScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "cardScale"
    )

    Box(
        modifier = modifier
            .scale(cardScale)
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
                text       = theme.displayName,
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
// ── COMING SOON CARD ──────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ComingSoonCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
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