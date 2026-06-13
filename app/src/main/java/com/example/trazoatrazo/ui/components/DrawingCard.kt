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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.utils.subtitleColorFor
import com.example.trazoatrazo.utils.textColorFor

// ─────────────────────────────────────────────────────────────────────────────
// DrawingCard — v2.2  Componente unificado para HomeScreen y CategoryScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tarjeta de dibujo con estilo adaptado por categoría.
 * La categoría SPECIAL recibe tratamiento negro/dorado automáticamente.
 *
 * @param emoji          Emoji del dibujo
 * @param title          Nombre del dibujo
 * @param description    Subtítulo corto
 * @param categoryId     ID de la categoría (Routes.Category.*) — controla el estilo
 * @param accentColor    Color de acento (borde, barra, botón play)
 * @param bgColor        Color de fondo de la tarjeta
 * @param categoryLabel  Texto del badge (ej: "Flores")
 * @param isNew          Muestra badge "Nuevo"
 * @param onClick        Acción al pulsar
 */
// ── Colores fijos de la categoría Especial ────────────────────────────────────
private object SpecialColors {
    val Gold        = Color(0xFFD4A017)
    val GoldBg      = Color(0xFF0A0A0A)
    val GoldIcon    = Color(0xFFD4A017).copy(alpha = 0.12f)
    val GoldBorder  = Color(0xFFD4A017).copy(alpha = 0.45f)
    val GoldPlay    = Color(0xFFD4A017).copy(alpha = 0.15f)
    val GoldShimmer = Color(0xFFD4A017).copy(alpha = 0.09f)
    val GoldDesc    = Color(0xFF9A7A30)
    val Ornament    = Color(0xFFD4A017).copy(alpha = 0.5f)
}

@Composable
fun DrawingCard(
    emoji:         String,
    title:         String,
    description:   String  = "",
    categoryId:    String  = "",
    accentColor:   Color   = AppColors.Tecnica,
    categoryLabel: String  = "",
    isNew:         Boolean = false,
    onClick:       () -> Unit
) {
    val isSpecial = categoryId == Routes.Category.SPECIAL

    // Fondo reactivo al tema por categoría
    val cardBg = when (categoryId) {
        Routes.Category.FLOWERS  -> AppColors.FlowersBg
        Routes.Category.CARTOONS -> AppColors.CartoonsBg
        Routes.Category.ANIMALS  -> AppColors.AnimalsBg
        Routes.Category.SPRING   -> AppColors.SpringBg
        Routes.Category.WINTER   -> AppColors.WinterBg
        Routes.Category.SPECIAL  -> SpecialColors.GoldBg
        else                     -> AppColors.Sombra
    }

    // Colores derivados — solo se recalculan si cambia isSpecial, cardBg o accentColor
    val borderColor = if (isSpecial) SpecialColors.Gold        else accentColor
    val titleColor  = if (isSpecial) SpecialColors.Gold        else textColorFor(cardBg)
    val descColor   = if (isSpecial) SpecialColors.GoldDesc    else subtitleColorFor(cardBg)
    val iconBg      = if (isSpecial) SpecialColors.GoldIcon    else accentColor.copy(alpha = 0.16f)
    val iconBorder  = if (isSpecial) SpecialColors.GoldBorder  else accentColor.copy(alpha = 0.32f)
    val playBg      = if (isSpecial) SpecialColors.GoldPlay    else accentColor.copy(alpha = 0.18f)
    val playBorder  = if (isSpecial) SpecialColors.Gold        else accentColor.copy(alpha = 0.45f)
    val playColor   = if (isSpecial) SpecialColors.Gold        else accentColor
    val shimmerTint = if (isSpecial) SpecialColors.GoldShimmer else Color.White.copy(alpha = 0.07f)

    // Presión — vuelve a false cuando la animación termina
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        finishedListener = { pressed = false },   // ← reset automático
        label         = "press"
    )

    // Shimmer — una sola instancia por tarjeta, compartida en el scope de composición
    val shimmerTrans = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerTrans.animateFloat(
        initialValue  = -1.2f,
        targetValue   = 2.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = Modifier
            .scale(pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .drawWithCache {
                // Geometría calculada una sola vez mientras el tamaño no cambie
                val cornerR   = CornerRadius(18.dp.toPx())
                val barTop    = 8.dp.toPx()
                val barHeight = size.height - 16.dp.toPx()
                val borderW   = if (isSpecial) 1.5f else 1.2f
                val borderA   = if (isSpecial) 1f   else 0.42f

                // Brush del gradiente de barra — fijo mientras accentColor no cambie
                val barBrush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.35f)),
                    start  = Offset(0f, 0f),
                    end    = Offset(0f, size.height)
                )

                onDrawWithContent {
                    drawContent()

                    // Borde exterior
                    drawRoundRect(
                        color        = borderColor.copy(alpha = borderA),
                        cornerRadius = cornerR,
                        style        = Stroke(width = borderW)
                    )

                    // Barra de acento izquierda
                    drawRoundRect(
                        brush        = barBrush,
                        topLeft      = Offset(0f, barTop),
                        size         = Size(3.5f, barHeight),
                        cornerRadius = CornerRadius(4f)
                    )

                    // Shimmer — leído dentro del draw scope (no fuerza recomposición)
                    val sx = size.width * shimmerX
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, shimmerTint, Color.Transparent),
                            start  = Offset(sx, 0f),
                            end    = Offset(sx + size.width * 0.55f, size.height)
                        )
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) {
                pressed = true
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            // ── Ícono ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg)
                    .drawWithCache {
                        val cornerR     = CornerRadius(13.dp.toPx())
                        val innerCorner = CornerRadius(11.dp.toPx())
                        val reflectH    = size.height * 0.45f
                        val reflectBrush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.13f), Color.Transparent),
                            start  = Offset(0f, 0f),
                            end    = Offset(0f, reflectH)
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRoundRect(color = iconBorder, cornerRadius = cornerR, style = Stroke(1f))
                            drawRoundRect(
                                brush        = reflectBrush,
                                topLeft      = Offset(2f, 2f),
                                size         = Size(size.width - 4f, reflectH),
                                cornerRadius = innerCorner
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 25.sp)
            }

            // ── Textos ────────────────────────────────────────────────────
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text      = title,
                    fontSize  = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color     = titleColor,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )

                if (description.isNotEmpty()) {
                    Text(
                        text     = description,
                        fontSize = 11.sp,
                        color    = descColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (categoryLabel.isNotEmpty() || isNew) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier              = Modifier.padding(top = 3.dp)
                    ) {
                        if (categoryLabel.isNotEmpty()) {
                            DrawingBadge(label = categoryLabel, accentColor = borderColor)
                        }
                        if (isNew) {
                            DrawingBadge(label = "Nuevo", accentColor = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // ── Botón play ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(playBg)
                    .drawWithCache {
                        val cornerR = CornerRadius(10.dp.toPx())
                        val strokeW = if (isSpecial) 1.3f else 1f
                        onDrawWithContent {
                            drawContent()
                            drawRoundRect(color = playBorder, cornerRadius = cornerR, style = Stroke(strokeW))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(11.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.22f, 0f)
                        lineTo(size.width,         size.height * 0.5f)
                        lineTo(size.width * 0.22f, size.height)
                        close()
                    }
                    drawPath(path, color = playColor)
                }
            }
        }

        // Ornamento dorado — solo categoría Especial
        if (isSpecial) {
            Text(
                text     = "✦",
                fontSize = 10.sp,
                color    = SpecialColors.Ornament,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

// ── Badge ─────────────────────────────────────────────────────────────────────
@Composable
private fun DrawingBadge(label: String, accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .drawWithCache {
                val cornerR = CornerRadius(5.dp.toPx())
                onDrawWithContent {
                    drawContent()
                    drawRoundRect(
                        color        = accentColor.copy(alpha = 0.38f),
                        cornerRadius = cornerR,
                        style        = Stroke(0.8f)
                    )
                }
            }
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text          = label.uppercase(),
            fontSize      = 8.5.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = accentColor.copy(alpha = 0.88f),
            letterSpacing = 0.5.sp
        )
    }
}