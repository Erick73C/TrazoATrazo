package com.example.trazoatrazo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.utils.textColorFor
import androidx.compose.ui.geometry.Offset

@Composable
fun EventInfoBox(
    event: SpecialEvent,
    onDismiss: () -> Unit
) {
    val textColor = textColorFor(event.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(AppColors.Sombra)
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        listOf(event.accentColor, event.accentColor.copy(alpha = 0.2f))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .clickable(enabled = false) { }
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Halo de brillo detrás del emoji
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(event.accentColor.copy(alpha = 0.4f), Color.Transparent),
                                center = center,
                                radius = size.width / 1.2f
                            )
                        )
                    }
            ) {
                Text(event.bannerEmoji, fontSize = 64.sp)
            }
            
            Spacer(Modifier.height(20.dp))
            
            Text(
                text = event.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = event.accentColor,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = event.bannerMessage,
                fontSize = 15.sp,
                color = AppColors.ReversaSuave,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Recuadro de duración mejorado
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(event.accentColor.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = event.accentColor.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "📅 " + event.durationText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = event.accentColor
                )
            }
            
            Spacer(Modifier.height(36.dp))
            
            // Botón de cerrar con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(event.accentColor, event.accentColor.copy(alpha = 0.8f))
                        )
                    )
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Qué genial! ✨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
        }
    }
}
