package com.example.trazoatrazo.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.data.DrawingItem
import com.example.trazoatrazo.ui.theme.AppColors
import okhttp3.internal.platform.android.AndroidLogHandler.close
import kotlin.io.path.Path
import kotlin.io.path.moveTo

// ─────────────────────────────────────────────────────────────────────────────
// DrawingCard — v2.2  Componente unificado para HomeScreen y CategoryScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @param emoji       Emoji del dibujo
 * @param title       Nombre del dibujo
 * @param description Subtítulo corto
 * @param accentColor Color de acento de la categoría (barra lateral + borde + botón)
 * @param bgGradientStart  Color de inicio del gradiente de fondo (default: AppColors.Dominio)
 * @param bgGradientEnd    Color de fin del gradiente de fondo (default: AppColors.Expansion)
 * @param categoryLabel    Badge de categoría opcional ej: "Flores"
 * @param isNew            Muestra un badge "Nuevo" adicional
 * @param onClick     Acción al pulsar
 */
@Composable
fun DrawingCard(
    emoji:             String,
    title:             String,
    description:       String  = "",
    accentColor:       Color   = AppColors.Tecnica,
    bgGradientStart:   Color   = AppColors.Dominio,
    bgGradientEnd:     Color   = AppColors.Expansion,
    categoryLabel:     String  = "",
    isNew:             Boolean = false,
    onClick:           () -> Unit
) {
    // Animación de presión
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "press"
    )

    // Shimmer que recorre la tarjeta continuamente
    val shimmerAnim = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerAnim.animateFloat(
        initialValue  = -1f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = Modifier
            .scale(pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            // Fondo gradiente oscuro
            .background(
                Brush.linearGradient(
                    colors = listOf(bgGradientStart, bgGradientEnd),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            // Borde con el color de acento
            .drawWithContent {
                drawContent()
                // Borde exterior
                drawRoundRect(
                    color        = accentColor.copy(alpha = 0.45f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                    style        = Stroke(1.2f)
                )
                // Barra de acento izquierda
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.4f)),
                        start  = Offset(0f, 0f),
                        end    = Offset(0f, size.height)
                    ),
                    topLeft      = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size         = androidx.compose.ui.geometry.Size(3.5f, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
                // Shimmer
                val shimmerStartX = size.width * shimmerX
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            accentColor.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerStartX, 0f),
                        end   = Offset(shimmerStartX + size.width * 0.5f, size.height)
                    )
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) {
                pressed = true
                onClick()
            }
            .padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 13.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Ícono con brillo interior ────────────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .drawWithContent {
                        drawContent()
                        // Borde del ícono
                        drawRoundRect(
                            color        = accentColor.copy(alpha = 0.35f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                            style        = Stroke(1f)
                        )
                        // Brillo superior (pseudo-reflejo)
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end   = Offset(0f, size.height * 0.45f)
                            ),
                            topLeft      = androidx.compose.ui.geometry.Offset(2f, 2f),
                            size         = androidx.compose.ui.geometry.Size(size.width - 4f, size.height * 0.45f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }

            // ── Texto ────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text       = title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppColors.Reversa,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                if (description.isNotEmpty()) {
                    Text(
                        text     = description,
                        fontSize = 11.sp,
                        color    = AppColors.Eco,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badges: categoría + "Nuevo"
                if (categoryLabel.isNotEmpty() || isNew) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        if (categoryLabel.isNotEmpty()) {
                            CategoryBadge(
                                label = categoryLabel,
                                color = accentColor
                            )
                        }
                        if (isNew) {
                            CategoryBadge(
                                label = "Nuevo",
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            // ── Botón play ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.22f))
                    .drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color        = accentColor.copy(alpha = 0.5f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                            style        = Stroke(1f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Triángulo play manual (sin dependencia de íconos externos)
                androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.25f, 0f)
                        lineTo(size.width,         size.height * 0.5f)
                        lineTo(size.width * 0.25f, size.height)
                        close()
                    }
                    drawPath(path, color = accentColor)
                }
            }
        }
    }
}

// ── Badge de categoría ────────────────────────────────────────────────────────
@Composable
private fun CategoryBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color        = color.copy(alpha = 0.4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                    style        = Stroke(0.8f)
                )
            }
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text      = label.uppercase(),
            fontSize  = 8.5.sp,
            fontWeight = FontWeight.SemiBold,
            color     = color.copy(alpha = 0.85f),
            letterSpacing = 0.5.sp
        )
    }
}
