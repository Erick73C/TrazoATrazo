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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.utils.textColorFor

@Composable
fun EventInfoBox(
    event: SpecialEvent,
    onDismiss: () -> Unit
) {
    val textColor = textColorFor(event.accentColor)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AppColors.Sombra)
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        listOf(event.accentColor, event.accentColor.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable(enabled = false) { }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(event.bannerEmoji, fontSize = 52.sp)
            
            Spacer(Modifier.height(14.dp))
            
            Text(
                text = event.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = event.accentColor,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(10.dp))
            
            Text(
                text = event.bannerMessage,
                fontSize = 14.sp,
                color = AppColors.ReversaSuave,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(18.dp))
            
            // Recuadro de duración
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(event.accentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "📅 " + event.durationText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = event.accentColor
                )
            }
            
            Spacer(Modifier.height(28.dp))
            
            // Botón de cerrar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(event.accentColor)
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Qué genial! ✨",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }
    }
}
