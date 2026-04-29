package com.example.trazoatrazo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bloque reutilizable que aparece al terminar cualquier animación.
 * Incluye: mensaje principal, submensaje opcional, botón Repetir y botón Menú.
 *
 * @param visible      Mostrar u ocultar (se anima con fade+slide)
 * @param message      Texto principal  ej: "🌻 Una flor para ti 🌻"
 * @param subMessage   Texto secundario ej: "Que tengas una bonita navidad"
 * @param repeatLabel  Texto del botón repetir (default "Repetir")
 * @param repeatEmoji  Emoji del botón repetir (default "🔁")
 * @param accentColor  Color de los botones (cada dibujo puede tener el suyo)
 * @param onRepeat     Acción al presionar Repetir
 * @param onBack       Acción al presionar ← Menú
 */
@Composable
fun DrawingButtons(
    visible:     Boolean,
    message:     String,
    subMessage:  String  = "",
    repeatLabel: String  = "Repetir",
    repeatEmoji: String  = "🔁",
    accentColor: Color   = Color(0xFFB8860B),
    onRepeat:    () -> Unit,
    onBack:      () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(500)) + slideInVertically(
            animationSpec = tween(500),
            initialOffsetY = { it / 2 }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mensaje principal
            Text(
                text       = message,
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )

            // Submensaje opcional
            if (subMessage.isNotEmpty()) {
                Text(
                    text      = subMessage,
                    color     = Color.White.copy(alpha = 0.75f),
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(4.dp))

            // Botones
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // ← Menú
                Button(
                    onClick = onBack,
                    shape   = RoundedCornerShape(14.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f)
                    )
                ) {
                    Text("← Menú", color = Color.White, fontSize = 14.sp)
                }

                // Repetir
                Button(
                    onClick = onRepeat,
                    shape   = RoundedCornerShape(14.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    )
                ) {
                    Text(
                        "$repeatEmoji $repeatLabel",
                        color    = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Botón pequeño "← Menú" siempre visible en la esquina superior izquierda.
 * Úsalo en todas las pantallas de dibujo.
 */
@Composable
fun BackMenuButton(
    onBack:     () -> Unit,
    tintColor:  Color = Color.White.copy(alpha = 0.85f)
) {
    Button(
        onClick  = onBack,
        modifier = Modifier.padding(12.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.25f)
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text("← Menú", color = tintColor, fontSize = 13.sp)
    }
}