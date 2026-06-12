package com.example.trazoatrazo.presentation.gallery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.data.memoriesList
import com.example.trazoatrazo.domain.model.Memory
import com.example.trazoatrazo.domain.model.MemoryCategory
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay

// GalleryScreen
@Composable
fun GalleryScreen() {
    var selectedMemory by remember { mutableStateOf<Memory?>(null) }
    var selectedCategory by remember { mutableStateOf<MemoryCategory?>(null) }

    // Filtrar lista según categoría seleccionada
    val filteredMemories = remember(selectedCategory) {
        if (selectedCategory == null) memoriesList
        else memoriesList.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Contenido principal (grid) ────────────────────────────────────────
        LazyVerticalGrid(
            columns             = GridCells.Fixed(2),
            contentPadding      = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.fillMaxSize()
        ) {
            // ── Header de sección con Filtros ─────────────────────────────────
            item(
                key  = "gallery_header",
                span = { GridItemSpan(2) }
            ) {
                GalleryHeader(
                    totalCount = memoriesList.size,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { cat ->
                        selectedCategory = if (selectedCategory == cat) null else cat
                    }
                )
            }

            // ── Tarjetas polaroid filtradas ────────────────────────────────────
            itemsIndexed(
                items = filteredMemories,
                key   = { _, memory -> "memory_${memory.id}" }
            ) { index, memory ->
                PolaroidCard(
                    memory = memory, 
                    animIndex = index, 
                    onClick = { selectedMemory = memory }
                )
            }
            
            // Empty state si no hay recuerdos en esa categoría
            if (filteredMemories.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay recuerdos en esta categoría aún ✨", color = AppColors.Eco, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Overlay de detalle ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = selectedMemory != null,
            enter   = fadeIn(tween(260)) + scaleIn(tween(320), initialScale = 0.88f),
            exit    = fadeOut(tween(200)) + scaleOut(tween(220), targetScale = 0.92f)
        ) {
            selectedMemory?.let { memory ->
                MemoryDetailOverlay(
                    memory    = memory,
                    onDismiss = { selectedMemory = null }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── HEADER DE SECCIÓN ─────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun GalleryHeader(
    totalCount: Int,
    selectedCategory: MemoryCategory?,
    onCategorySelected: (MemoryCategory) -> Unit
) {
    val headerAnim = remember { Animatable(0f) }

    val countsByCategory = remember {
        MemoryCategory.entries.associateWith { cat ->
            memoriesList.count { it.category == cat }
        }
    }

    LaunchedEffect(Unit) {
        headerAnim.animateTo(1f, tween(600, easing = EaseOutBack))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(headerAnim.value)
            .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 0.dp)
            .offset(y = (-4).dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6B21A8).copy(alpha = 0.5f), Color(0xFFF06292).copy(alpha = 0.4f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", fontSize = 22.sp)
            }
            Column {
                Text(
                    text       = "Galería de Recuerdos",
                    fontSize   = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = AppColors.Reversa
                )
                Text(
                    text     = "$totalCount momentos especiales ✨",
                    fontSize = 11.sp,
                    color    = AppColors.Eco
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Filtrar por categoría:",
            fontSize = 11.sp,
            color = AppColors.ReversaSuave,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(countsByCategory.entries.filter { it.value > 0 }.toList()) { entry ->
                InteractiveCategoryChip(
                    category = entry.key,
                    count = entry.value,
                    isSelected = selectedCategory == entry.key,
                    onClick = { onCategorySelected(entry.key) }
                )
            }
        }

    }
}

@Composable
private fun InteractiveCategoryChip(
    category: MemoryCategory, 
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6B21A8).copy(alpha = 0.8f) else AppColors.Sombra,
        label = "bgColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppColors.Eco,
        label = "textColor"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text     = "${category.emoji} ${category.label} ($count)",
            fontSize = 10.sp,
            color    = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── TARJETA POLAROID ──────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PolaroidCard(
    memory:    Memory,
    animIndex: Int,
    onClick:   () -> Unit
) {
    // Animación de entrada escalonada
    val cardAnim = remember { Animatable(0f) }
    LaunchedEffect(memory.id) { // Reiniciar si el ID cambia (al filtrar)
        cardAnim.snapTo(0f)
        delay((animIndex * 40L).coerceAtMost(400L))
        cardAnim.animateTo(1f, tween(400, easing = EaseOutBack))
    }

    // Rotación determinista por id (pseudo-aleatoria pero consistente)
    val rotation = remember(memory.id) {
        val values = floatArrayOf(-2.5f, 1.2f, -1.8f, 2.2f, -0.8f, 1.5f, -2.0f, 0.9f)
        values[memory.id % values.size]
    }

    Box(
        modifier = Modifier
            .scale(cardAnim.value)
            .rotate(rotation)
            .shadow(
                elevation    = 8.dp,
                shape        = RoundedCornerShape(4.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
    ) {
        Column {
            // Foto
            Image(
                painter            = painterResource(id = memory.drawableRes),
                contentDescription = memory.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)  // Proporción polaroid: ligeramente más alta que ancha
            )

            // Tira blanca inferior (estilo polaroid)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = memory.title,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color(0xFF2A2A2A),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ── OVERLAY DE DETALLE ────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MemoryDetailOverlay(
    memory: Memory,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Tarjeta de detalle (intercepta clicks para no cerrar al tocar dentro)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.87f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { /* Intercepta el click — no cierra */ }
        ) {
            // ── Imagen grande ─────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter            = painterResource(id = memory.drawableRes),
                    contentDescription = memory.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(255.dp)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                )

                // Gradiente inferior sobre la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                            )
                        )
                )

                // Chip de categoría encima de la imagen
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text     = "${memory.category.emoji} ${memory.category.label}",
                        fontSize = 10.sp,
                        color    = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Detalles textuales ────────────────────────────────────────────
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text       = memory.title,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text       = memory.description,
                    fontSize   = 14.sp,
                    color      = Color(0xFF555555),
                    lineHeight = 21.sp
                )

                Spacer(Modifier.height(20.dp))

                // ── Botón cerrar ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6B21A8).copy(alpha = 0.12f),
                                    Color(0xFFF06292).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text("✕", fontSize = 13.sp, color = Color(0xFF6B21A8))
                        Text(
                            text       = "Cerrar",
                            fontSize   = 14.sp,
                            color      = Color(0xFF6B21A8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}