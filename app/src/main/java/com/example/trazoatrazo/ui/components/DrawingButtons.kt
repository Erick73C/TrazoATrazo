package com.example.trazoatrazo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.LocalAppFont
import com.example.trazoatrazo.ui.theme.fontFamilyFor
import com.example.trazoatrazo.utils.subtitleColorFor
import com.example.trazoatrazo.utils.textColorFor
import androidx.compose.ui.tooling.preview.Preview

/**
 * Bloque reutilizable que aparece al terminar cualquier animación.
 * Ahora separa el Título (arriba) del Subtítulo y Botones (abajo).
 *
 * @param visible         Mostrar u ocultar
 * @param message         Texto principal (Título que aparecerá arriba)
 * @param subMessage      Texto secundario (Aparecerá abajo con los botones)
 * @param repeatLabel     Texto del botón repetir
 * @param repeatEmoji     Emoji del botón repetir
 * @param accentColor     Color de los botones
 * @param backgroundColor Color de fondo actual para adaptar el contraste
 * @param onRepeat        Acción al presionar Repetir
 * @param onBack          Acción al presionar ← Menú
 */
@Composable
fun DrawingButtons(
    visible:         Boolean,
    message:         String,
    subMessage:      String  = "",
    repeatLabel:     String  = "Repetir",
    repeatEmoji:     String  = "🔁",
    accentColor:     Color   = Color(0xFFB8860B),
    backgroundColor: Color   = Color.Black,
    onRepeat:        () -> Unit,
    onBack:          () -> Unit,
    onSave:          ((withText: Boolean) -> Unit)? = null
) {
    val mainTextColor = textColorFor(backgroundColor)
    val secondaryTextColor = subtitleColorFor(backgroundColor)
    var showSaveOptions by remember { mutableStateOf(false) }
    
    val currentFont = LocalAppFont.current
    val fontFamily = fontFamilyFor(currentFont)

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(600)) + slideInVertically(tween(600), initialOffsetY = { it / 2 })
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // ── Título (Parte Superior) ───────────────────────────────────────
            if (message.isNotEmpty()) {
                Text(
                    text       = message,
                    color      = mainTextColor,
                    fontFamily = fontFamily,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier   = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth()
                )
            }

            // ── Contenido Inferior (Subtítulo + Botones) ──────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (subMessage.isNotEmpty()) {
                    Text(
                        text      = subMessage,
                        color     = secondaryTextColor,
                        fontFamily = fontFamily,
                        fontSize  = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 30.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Botón Menú
                    Button(
                        onClick = onBack,
                        shape   = RoundedCornerShape(14.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = mainTextColor.copy(alpha = 0.12f),
                            contentColor   = mainTextColor
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text("← Menú", fontSize = 13.sp, fontFamily = fontFamily)
                    }

                    // Botón Repetir
                    Button(
                        onClick = onRepeat,
                        shape   = RoundedCornerShape(14.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor   = textColorFor(accentColor)
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            "$repeatEmoji $repeatLabel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }

                    // Botón Guardar (Si está disponible)
                    if (onSave != null) {
                        Button(
                            onClick = { showSaveOptions = true },
                            shape   = RoundedCornerShape(14.dp),
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Eco.copy(alpha = 0.2f),
                                contentColor   = AppColors.Eco
                            ),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Guardar", fontSize = 13.sp, fontFamily = fontFamily)
                        }
                    }
                }
            }

            // ── Diálogo de Opciones de Guardado ────────────────────────────────
            if (showSaveOptions && onSave != null) {
                AlertDialog(
                    onDismissRequest = { showSaveOptions = false },
                    containerColor = Color(0xFF1E1E1E),
                    title = {
                        Text("Guardar creación", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = fontFamily)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("¿Cómo deseas guardar tu dibujo?", color = Color.Gray, fontSize = 14.sp, fontFamily = fontFamily)
                            
                            OptionButton(
                                icon = "🖼️",
                                title = "Sólo el dibujo",
                                subtitle = "Sin mensajes ni fondos extra",
                                onClick = {
                                    onSave(false)
                                    showSaveOptions = false
                                }
                            )

                            OptionButton(
                                icon = "✨",
                                title = "Imagen completa",
                                subtitle = "Incluye mensajes y diseño final",
                                onClick = {
                                    onSave(true)
                                    showSaveOptions = false
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSaveOptions = false }) {
                            Text("Cancelar", color = AppColors.Maldicion, fontFamily = fontFamily)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OptionButton(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val currentFont = LocalAppFont.current
    val fontFamily = fontFamilyFor(currentFont)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = fontFamily)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp, fontFamily = fontFamily)
        }
    }
}

/**
 * Botón pequeño "← Menú" siempre visible en la esquina superior izquierda.
 */
@Composable
fun BackMenuButton(
    onBack:     () -> Unit,
    tintColor:  Color = Color.White.copy(alpha = 0.85f)
) {
    val currentFont = LocalAppFont.current
    val fontFamily = fontFamilyFor(currentFont)

    Button(
        onClick  = onBack,
        modifier = Modifier.padding(12.dp),
        shape    = RoundedCornerShape(12.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text("← Menú", color = tintColor, fontSize = 13.sp, fontFamily = fontFamily)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF003366)
@Composable
fun DrawingButtonsPreview() {
    DrawingButtons(
        visible = true,
        message = "🐢 ¡Una linda tortuga! 🐢",
        subMessage = "Hace referencia a la tortuga que te di XD",
        repeatEmoji = "🔄",
        accentColor = Color(0xFF2E7D32),
        backgroundColor = Color(0xFF003366),
        onRepeat = {},
        onBack = {}
    )
}
