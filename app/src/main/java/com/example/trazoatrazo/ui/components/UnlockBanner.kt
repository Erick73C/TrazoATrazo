package com.example.trazoatrazo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay

/**
 * Banner temporal "🌻 Nuevo recuerdo desbloqueado" — a diferencia de
 * [EventBanner] (que dura toda la temporada de un evento), este se
 * **auto-oculta** después de unos segundos, ya que celebra un momento
 * puntual (cruzar el umbral de racha de un [com.example.trazoatrazo.domain.model.UnlockRequirement])
 * y no un estado que deba permanecer visible.
 *
 * El llamador es responsable de "consumir" el aviso (normalmente marcando
 * el id como notificado vía `AppUsageViewModel.markAsNotified`) — este
 * composable solo se encarga de la presentación visual y del temporizador
 * de auto-cierre, no decide cuándo debe aparecer.
 *
 * @param message         Texto a mostrar, normalmente [com.example.trazoatrazo.domain.model.UnlockRequirement.unlockedMessage].
 * @param durationMillis   Cuánto tiempo permanece visible antes de auto-ocultarse.
 * @param onDismissed      Se llama una vez, cuando termina el tiempo de vida del banner
 *                          (útil para que el llamador limpie su estado, ej. `unlockBannerToShow = null`).
 */
@Composable
fun UnlockBanner(
    message:        String,
    durationMillis: Long = 5200L,
    onDismissed:    () -> Unit
) {
    var visible by remember(message) { mutableStateOf(true) }

    LaunchedEffect(message) {
        delay(durationMillis)
        visible = false
        delay(400L) // deja que termine la animación de salida antes de notificar
        onDismissed()
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { -it / 2 },
        exit    = fadeOut(tween(350)) + slideOutVertically(tween(350)) { -it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.Maldicion.copy(alpha = 0.85f),
                            AppColors.Tecnica.copy(alpha = 0.55f)
                        )
                    )
                )
                .drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color        = AppColors.KiEspiritual.copy(alpha = 0.5f),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style        = Stroke(width = 1.2f)
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text       = message,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}