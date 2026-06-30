package com.example.trazoatrazo.presentation.pixeleditor

import android.graphics.Bitmap
import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.data.local.repository.PixelArtRepository
import com.example.trazoatrazo.ui.components.BackMenuButton
import com.example.trazoatrazo.ui.components.DrawingButtons
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.utils.subtitleColorFor
import com.example.trazoatrazo.utils.textColorFor
import kotlinx.coroutines.delay
import kotlin.collections.getOrNull

enum class ReplaySpeed(val label: String, val multiplier: Float) {
    LENTO("Lento", 3.0f),
    NORMAL("Normal", 1.0f),
    RAPIDO("Rápido", 0.3f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelArtReplayScreen(
    drawingId: Long,                 // ← antes recibía PixelArtwork completo
    viewModel: PixelArtViewModel,
    onBack: () -> Unit
) {
    // ── Carga desde Room ─────────────────────────────────────────────────────
    // null = cargando, emptyList = dibujo sin trazos, lista = listo para animar
    var steps          by remember { mutableStateOf<List<PixelArtRepository.StrokeStep>?>(null) }
    var canvasSize     by remember { mutableIntStateOf(16) }
    var title          by remember { mutableStateOf("") }

    // Estados de personalización
    var currentSpeed   by remember { mutableStateOf(ReplaySpeed.NORMAL) }
    var canvasBgColor  by remember { mutableStateOf(Color.White) }
    var customMessage  by remember { mutableStateOf("🎨 ¡Tu creación, trazo a trazo! 🎨") }
    var customSubTitle by remember { mutableStateOf("") }
    var showTexts      by remember { mutableStateOf(true) }
    var showSettings   by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(drawingId) {
        val artwork = viewModel.repository.getDrawingForPlayback(drawingId)
        if (artwork != null) {
            canvasSize = artwork.canvasSize
            title      = artwork.title
            customSubTitle = artwork.title
            steps      = artwork.paintOrder   // ya ordenado cronológicamente por el Repository
        } else {
            steps = emptyList()               // dibujo no encontrado — muestra pantalla vacía
        }
    }

    // ── Animación trazo a trazo ──────────────────────────────────────────────
    var count   by remember { mutableIntStateOf(0) }
    var repetir by remember { mutableIntStateOf(0) }

    // El tiempo total apunta a ~2.8s base, ajustado por el multiplicador de velocidad
    val totalSteps = (steps?.size ?: 0).coerceAtLeast(1)
    val baseDelay  = (2800L / totalSteps).coerceIn(6L, 60L)
    val stepDelay  = (baseDelay * currentSpeed.multiplier).toLong().coerceAtLeast(1L)

    // Se lanza cuando steps llega de Room O cuando el usuario pulsa Repetir O cambia velocidad
    LaunchedEffect(repetir, steps, currentSpeed) {
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

                // 1. Dibujamos primero el fondo del lienzo (personalizable)
                drawRect(
                    color   = canvasBgColor,
                    topLeft = Offset(offsetX, offsetY),
                    size    = Size(lienzo, lienzo)
                )

                // 2. Reconstruimos el estado visual del lienzo hasta el paso 'count'
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

            // Panel de Botones Finales
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                DrawingButtons(
                    visible     = count >= (steps?.size ?: 0),
                    message     = if (showTexts) customMessage else "",
                    subMessage  = if (showTexts) customSubTitle else "",
                    repeatEmoji = "🔁",
                    accentColor = AppColors.Maldicion,
                    onRepeat    = { repetir++ },
                    onBack      = onBack
                )
            }

            // Botones Flotantes (Configuración y Guardar)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { showSettings = true },
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración")
                }

                if (count >= (steps?.size ?: 0)) {
                    FloatingActionButton(
                        onClick = {
                            saveDrawingAsImage(
                                context,
                                steps ?: emptyList(),
                                canvasSize,
                                canvasBgColor,
                                customMessage,
                                customSubTitle,
                                showTexts
                            )
                        },
                        containerColor = AppColors.Maldicion,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Guardar Imagen")
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopStart)) {
            BackMenuButton(onBack = onBack, tintColor = AppColors.Reversa)
        }

        // ── Bottom Sheet de Configuración ────────────────────────────────────
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
                // Removido windowInsets ya que causaba error en esta versión de Compose
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding() // Asegura que el contenido suba con el teclado
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Configuración de Reproducción", fontSize = 20.sp, color = Color.White)

                    // Velocidad
                    Column {
                        Text("Velocidad de trazo", fontSize = 14.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReplaySpeed.entries.forEach { speed ->
                                FilterChip(
                                    selected = currentSpeed == speed,
                                    onClick = { currentSpeed = speed },
                                    label = { Text(speed.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.Maldicion,
                                        selectedLabelColor = Color.White,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }
                        }
                    }

                    // Color de Fondo
                    Column {
                        Text("Color de fondo del lienzo", fontSize = 14.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()) // Scroll para más colores
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val backgroundColors = listOf(
                                Color.White, Color.Black, Color(0xFFF5F5F5), 
                                Color(0xFFFFEBEE), Color(0xFFFCE4EC), Color(0xFFF3E5F5),
                                Color(0xFFE8EAF6), Color(0xFFE3F2FD), Color(0xFFE0F2F1),
                                Color(0xFFE8F5E9), Color(0xFFFFFDE7), Color(0xFFFFF3E0)
                            )
                            backgroundColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { canvasBgColor = color }
                                        .then(
                                            if (canvasBgColor == color) Modifier.background(color.copy(alpha = 0.5f)) else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (canvasBgColor == color) {
                                        Box(Modifier.size(12.dp).clip(CircleShape).background(AppColors.Maldicion))
                                    }
                                }
                            }
                        }
                    }

                    // Textos
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showTexts, onCheckedChange = { showTexts = it })
                            Text("Mostrar mensajes finales", color = Color.White)
                        }

                        if (showTexts) {
                            OutlinedTextField(
                                value = customMessage,
                                onValueChange = { customMessage = it },
                                label = { Text("Mensaje principal") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    focusedBorderColor = AppColors.Maldicion
                                )
                            )
                            OutlinedTextField(
                                value = customSubTitle,
                                onValueChange = { customSubTitle = it },
                                label = { Text("Sub-mensaje") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White,
                                    focusedBorderColor = AppColors.Maldicion
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showSettings = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Maldicion)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

/**
 * Función para guardar el dibujo actual en la galería del dispositivo.
 */
fun saveDrawingAsImage(
    context: android.content.Context,
    steps: List<PixelArtRepository.StrokeStep>,
    gridSize: Int,
    bgColor: Color,
    message: String,
    subMessage: String,
    showTexts: Boolean
) {
    try {
        val artSize = 1024 // Tamaño del área del dibujo
        val hasText = showTexts && (message.isNotEmpty() || subMessage.isNotEmpty())
        val footerHeight = if (hasText) 180 else 0
        
        val bitmap = Bitmap.createBitmap(artSize, artSize + footerHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Fondo completo
        paint.color = bgColor.toArgb()
        canvas.drawRect(0f, 0f, artSize.toFloat(), (artSize + footerHeight).toFloat(), paint)

        // 2. Píxeles
        val cellSize = artSize.toFloat() / gridSize
        
        // Reconstruir el estado final
        val finalPixels = MutableList<Color?>(gridSize * gridSize) { null }
        steps.forEach { step ->
            if (step.pixelIndex in finalPixels.indices) {
                finalPixels[step.pixelIndex] = step.color
            }
        }

        finalPixels.forEachIndexed { idx, color ->
            if (color != null) {
                paint.color = color.toArgb()
                val row = idx / gridSize
                val col = idx % gridSize
                canvas.drawRect(
                    col * cellSize,
                    row * cellSize,
                    (col + 1) * cellSize,
                    (row + 1) * cellSize,
                    paint
                )
            }
        }

        // 3. Dibujar Textos si están habilitados
        if (hasText) {
            paint.textAlign = Paint.Align.CENTER
            
            if (message.isNotEmpty()) {
                paint.color = textColorFor(bgColor).toArgb() // Uso de ColorUtils
                paint.textSize = 44f
                paint.isFakeBoldText = true
                canvas.drawText(message, artSize / 2f, artSize + 70f, paint)
            }

            if (subMessage.isNotEmpty()) {
                paint.color = subtitleColorFor(bgColor).toArgb() // Uso de ColorUtils
                paint.textSize = 28f
                paint.isFakeBoldText = false
                canvas.drawText(subMessage, artSize / 2f, artSize + 130f, paint)
            }
        }

        // 4. Guardar en MediaStore
        val filename = "PixelArt_${System.currentTimeMillis()}.png"
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/TrazoATrazo")
            }
        }

        val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            Toast.makeText(context, "Imagen guardada en Galería", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
