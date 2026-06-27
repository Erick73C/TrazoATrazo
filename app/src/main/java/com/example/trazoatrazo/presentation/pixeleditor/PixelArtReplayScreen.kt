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
import com.example.trazoatrazo.data.local.repository.PixelArtRepository
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlin.collections.getOrNull


@Composable
fun PixelArtReplayScreen(
    drawingId: Long,                 // ← antes recibía PixelArtwork completo
    viewModel: PixelArtViewModel,
    onBack: () -> Unit
) {
    // ── Carga desde Room ─────────────────────────────────────────────────────
    // null = cargando, emptyList = dibujo sin trazos, lista = listo para animar
    var steps      by remember { mutableStateOf<List<PixelArtRepository.StrokeStep>?>(null) }
    var canvasSize by remember { mutableIntStateOf(16) }
    var title      by remember { mutableStateOf("") }

    LaunchedEffect(drawingId) {
        val artwork = viewModel.repository.getDrawingForPlayback(drawingId)
        if (artwork != null) {
            canvasSize = artwork.canvasSize
            title      = artwork.title
            steps      = artwork.paintOrder   // ya ordenado cronológicamente por el Repository
        } else {
            steps = emptyList()               // dibujo no encontrado — muestra pantalla vacía
        }
    }

    // ── Animación trazo a trazo ──────────────────────────────────────────────
    var count   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    // El tiempo total apunta a ~2.8s sin importar si son 16 o 1024 píxeles
    val totalSteps = (steps?.size ?: 0).coerceAtLeast(1)
    val stepDelay  = (2800L / totalSteps).coerceIn(6L, 60L)

    // Se lanza cuando steps llega de Room O cuando el usuario pulsa Repetir
    LaunchedEffect(repetir, steps) {
        if (steps == null) return@LaunchedEffect   // todavía cargando
        count = 0
        delay(300L)
        val stepsSnapshot = steps ?: return@LaunchedEffect
        while (count < stepsSnapshot.size) {
            delay(stepDelay)
            count++
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        // Mientras carga, no dibujamos nada — el BackMenuButton ya es visible
        if (steps != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = canvasSize
                if (gridSize <= 0) return@Canvas

                val lienzo  = minOf(this.size.width, this.size.height) * 0.85f
                val cell    = lienzo / gridSize
                val offsetX = (this.size.width  - lienzo) / 2f
                val offsetY = (this.size.height - lienzo) / 2f

                val stepsSnapshot = steps ?: return@Canvas

                // 1. Dibujamos primero el fondo del lienzo (blanco sólido)
                //    Esto asegura que los borrados se vean bien sobre el tema oscuro.
                drawRect(
                    color   = Color.White,
                    topLeft = Offset(offsetX, offsetY),
                    size    = Size(lienzo, lienzo)
                )

                // 2. Reconstruimos el estado visual del lienzo hasta el paso 'count'
                //    en un único orden cronológico.
                val currentPixels = MutableList<Color?>(gridSize * gridSize) { null }
                for (i in 0 until count) {
                    val step = stepsSnapshot.getOrNull(i) ?: continue
                    if (step.pixelIndex in 0 until (gridSize * gridSize)) {
                        currentPixels[step.pixelIndex] = step.color
                    }
                }

                // 3. Dibujamos el estado reconstruido
                for (idx in currentPixels.indices) {
                    val color = currentPixels[idx] ?: continue
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
                    visible     = count >= (steps?.size ?: 0),
                    message     = "🎨 ¡Tu creación, trazo a trazo! 🎨",
                    subMessage  = title,
                    repeatEmoji = "🔁",
                    accentColor = AppColors.Maldicion,
                    onRepeat    = { repetir++ },
                    onBack      = onBack
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = AppColors.Reversa)
        }
    }
}