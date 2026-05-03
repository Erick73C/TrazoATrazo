package com.example.trazoatrazo.ui.Screen

import allCategories
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay


// ── Modelo de dibujo ──────────────────────────────────────────────────────────
data class DrawingItem(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val description: String,
    val bgColor:     Color = AppColors.Sombra,
    val accentColor: Color = AppColors.FlowersAccent
)

// Catálogo de dibujos por categoría
val drawingsByCategoryId: Map<String, List<DrawingItem>> = mapOf(

    Routes.Category.FLOWERS to listOf(
        DrawingItem(
            id          = Routes.Drawings.GIRASOL,
            emoji       = "🌻",
            title       = "Girasol",
            description = "Especial para ti :D",
            bgColor     = Color(0xFFFFF9C4),
            accentColor = Color(0xFFF9A825)
        ),
        DrawingItem(
            id          = Routes.Drawings.FLORES,
            emoji       = "💐",
            title       = "Ramo de Flores",
            description = "Ten un bonito día :D",
            bgColor     = Color(0xFFFCE4EC),

            accentColor = Color(0xFFE75480)
        ),
        DrawingItem(
            id          = Routes.Drawings.IMPROVED_SUNFLOWER,
            emoji       = "🌞",
            title       = "Girasol Mejorado",
            description = "Con tallo y hojas animados",
            bgColor     = Color(0xFFFFF3CD),
            accentColor = Color(0xFF8B6914)
        ),
    ),

    Routes.Category.CARTOONS to listOf(
        DrawingItem(
            id          = Routes.Drawings.HEART,
            emoji       = "🤍",
            title       = "Corazón",
            description = "14 de febrero",
            bgColor     = Color(0xFFFFEBEE),
            accentColor = Color(0xFFE91E63)
        ),
    ),

    Routes.Category.ANIMALS to listOf(
        DrawingItem(
            id          = Routes.Drawings.TURTLE,
            emoji       = "🐢",
            title       = "Tortuga",
            description = "Animación paso a paso",
            bgColor     = Color(0xFFE8F5E9),
            accentColor = Color(0xFF2E7D32)
        ),
    ),
)

// ── CategoryScreen ────────────────────────────────────────────────────────────
@Composable
fun CategoryScreen(
    categoryId:    String,
    onDrawingClick: (String) -> Unit,
    onBack:        () -> Unit
) {
    val category = allCategories.find { it.id == categoryId }
        ?: return

    val drawings = drawingsByCategoryId[categoryId] ?: emptyList()

    // Animaciones de entrada
    val headerAnim = remember { Animatable(0f) }
    val itemAnims  = remember { List(maxOf(drawings.size, 1)) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        headerAnim.animateTo(1f, tween(550, easing = EaseOutBack))
        itemAnims.forEach { anim ->
            delay(70)
            anim.animateTo(1f, tween(420, easing = EaseOutBack))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        // ── Fondo decorativo ─────────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        category.accentColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.8f, size.height * 0.1f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.8f, size.height * 0.1f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(headerAnim.value)
                    .fillMaxWidth()
                    .padding(top = 52.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
            ) {
                Column {
                    // Botón volver
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Sombra)
                            .clickable { onBack() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            "← Inicio",
                            color    = AppColors.ReversaSuave,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Emoji + título de categoría
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(category.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(category.emoji, fontSize = 28.sp)
                        }
                        Column {
                            Text(
                                text       = category.title,
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = AppColors.Reversa
                            )
                            Text(
                                text     = "${drawings.size} dibujo${if (drawings.size != 1) "s" else ""}",
                                fontSize = 13.sp,
                                color    = AppColors.Eco
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Lista de dibujos ─────────────────────────────────────────
            if (drawings.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎨", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Próximamente",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AppColors.Reversa
                        )
                        Text(
                            "Se están preparando dibujos\npara esta categoría",
                            fontSize  = 14.sp,
                            color     = AppColors.ReversaSuave,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier  = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding        = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement   = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(drawings) { index, drawing ->
                        DrawingListCard(
                            scale       = itemAnims.getOrNull(index)?.value ?: 1f,
                            drawing     = drawing,
                            accentColor = category.accentColor,
                            onClick     = { onDrawingClick(drawing.id) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── DrawingListCard ───────────────────────────────────────────────────────────
@Composable
private fun DrawingListCard(
    scale:       Float,
    drawing:     DrawingItem,
    accentColor: Color,
    onClick:     () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue   = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "press"
    )

    Box(
        modifier = Modifier
            .scale(scale * pressScale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(drawing.bgColor)
            .then(
                Modifier.drawWithContent {
                    drawContent()
                    drawRoundRect(
                        color        = accentColor.copy(alpha = 0.25f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                        style        = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )
                }
            )
            .clickable {
                pressed = true
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 18.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono del dibujo
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(drawing.emoji, fontSize = 26.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = drawing.title,
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.Black//Cambio de color de titulo
                )
                Text(
                    text     = drawing.description,
                    fontSize = 12.sp,
                    color    = AppColors.Eco
                )
            }

            // Flecha
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", fontSize = 12.sp, color = accentColor)
            }
        }
    }
}