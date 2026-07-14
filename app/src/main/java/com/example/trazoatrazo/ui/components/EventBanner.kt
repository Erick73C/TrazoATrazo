package com.example.trazoatrazo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.utils.textColorFor

/**
 * Tarjeta que aparece en la parte superior del Home mientras hay un
 * [SpecialEvent] activo (ver [com.example.trazoatrazo.utils.EventDetector]).
 *
 * Es puramente informativa/decorativa: no navega a ningún lado por defecto,
 * aunque acepta [onClick] opcional por si en el futuro se quiere que el
 * banner lleve directo al [SpecialEvent.temporaryDrawingId] cuando exista.
 *
 * No se persiste ni se puede "cerrar" — desaparece solo cuando la fecha
 * actual sale del rango del evento (comportamiento intencional según la
 * documentación: el evento "desaparece después para potenciar el valor
 * del recuerdo").
 */
@Composable
fun EventBanner(
    event:   SpecialEvent,
    onClick: (() -> Unit)? = null
) {
    val textColor = textColorFor(event.accentColor)

    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            event.accentColor.copy(alpha = 0.85f),
                            event.accentColor.copy(alpha = 0.55f)
                        )
                    )
                )
                .drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color        = event.accentColor.copy(alpha = 0.9f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style        = Stroke(width = 1.2f)
                    )
                }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onClick
                        )
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(event.bannerEmoji, fontSize = 22.sp)

                Text(
                    text       = event.bannerMessage,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = textColor,
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}