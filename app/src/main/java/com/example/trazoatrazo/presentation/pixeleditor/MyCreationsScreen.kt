package com.example.trazoatrazo.presentation.pixeleditor

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trazoatrazo.data.local.repository.PixelArtRepository
import com.example.trazoatrazo.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Colores internos ──────────────────────────────────────────────────────────
private val ThumbCheckerLight = Color(0xFFF2F2F2)
private val ThumbCheckerDark  = Color(0xFFD8D8D8)

// ── Ordenamiento ──────────────────────────────────────────────────────────────
private enum class SortOrder(val label: String) {
    RECENT("Recientes"),
    OLDEST("Antiguos"),
    AZ("A → Z")
}

// ── Pantalla principal ────────────────────────────────────────────────────────
@Composable
fun MyCreationsScreen(
    viewModel:      PixelArtViewModel,
    onBack:         () -> Unit,
    onNewCreation:  () -> Unit,
    onOpenCreation: (artworkId: Long) -> Unit,
    onReplay:       (artworkId: Long) -> Unit
) {
    val galleryState by viewModel.galleryState.collectAsStateWithLifecycle()
    var sortOrder    by remember { mutableStateOf(SortOrder.RECENT) }
    var artworkToDelete by remember { mutableStateOf<PixelArtRepository.PixelArtworkDomain?>(null) }

    // Orden nativo de Room ya viene por updatedAt DESC — aplicamos sort local solo si cambia
    val artworks = remember(galleryState.artworks, sortOrder) {
        when (sortOrder) {
            SortOrder.RECENT -> galleryState.artworks                              // Room ya los trae así
            SortOrder.OLDEST -> galleryState.artworks.sortedBy { it.updatedAt }
            SortOrder.AZ     -> galleryState.artworks.sortedBy { it.title.lowercase() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            MyCreationsHeader(onBack = onBack, count = artworks.size)

            // ── Chips de ordenamiento ──────────────────────────────────────────
            if (artworks.isNotEmpty() || galleryState.isLoading) {
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SortOrder.entries.forEach { order ->
                        SortChip(
                            label      = order.label,
                            isSelected = order == sortOrder,
                            onClick    = { sortOrder = order }
                        )
                    }
                }
            }

            // ── Grid o estado vacío ────────────────────────────────────────────
            if (artworks.isEmpty() && !galleryState.isLoading) {
                EmptyCreationsState(modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    modifier              = Modifier.weight(1f),
                    contentPadding        = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(artworks, key = { it.id }) { artwork ->
                        CreationCard(
                            artwork  = artwork,
                            onClick  = { onOpenCreation(artwork.id) },
                            onReplay = { onReplay(artwork.id) },
                            onDelete = { artworkToDelete = artwork }
                        )
                    }
                }
            }

            // ── Botón nueva creación ───────────────────────────────────────────
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick  = onNewCreation,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Maldicion)
                ) {
                    Text("✏️  Nueva creación", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // ── Diálogo de confirmación de borrado ─────────────────────────────────────
    artworkToDelete?.let { artwork ->
        DeleteConfirmDialog(
            artworkName = artwork.title,
            onConfirm   = {
                viewModel.deleteDrawing(artwork.id)
                artworkToDelete = null
            },
            onDismiss = { artworkToDelete = null }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun MyCreationsHeader(onBack: () -> Unit, count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Sombra)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text("← Atrás", fontSize = 13.sp, color = AppColors.ReversaSuave)
        }

        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "🖼️ Mis Creaciones",
                fontSize   = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = AppColors.Reversa
            )
            Text(
                text     = when (count) {
                    0    -> "Sin dibujos aún"
                    1    -> "1 dibujo guardado"
                    else -> "$count dibujos guardados"
                },
                fontSize = 11.sp,
                color    = AppColors.Eco
            )
        }
    }
}

// ── Chip de ordenamiento ──────────────────────────────────────────────────────
@Composable
private fun SortChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AppColors.Maldicion else AppColors.Sombra)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else AppColors.Maldicion.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (isSelected) Color.White else AppColors.Eco
        )
    }
}

// ── Tarjeta de creación ───────────────────────────────────────────────────────
@Composable
private fun CreationCard(
    artwork:  PixelArtRepository.PixelArtworkDomain,
    onClick:  () -> Unit,
    onReplay: () -> Unit,
    onDelete: () -> Unit
) {
    // Animación más corta para gama baja
    val cardAnim = remember { Animatable(0.95f) }
    LaunchedEffect(artwork.id) {
        cardAnim.animateTo(1f, tween(300))
    }

    Box(
        modifier = Modifier
            .scale(cardAnim.value)
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Sombra)
            .border(1.dp, AppColors.Maldicion.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
    ) {
        Column {

            // ── Thumbnail ──────────────────────────────────────────────────────
            Box {
                PixelArtworkThumbnail(
                    artwork  = artwork,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                )

                // Badge de tamaño — esquina superior izquierda
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text     = "${artwork.canvasSize}px",
                        fontSize = 9.sp,
                        color    = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Info + acciones ────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {

                Text(
                    text       = artwork.title,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppColors.Reversa,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                // Fecha de última modificación desde updatedAt
                Text(
                    text     = formatDate(artwork.updatedAt),
                    fontSize = 10.sp,
                    color    = AppColors.Eco
                )

                Spacer(Modifier.height(8.dp))

                // Barra de acciones: reproducir | editar | borrar
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Reproducir
                    ActionChip(
                        emoji    = "▶",
                        label    = "Ver trazo",
                        bgColor  = AppColors.Maldicion.copy(alpha = 0.15f),
                        txtColor = AppColors.Tecnica,
                        modifier = Modifier.weight(1f),
                        onClick  = onReplay
                    )
                    // Borrar
                    ActionChip(
                        emoji    = "✕",
                        label    = "Borrar",
                        bgColor  = AppColors.Sukuna.copy(alpha = 0.12f),
                        txtColor = AppColors.Sukuna,
                        modifier = Modifier.weight(1f),
                        onClick  = onDelete
                    )
                }
            }
        }
    }
}

// ── Chip de acción dentro de la tarjeta ──────────────────────────────────────
@Composable
private fun ActionChip(
    emoji:    String,
    label:    String,
    bgColor:  Color,
    txtColor: Color,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 10.sp, color = txtColor)
            Spacer(Modifier.width(3.dp))
            Text(label, fontSize = 10.sp, color = txtColor, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Thumbnail del pixel art optimizado ─────────────────────────────────────────
@Composable
fun PixelArtworkThumbnail(
    artwork:  PixelArtRepository.PixelArtworkDomain,
    modifier: Modifier = Modifier
) {
    val gridSize = artwork.canvasSize
    val pixels   = artwork.pixels

    // Optimización vital para gama baja: 
    // Renderizar el dibujo a un Bitmap de 1:1 píxeles una sola vez y cachearlo.
    // Esto evita miles de llamadas drawRect por frame durante el scroll.
    val thumbnailBitmap = remember(artwork.id, pixels) {
        if (gridSize <= 0) return@remember null
        
        val bitmap = ImageBitmap(gridSize, gridSize)
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
        val paint  = Paint()

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val idx     = row * gridSize + col
                val checker = if ((row + col) % 2 == 0) ThumbCheckerLight else ThumbCheckerDark
                val color   = pixels.getOrNull(idx) ?: checker
                
                paint.color = color
                canvas.drawRect(
                    left   = col.toFloat(),
                    top    = row.toFloat(),
                    right  = col + 1f,
                    bottom = row + 1f,
                    paint  = paint
                )
            }
        }
        bitmap
    }

    Box(modifier = modifier.background(Color.White)) {
        if (thumbnailBitmap != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // FilterQuality.None es crucial: es más rápido y mantiene el estilo Pixel Art nítido
                drawImage(
                    image         = thumbnailBitmap,
                    dstSize       = IntSize(size.width.toInt(), size.height.toInt()),
                    filterQuality = FilterQuality.None
                )
            }
        }
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyCreationsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("🎨", fontSize = 52.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                text       = "Todavía no tienes creaciones",
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = AppColors.Reversa,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = "Toca \"Nueva creación\" para hacer\ntu primer pixel art",
                fontSize  = 13.sp,
                color     = AppColors.Eco,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 8.dp)
            )
        }
    }
}

// ── Diálogo de confirmación de borrado ────────────────────────────────────────
@Composable
private fun DeleteConfirmDialog(
    artworkName: String,
    onConfirm:   () -> Unit,
    onDismiss:   () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Sombra)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text       = "🗑️ Eliminar creación",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Reversa
                )
                Text(
                    text       = "¿Seguro que quieres eliminar \"$artworkName\"?\nEsta acción no se puede deshacer.",
                    fontSize   = 13.sp,
                    color      = AppColors.Eco,
                    lineHeight = 18.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = AppColors.Eco)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors  = ButtonDefaults.buttonColors(containerColor = AppColors.Sukuna)
                    ) {
                        Text("Eliminar", color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Utilidad: formatear fecha legible desde timestamp ─────────────────────────
private fun formatDate(timestamp: Long): String {
    val now      = System.currentTimeMillis()
    val diffMs   = now - timestamp
    val diffMins = diffMs / 60_000L
    val diffHrs  = diffMs / 3_600_000L
    val diffDays = diffMs / 86_400_000L

    return when {
        diffMins < 1    -> "Ahora mismo"
        diffMins < 60   -> "Hace ${diffMins}m"
        diffHrs  < 24   -> "Hace ${diffHrs}h"
        diffDays < 7    -> "Hace ${diffDays}d"
        else            -> SimpleDateFormat("dd MMM yyyy", Locale("es")).format(Date(timestamp))
    }
}