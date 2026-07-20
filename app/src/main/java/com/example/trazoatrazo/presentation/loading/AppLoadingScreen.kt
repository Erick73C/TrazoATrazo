package com.example.trazoatrazo.presentation.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.R
import kotlinx.coroutines.delay

// ── Paleta fija de la pantalla de carga ────────────────────────────────────
// Intencionalmente NO usa AppColors: el tema del usuario todavía no ha
// terminado de cargarse en este punto (es justo lo que estamos esperando),
// así que esta pantalla usa colores propios, consistentes con la estética
// JJK de la app por defecto.
private val LoadingBg     = Color(0xFF0A0A0A)
private val LoadingGold   = Color(0xFFD4A017)
private val LoadingText   = Color(0xFFAAAAAA)
private val BarTrack      = Color(0xFF2A2412)

private const val FULL_TITLE = "Trazo a Trazo"

/**
 * Pantalla mostrada mientras `SettingsViewModel` termina de leer el tema
 * guardado desde DataStore (ver `AppNavigation.themeReady`).
 *
 * Reemplaza el flash negro que se notaba antes en dispositivos de gama
 * baja. Secuencia de animación, mismo patrón `Animatable` + `LaunchedEffect`
 * que se usa en todos los dibujos de la app:
 *
 *  1. Logo aparece con escala + fade (EaseOutBack)
 *  2. Título se escribe letra por letra
 *  3. Mensaje secundario aparece
 *  4. Barra de carga indeterminada corre en bucle mientras se espera
 */
@Composable
fun AppLoadingScreen(
    onFinished: () -> Unit = {}
) {
    var etapa by remember { mutableIntStateOf(0) }

    val logoAnim  = remember { Animatable(0f) }
    var titleCharCount by remember { mutableIntStateOf(0) }
    val messageAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Logo
        logoAnim.animateTo(1f, tween(550, easing = EaseOutBack))
        etapa = 1

        // 2. Título letra por letra
        delay(100L)
        while (titleCharCount < FULL_TITLE.length) {
            delay(45L)
            titleCharCount++
        }
        etapa = 2

        // 3. Mensaje secundario
        delay(150L)
        messageAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
        etapa = 3

        // 4. Deja la barra de carga visible un momento antes de continuar,
        // así siempre se alcanza a ver aunque themeReady ya esté en true.
        delay(900L)
        onFinished()
    }

    // Barra de carga indeterminada — corre en bucle una vez que ya hay algo en pantalla
    val infiniteTransition = rememberInfiniteTransition(label = "loadingBar")
    val barProgress by infiniteTransition.animateFloat(
        initialValue  = -0.4f,
        targetValue   = 1.4f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "barProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoadingBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Logo ──────────────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.ic_recuerdos_logo_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .scale(logoAnim.value)
            )

            // ── Título letra por letra ───────────────────────────────────
            Text(
                text       = FULL_TITLE.take(titleCharCount),
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = LoadingGold,
                modifier   = Modifier.height(28.dp) // reserva altura para que no "salte" mientras se escribe
            )

            // ── Mensaje secundario ────────────────────────────────────────
            Text(
                text     = "Preparando tus recuerdos...",
                fontSize = 13.sp,
                color    = LoadingText.copy(alpha = messageAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            // ── Barra de carga indeterminada ──────────────────────────────
            if (etapa >= 2) {
                Canvas(
                    modifier = Modifier
                        .width(140.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                ) {
                    drawRect(color = BarTrack, size = size)

                    val barWidth = size.width * 0.35f
                    val startX   = (size.width + barWidth) * barProgress - barWidth

                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, LoadingGold, Color.Transparent),
                            startX = startX,
                            endX   = startX + barWidth
                        ),
                        topLeft = Offset(0f, 0f),
                        size    = Size(size.width, size.height)
                    )
                }
            }
        }
    }
}