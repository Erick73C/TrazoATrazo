package com.example.trazoatrazo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tarjeta para el HomeScreen — representa una categoría (Flowers, Cartoons, Animals…)
 *
 * @param scale       Valor de animación de entrada (0f → 1f)
 * @param emoji       Emoji representativo de la categoría
 * @param title       Nombre de la categoría
 * @param subtitle    Descripción corta o cantidad de dibujos
 * @param bgColor     Color de fondo de la tarjeta
 * @param accentColor Color del título y la flecha
 * @param onClick     Acción al pulsar
 */
@Composable
fun MenuCard(
    scale:       Float,
    emoji:       String,
    title:       String,
    subtitle:    String,
    bgColor:     Color,
    accentColor: Color,
    onClick:     () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "pressScale"
    )

    Box(
        modifier = Modifier
            .scale(scale * pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable {
                pressed = true
                onClick()
            }
            .padding(vertical = 22.dp, horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono con emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 34.sp)
            }

            // Texto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
                Text(
                    text     = subtitle,
                    fontSize = 13.sp,
                    color    = Color(0xFF555555)
                )
            }

            // Flecha
            Text("▶", fontSize = 18.sp, color = accentColor)
        }
    }
}

/**
 * Tarjeta más pequeña para listar dibujos dentro de una categoría.
 *
 * @param emoji       Emoji o miniatura del dibujo
 * @param title       Nombre del dibujo
 * @param bgColor     Color de fondo
 * @param accentColor Color del título
 * @param onClick     Acción al pulsar
 */
@Composable
fun DrawingCard(
    emoji:       String,
    title:       String,
    bgColor:     Color  = Color(0xFFF5F5F5),
    accentColor: Color  = Color(0xFF333333),
    onClick:     () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "drawingPress"
    )

    Box(
        modifier = Modifier
            .scale(pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable {
                pressed = true
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 26.sp)
            }

            Text(
                text       = title,
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = accentColor,
                modifier   = Modifier.weight(1f)
            )

            Text("▶", fontSize = 15.sp, color = accentColor.copy(alpha = 0.6f))
        }
    }
}