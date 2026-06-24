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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trazoatrazo.data.PixelArtwork


import com.example.trazoatrazo.ui.theme.AppColors

private val ThumbCheckerLight = Color(0xFFF2F2F2)
private val ThumbCheckerDark  = Color(0xFFD8D8D8)

@Composable
fun MyCreationsScreen(
    viewModel:      PixelArtViewModel,
    onBack:         () -> Unit,
    onNewCreation:  () -> Unit,
    onOpenCreation: (artworkId: String) -> Unit
) {
    val artworks by viewModel.artworks.collectAsStateWithLifecycle()
    var artworkToDelete by remember { mutableStateOf<PixelArtwork?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MyCreationsHeader(onBack = onBack, count = artworks.size)

            if (artworks.isEmpty()) {
                EmptyCreationsState(modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    modifier              = Modifier.weight(1f),
                    contentPadding        = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement   = Arrangement.spacedBy(14.dp)
                ) {
                    items(artworks, key = { it.id }) { artwork ->
                        CreationCard(
                            artwork  = artwork,
                            onClick  = { onOpenCreation(artwork.id) },
                            onDelete = { artworkToDelete = artwork }
                        )
                    }
                }
            }

            // ── Botón nueva creación ───────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

    artworkToDelete?.let { artwork ->
        DeleteConfirmDialog(
            artworkName = artwork.name,
            onConfirm   = {
                viewModel.delete(artwork.id)
                artworkToDelete = null
            },
            onDismiss   = { artworkToDelete = null }
        )
    }
}

@Composable
private fun MyCreationsHeader(onBack: () -> Unit, count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
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
            modifier             = Modifier.align(Alignment.Center),
            horizontalAlignment  = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "🖼️ Mis Creaciones",
                fontSize   = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = AppColors.Reversa
            )
            Text(
                text     = if (count == 1) "1 dibujo guardado" else "$count dibujos guardados",
                fontSize = 11.sp,
                color    = AppColors.Eco
            )
        }
    }
}

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

@Composable
private fun CreationCard(
    artwork:  PixelArtwork,
    onClick:  () -> Unit,
    onDelete: () -> Unit
) {
    val cardAnim = remember { Animatable(0f) }
    LaunchedEffect(artwork.id) {
        cardAnim.animateTo(1f, tween(350, easing = EaseOutBack))
    }

    Box(
        modifier = Modifier
            .scale(cardAnim.value)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Sombra)
            .border(1.dp, AppColors.Maldicion.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(10.dp)
    ) {
        Column {
            PixelArtworkThumbnail(
                artwork  = artwork,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = artwork.name,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = AppColors.Reversa,
                maxLines   = 1
            )
            Text(
                text     = "${artwork.gridSize}×${artwork.gridSize}",
                fontSize = 10.sp,
                color    = AppColors.Eco
            )
        }

        // Botón eliminar — esquina superior derecha
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .clip(CircleShape)
                .background(AppColors.Sukuna.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onDelete
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("✕", fontSize = 12.sp, color = Color.White)
        }
    }
}

// ── Mini-canvas de solo lectura para previsualizar una creación ────────────────
@Composable
fun PixelArtworkThumbnail(artwork: PixelArtwork, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color.White)) {
        val gridSize = artwork.gridSize
        if (gridSize <= 0) return@Canvas
        val cell = size.width / gridSize
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val idx     = row * gridSize + col
                val checker = if ((row + col) % 2 == 0) ThumbCheckerLight else ThumbCheckerDark
                val color   = artwork.pixels.getOrNull(idx) ?: checker
                drawRect(
                    color   = color,
                    topLeft = Offset(col * cell, row * cell),
                    size    = Size(cell, cell)
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    artworkName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Sombra)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("🗑️ Eliminar creación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)
                Text(
                    text       = "¿Seguro que quieres eliminar \"$artworkName\"? Esta acción no se puede deshacer.",
                    fontSize   = 13.sp,
                    color      = AppColors.Eco,
                    lineHeight = 18.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.Eco) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Sukuna)) {
                        Text("Eliminar", color = Color.White)
                    }
                }
            }
        }
    }
}