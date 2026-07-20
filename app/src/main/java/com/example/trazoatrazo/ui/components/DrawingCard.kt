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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.LocalUiTransparency
import com.example.trazoatrazo.utils.subtitleColorFor
import com.example.trazoatrazo.utils.textColorFor

// ─────────────────────────────────────────────────────────────────────────────
// DrawingCard — v2.4  Componente unificado para HomeScreen y CategoryScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tarjeta de dibujo con estilo adaptado por categoría.
 * La categoría SPECIAL recibe tratamiento negro/dorado automáticamente.
 *
 * Soporta dos sistemas de bloqueo independientes, que **no dependen entre
 * sí** y pueden convivir en el mismo dibujo (aunque en la práctica cada
 * `DrawingItem` normalmente solo usa uno):
 *
 * - **Cápsula del Tiempo** ([isCapsuleLocked]): bloqueo por fecha fija,
 *   ícono 🔒, ver [com.example.trazoatrazo.utils.CapsuleUtils].
 * - **Racha de uso** ([isRaceLocked]): bloqueo por días distintos abiertos,
 *   ícono ⏳, ver [com.example.trazoatrazo.utils.UnlockUtils].
 *
 * Si ambos son `true` a la vez, la cápsula tiene prioridad visual (fecha
 * fija es una restricción más "dura" que la racha).
 *
 * @param emoji                 Emoji del dibujo
 * @param title                 Nombre del dibujo
 * @param description           Subtítulo corto
 * @param categoryId            ID de la categoría (Routes.Category.*) — controla el estilo
 * @param accentColor           Color de acento (borde, barra, botón play)
 * @param categoryLabel         Texto del badge (ej: "Flores")
 * @param isNew                 Muestra badge "Nuevo"
 * @param isCapsuleLocked       true si el dibujo pertenece a una Cápsula del Tiempo sin desbloquear
 * @param capsuleDaysRemaining  Días restantes para el desbloqueo de la cápsula
 * @param isRaceLocked          true si el dibujo requiere más racha de días abiertos
 * @param raceDaysRemaining     Días de racha que faltan para desbloquearlo
 * @param onClick               Acción al pulsar cuando el dibujo está disponible
 * @param onLockedClick         Acción al pulsar cuando está bloqueado (por cualquiera de los dos sistemas)
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

// ── Colores fijos del estado "bloqueado por cápsula" ──────────────────────────
private object CapsuleLockColors {
    val Border = Color(0xFFD4A017).copy(alpha = 0.35f)
    val Icon   = Color(0xFFD4A017).copy(alpha = 0.10f)
    val Text   = Color(0xFFB8A57A)
}

// ── Colores fijos del estado "bloqueado por racha de uso" ─────────────────────
private object RaceLockColors {
    val Border = Color(0xFF7B6A9A).copy(alpha = 0.35f)
    val Icon   = Color(0xFF7B6A9A).copy(alpha = 0.12f)
    val Text   = Color(0xFF9E8FBF)
}

@Composable
fun DrawingCard(
    emoji:                 String,
    title:                 String,
    description:           String  = "",
    categoryId:            String  = "",
    accentColor:           Color   = AppColors.Tecnica,
    categoryLabel:         String  = "",
    isNew:                 Boolean = false,
    isCapsuleLocked:       Boolean = false,
    capsuleDaysRemaining:  Long    = 0L,
    isRaceLocked:          Boolean = false,
    raceDaysRemaining:     Int     = 0,
    onClick:               () -> Unit,
    onLockedClick:         () -> Unit = {}
) {
    val isSpecial = categoryId == Routes.Category.SPECIAL
    val transparency = LocalUiTransparency.current

    // Bloqueo efectivo: cualquiera de los dos sistemas activa el estado bloqueado
    val isLocked = isCapsuleLocked || isRaceLocked
    // La cápsula manda si ambos aplican (restricción "más dura")
    val lockIcon = if (isCapsuleLocked) "🔒" else "⏳"

    // Fondo reactivo al tema por categoría
    val cardBg = when (categoryId) {
        Routes.Category.SPECIAL -> SpecialColors.GoldBg
        else -> {
            val base = AppColors.Sombra
            val accent = when (categoryId) {
                Routes.Category.FLOWERS  -> AppColors.FlowersAccent
                Routes.Category.CARTOONS -> AppColors.CartoonsAccent
                Routes.Category.ANIMALS  -> AppColors.AnimalsAccent
                Routes.Category.SPRING   -> AppColors.SpringAccent
                Routes.Category.WINTER   -> AppColors.WinterAccent
                else -> Color.Transparent
            }
            if (accent != Color.Transparent) lerp(base, accent, 0.08f) else base
        }
    }.copy(alpha = (1f - transparency).coerceIn(0f, 1f))

    // Paleta de bloqueo activa según qué sistema aplica
    val lockBorder = if (isCapsuleLocked) CapsuleLockColors.Border else RaceLockColors.Border
    val lockIconBg  = if (isCapsuleLocked) CapsuleLockColors.Icon   else RaceLockColors.Icon
    val lockText    = if (isCapsuleLocked) CapsuleLockColors.Text   else RaceLockColors.Text

    // Colores derivados
    val borderColor = if (isLocked) lockBorder
    else if (isSpecial) SpecialColors.Gold else accentColor
    val titleColor  = if (isLocked) lockText
    else if (isSpecial) SpecialColors.Gold else textColorFor(cardBg)
    val descColor   = if (isLocked) lockText.copy(alpha = 0.75f)
    else if (isSpecial) SpecialColors.GoldDesc else subtitleColorFor(cardBg)
    val iconBg      = if (isLocked) lockIconBg
    else if (isSpecial) SpecialColors.GoldIcon else accentColor.copy(alpha = 0.16f)
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
        finishedListener = { pressed = false },
        label         = "press"
    )

    // Shimmer — se desactiva mientras está bloqueada
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
                val cornerR   = CornerRadius(18.dp.toPx())
                val barTop    = 8.dp.toPx()
                val barHeight = size.height - 16.dp.toPx()
                val borderW   = if (isSpecial) 1.5f else 1.2f
                val borderA   = if (isLocked) {
                    (0.6f + (0.4f * transparency)).coerceIn(0f, 1f)
                } else if (isSpecial) {
                    (1f - (0.3f * transparency)).coerceIn(0f, 1f)
                } else {
                    (0.42f + (0.38f * transparency)).coerceIn(0f, 1f)
                }

                val barBrush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = (1f - transparency * 0.5f).coerceIn(0f, 1f)),
                        borderColor.copy(alpha = (0.35f - transparency * 0.2f).coerceIn(0f, 1f))
                    ),
                    start  = Offset(0f, 0f),
                    end    = Offset(0f, size.height)
                )

                onDrawWithContent {
                    drawContent()

                    drawRoundRect(
                        color        = borderColor.copy(alpha = borderA),
                        cornerRadius = cornerR,
                        style        = Stroke(width = borderW)
                    )

                    drawRoundRect(
                        brush        = barBrush,
                        topLeft      = Offset(0f, barTop),
                        size         = Size(3.5f, barHeight),
                        cornerRadius = CornerRadius(4f)
                    )

                    if (!isLocked) {
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
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) {
                pressed = true
                if (isLocked) onLockedClick() else onClick()
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
                    .background(iconBg.copy(alpha = (iconBg.alpha * (1f - transparency)).coerceIn(0f, 1f)))
                    .drawWithCache {
                        val cornerR     = CornerRadius(13.dp.toPx())
                        val innerCorner = CornerRadius(11.dp.toPx())
                        val reflectH    = size.height * 0.45f
                        val reflectBrush = Brush.linearGradient(
                            colors = listOf(Color.White.copy(alpha = 0.13f * (1f - transparency)), Color.Transparent),
                            start  = Offset(0f, 0f),
                            end    = Offset(0f, reflectH)
                        )
                        onDrawWithContent {
                            drawContent()
                            val strokeA = if (transparency > 0f) 0.3f * transparency else iconBorder.alpha
                            drawRoundRect(color = iconBorder.copy(alpha = strokeA.coerceIn(0f, 1f)), cornerRadius = cornerR, style = Stroke(1f))
                            if (!isLocked) {
                                drawRoundRect(
                                    brush        = reflectBrush,
                                    topLeft      = Offset(2f, 2f),
                                    size         = Size(size.width - 4f, reflectH),
                                    cornerRadius = innerCorner
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isLocked) lockIcon else emoji, fontSize = 25.sp)
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

                // Descripción normal, o el aviso del sistema de bloqueo que aplique
                val shownDescription = when {
                    isCapsuleLocked -> when {
                        capsuleDaysRemaining <= 0L -> "Disponible pronto"
                        capsuleDaysRemaining == 1L -> "Disponible en 1 día"
                        else -> "Disponible en $capsuleDaysRemaining días"
                    }
                    isRaceLocked -> when {
                        raceDaysRemaining <= 0 -> "¡Casi lo desbloqueas!"
                        raceDaysRemaining == 1 -> "Se desbloquea en 1 día más"
                        else -> "Se desbloquea en $raceDaysRemaining días más"
                    }
                    else -> description
                }

                if (shownDescription.isNotEmpty()) {
                    Text(
                        text     = shownDescription,
                        fontSize = 11.sp,
                        color    = descColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if ((categoryLabel.isNotEmpty() || isNew) && !isLocked) {
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

            // Botón play (se oculta si está bloqueada por cualquiera de los 2 sistemas)
            if (!isLocked) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(playBg.copy(alpha = (playBg.alpha * (1f - transparency)).coerceIn(0f, 1f)))
                        .drawWithCache {
                            val cornerR = CornerRadius(10.dp.toPx())
                            val strokeW = if (isSpecial) 1.3f else 1f
                            val strokeA = if (transparency > 0f) 0.4f * transparency else playBorder.alpha
                            onDrawWithContent {
                                drawContent()
                                drawRoundRect(color = playBorder.copy(alpha = strokeA.coerceIn(0f, 1f)), cornerRadius = cornerR, style = Stroke(strokeW))
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
            } else {
                Text(lockIcon, fontSize = 16.sp, color = lockText)
            }
        }

        // Ornamento dorado — solo categoría Especial, y solo si no está bloqueada
        if (isSpecial && !isLocked) {
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