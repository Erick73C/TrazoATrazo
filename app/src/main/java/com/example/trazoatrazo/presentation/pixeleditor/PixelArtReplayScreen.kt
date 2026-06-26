package com.example.trazoatrazo.presentation.pixeleditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.data.PixelArtwork
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun PixelArtReplayScreen(artwork: PixelArtwork, onBack: () -> Unit) {
    var count   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    // El tiempo total apunta a ~2.8s sin importar si son 16 o 1024 píxeles
    val totalSteps = artwork.paintOrder.size.coerceAtLeast(1)
    val stepDelay  = (2800L / totalSteps).coerceIn(6L, 60L)

    LaunchedEffect(repetir) {
        count = 0
        delay(300L)
        while (count < artwork.paintOrder.size) {
            delay(stepDelay)
            count++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = artwork.gridSize
            if (gridSize <= 0) return@Canvas

            val canvasSize = minOf(this.size.width, this.size.height) * 0.85f
            val cell        = canvasSize / gridSize
            val offsetX     = (this.size.width  - canvasSize) / 2f
            val offsetY     = (this.size.height - canvasSize) / 2f

            for (i in 0 until count) {
                val idx   = artwork.paintOrder.getOrNull(i) ?: continue
                val color = artwork.pixels.getOrNull(idx) ?: continue
                val row   = idx / gridSize
                val col   = idx % gridSize
                drawRect(
                    color   = color,
                    topLeft = Offset(offsetX + col * cell, offsetY + row * cell),
                    size    = Size(cell, cell)
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            DrawingButtons(
                visible     = count >= artwork.paintOrder.size,
                message     = "🎨 ¡Tu creación, trazo a trazo! 🎨",
                subMessage  = artwork.name,
                repeatEmoji = "🔁",
                accentColor = AppColors.Maldicion,
                onRepeat    = { repetir++ },
                onBack      = onBack
            )
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = AppColors.Reversa)
        }
    }
}