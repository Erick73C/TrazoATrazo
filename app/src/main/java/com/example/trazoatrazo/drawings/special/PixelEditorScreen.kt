package com.example.trazoatrazo.drawings.special

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trazoatrazo.ui.components.BackMenuButton
import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.trazoatrazo.data.PixelArtwork
import com.example.trazoatrazo.presentation.pixeleditor.PixelArtViewModel
import com.example.trazoatrazo.ui.theme.AppColors

// ── Paleta visual del editor ───────────────────────────────────────────────────
private val CheckerLight  = Color(0xFFF2F2F2)
private val CheckerDark   = Color(0xFFD8D8D8)
private val GridLineColor = Color.Black.copy(alpha = 0.08f)

private val basePalette = listOf(
    Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFFEB3B), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA),
    Color(0xFFEC407A), Color(0xFF6D4C41), Color(0xFF9E9E9E)
)

private val gridSizes = listOf(8, 16, 32)

// ── Herramientas disponibles ───────────────────────────────────────────────────
private enum class PixelTool(val emoji: String, val label: String) {
    PENCIL("✏️", "Lápiz"),
    ERASER("🧽", "Borrador"),
    FILL("🪣", "Relleno"),
    EYEDROPPER("💧", "Gotero")
}

// ── Pantalla principal ─────────────────────────────────────────────────────────
@Composable
fun PixelEditorScreen(
    viewModel: PixelArtViewModel,
    existingArtworkId: String? = null,
    onBack: () -> Unit
) {
    // 🆕 Si venimos de "Mis Creaciones", cargamos esa obra
    val existing = remember(existingArtworkId) { existingArtworkId?.let(viewModel::findById) }
    val artworkId = remember { existing?.id ?: viewModel.newId() }

    var gridSize by remember { mutableIntStateOf(existing?.gridSize ?: 16) }
    var pendingGridSize by remember { mutableStateOf<Int?>(null) }

    // Cada píxel guarda su propio color (null = transparente)
    val pixels = remember(gridSize) {
        mutableStateListOf<Color?>().apply {
            if (existing != null && existing.gridSize == gridSize) {
                addAll(existing.pixels)
            } else {
                repeat(gridSize * gridSize) { add(null) }
            }
        }
    }
    val undoStack = remember(gridSize) { mutableStateListOf<List<Color?>>() }

    var selectedColor   by remember { mutableStateOf(Color.Black) }
    var selectedTool    by remember { mutableStateOf(PixelTool.PENCIL) }
    var showGridLines   by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSaveDialog  by remember { mutableStateOf(false) }   // 🆕

    val screenAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    // ── Lógica de edición ───────────────────────────────────────────────────────
    fun pushUndo() {
        if (undoStack.size >= 25) undoStack.removeAt(0)
        undoStack.add(pixels.toList())
    }

    fun undo() {
        val last = undoStack.removeLastOrNull() ?: return
        pixels.clear()
        pixels.addAll(last)
    }

    fun clearCanvas() {
        if (pixels.any { it != null }) pushUndo()
        pixels.clear()
        repeat(gridSize * gridSize) { pixels.add(null) }
    }

    fun requestGridSizeChange(newSize: Int) {
        if (newSize == gridSize) return
        if (pixels.any { it != null }) {
            pendingGridSize = newSize
        } else {
            gridSize = newSize
        }
    }

    fun applyToolAt(index: Int, isFirstTouch: Boolean) {
        if (index !in pixels.indices) return
        when (selectedTool) {
            PixelTool.PENCIL -> {
                if (isFirstTouch) pushUndo()
                pixels[index] = selectedColor
            }
            PixelTool.ERASER -> {
                if (isFirstTouch) pushUndo()
                pixels[index] = null
            }
            PixelTool.FILL -> {
                if (isFirstTouch) {
                    pushUndo()
                    val filled = floodFill(pixels.toMutableList(), gridSize, index, selectedColor)
                    pixels.clear()
                    pixels.addAll(filled)
                }
            }
            PixelTool.EYEDROPPER -> {
                if (isFirstTouch) {
                    pixels[index]?.let { selectedColor = it }
                    selectedTool = PixelTool.PENCIL
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Vacio)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAnim.value)
        ) {
            PixelEditorHeader(
                onBack      = onBack,
                gridSize    = gridSize,
                onSaveClick = { showSaveDialog = true }   // 🆕
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Lienzo ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, AppColors.Maldicion.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                ) {
                    PixelCanvas(
                        pixels        = pixels,
                        gridSize      = gridSize,
                        showGridLines = showGridLines,
                        onPointerDown = { idx -> applyToolAt(idx, isFirstTouch = true) },
                        onPointerMove = { idx ->
                            if (selectedTool == PixelTool.PENCIL || selectedTool == PixelTool.ERASER) {
                                applyToolAt(idx, isFirstTouch = false)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                // ── Tamaño de cuadrícula ──────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    gridSizes.forEach { size ->
                        GridSizeChip(
                            size       = size,
                            isSelected = size == gridSize,
                            onClick    = { requestGridSizeChange(size) }
                        )
                    }
                }

                // ── Herramientas ──────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelTool.entries.forEach { tool ->
                        ToolButton(
                            emoji      = tool.emoji,
                            label      = tool.label,
                            isSelected = tool == selectedTool,
                            onClick    = { selectedTool = tool }
                        )
                    }
                }

                // ── Acciones: deshacer · cuadrícula · limpiar ─────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconActionButton(
                        emoji   = "↩️",
                        enabled = undoStack.isNotEmpty(),
                        onClick = { undo() }
                    )
                    IconActionButton(
                        emoji   = if (showGridLines) "▦" else "▢",
                        onClick = { showGridLines = !showGridLines }
                    )
                    IconActionButton(
                        emoji   = "🗑️",
                        onClick = { clearCanvas() }
                    )
                }

                // ── Paleta de colores ─────────────────────────────────────────
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedColor)
                            .border(2.dp, AppColors.Reversa.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(AppColors.Eco.copy(alpha = 0.25f))
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(basePalette) { color ->
                            ColorSwatchButton(
                                color      = color,
                                isSelected = color == selectedColor,
                                onClick    = { selectedColor = color }
                            )
                        }
                        item { CustomColorButton(onClick = { showColorPicker = true }) }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor    = selectedColor,
            onColorSelected = { selectedColor = it },
            onDismiss       = { showColorPicker = false }
        )
    }

    pendingGridSize?.let { target ->
        ResizeConfirmDialog(
            targetSize = target,
            onConfirm  = {
                gridSize = target
                pendingGridSize = null
            },
            onDismiss  = { pendingGridSize = null }
        )
    }

    // 🆕 Diálogo para nombrar y guardar la creación
    if (showSaveDialog) {
        SaveArtworkDialog(
            initialName = existing?.name ?: "",
            onConfirm   = { name ->
                viewModel.save(
                    PixelArtwork(
                        id = artworkId,
                        name = name,
                        gridSize = gridSize,
                        pixels = pixels.toList(),
                        createdAt = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
                showSaveDialog = false
                onBack()   // vuelve a "Mis Creaciones" ya guardado
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

// 🆕 ── Diálogo para nombrar y guardar ───────────────────────────────────────────
@Composable
private fun SaveArtworkDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Sombra)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("💾 Guardar creación", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.Reversa)

                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    placeholder   = { Text("Mi creación") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.Eco) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name.ifBlank { "Mi creación" }) },
                        colors  = ButtonDefaults.buttonColors(containerColor = AppColors.Maldicion)
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Diálogo de confirmación de cambio de tamaño ──────────────────────────────────
@Composable
private fun ResizeConfirmDialog(
    targetSize: Int,
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
                Text(
                    text       = "⚠️ Cambiar tamaño de cuadrícula",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Reversa
                )
                Text(
                    text       = "Cambiar a ${targetSize}×${targetSize} borrará tu dibujo actual. ¿Quieres continuar?",
                    fontSize   = 13.sp,
                    color      = AppColors.Eco,
                    lineHeight = 18.sp
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = AppColors.Eco)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors  = ButtonDefaults.buttonColors(containerColor = AppColors.Sukuna)
                    ) {
                        Text("Cambiar y borrar", color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Header (atrás · título · guardar) ───────────────────────────────────────────
// Nota: con 3 elementos uso Row + weight(1f) en vez del patrón Box+align()
// que usas en SettingsHeader, para que el título nunca se encime con los botones.
@Composable
private fun PixelEditorHeader(
    onBack: () -> Unit,
    gridSize: Int,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 52.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Sombra)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBack
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("←", fontSize = 15.sp, color = AppColors.ReversaSuave)
        }

        Column(
            modifier            = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "🎨 Editor de Píxeles",
                fontSize   = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = AppColors.Reversa,
                maxLines   = 1
            )
            Text(
                text     = "${gridSize}×${gridSize} px",
                fontSize = 10.sp,
                color    = AppColors.Eco
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Maldicion)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onSaveClick
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("💾", fontSize = 15.sp, color = Color.White)
        }
    }
}

// ── Canvas del lienzo + gestos de pintar ───────────────────────────────────────
@Composable
private fun PixelCanvas(
    pixels: List<Color?>,
    gridSize: Int,
    showGridLines: Boolean,
    onPointerDown: (Int) -> Unit,
    onPointerMove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .pointerInput(gridSize) {
                awaitEachGesture {
                    val canvasPx = size.width.toFloat()
                    val down = awaitFirstDown()
                    offsetToIndex(down.position, canvasPx, gridSize)?.let(onPointerDown)
                    while (true) {
                        val event  = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        offsetToIndex(change.position, canvasPx, gridSize)?.let(onPointerMove)
                        change.consume()
                    }
                }
            }
    ) {
        val cell = size.width / gridSize

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val idx     = row * gridSize + col
                val checker = if ((row + col) % 2 == 0) CheckerLight else CheckerDark
                val color   = pixels.getOrNull(idx) ?: checker
                drawRect(
                    color   = color,
                    topLeft = Offset(col * cell, row * cell),
                    size    = Size(cell, cell)
                )
            }
        }

        if (showGridLines) {
            for (i in 0..gridSize) {
                val pos = i * cell
                drawLine(GridLineColor, Offset(pos, 0f), Offset(pos, size.height), 1f)
                drawLine(GridLineColor, Offset(0f, pos), Offset(size.width, pos), 1f)
            }
        }
    }
}

// ── Helpers de mapeo y relleno ──────────────────────────────────────────────────
private fun offsetToIndex(offset: Offset, canvasPx: Float, gridSize: Int): Int? {
    if (canvasPx <= 0f) return null
    val cell = canvasPx / gridSize
    val col  = (offset.x / cell).toInt().coerceIn(0, gridSize - 1)
    val row  = (offset.y / cell).toInt().coerceIn(0, gridSize - 1)
    return row * gridSize + col
}

private fun floodFill(
    list: MutableList<Color?>,
    gridSize: Int,
    startIndex: Int,
    newColor: Color
): MutableList<Color?> {
    if (startIndex !in list.indices) return list
    val target = list[startIndex]
    if (target == newColor) return list

    val visited = BooleanArray(list.size)
    val queue   = ArrayDeque<Int>()
    queue.add(startIndex)
    visited[startIndex] = true

    while (queue.isNotEmpty()) {
        val idx = queue.removeFirst()
        list[idx] = newColor
        val row = idx / gridSize
        val col = idx % gridSize

        if (col > 0)            tryEnqueue(list, queue, visited, idx - 1, target)
        if (col < gridSize - 1) tryEnqueue(list, queue, visited, idx + 1, target)
        if (row > 0)            tryEnqueue(list, queue, visited, idx - gridSize, target)
        if (row < gridSize - 1) tryEnqueue(list, queue, visited, idx + gridSize, target)
    }
    return list
}

private fun tryEnqueue(
    list: List<Color?>, queue: ArrayDeque<Int>, visited: BooleanArray, idx: Int, target: Color?
) {
    if (!visited[idx] && list[idx] == target) {
        visited[idx] = true
        queue.add(idx)
    }
}

// ── Botón de herramienta ────────────────────────────────────────────────────────
@Composable
private fun ToolButton(emoji: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (isSelected) AppColors.Maldicion.copy(alpha = 0.25f) else AppColors.Sombra,
        label = "toolBg"
    )
    val borderColor by animateColorAsState(
        if (isSelected) AppColors.Maldicion else Color.Transparent,
        label = "toolBorder"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 19.sp)
        Text(label, fontSize = 9.sp, color = AppColors.Eco)
    }
}

// ── Chip de tamaño de cuadrícula ─────────────────────────────────────────────────
@Composable
private fun GridSizeChip(size: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AppColors.Maldicion else AppColors.Sombra)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text       = "${size}×${size}",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isSelected) Color.White else AppColors.Eco
        )
    }
}

// ── Botón de icono (deshacer / cuadrícula / limpiar) ────────────────────────────
@Composable
private fun IconActionButton(emoji: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.Sombra)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = enabled,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 16.sp)
    }
}

// ── Swatch de color de la paleta ─────────────────────────────────────────────────
@Composable
private fun ColorSwatchButton(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) AppColors.Tecnica else AppColors.Eco.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
    )
}

// ── Botón "color personalizado" (abre el picker) ─────────────────────────────────
@Composable
private fun CustomColorButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFFE53935), Color(0xFFFFEB3B), Color(0xFF43A047),
                        Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFE53935)
                    )
                )
            )
            .border(1.dp, AppColors.Eco.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// ── Diálogo de color personalizado (HSV) ─────────────────────────────────────────
@Composable
private fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHsv = remember {
        val out = FloatArray(3)
        AndroidColor.colorToHSV(initialColor.toArgb(), out)
        out
    }
    var hue        by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColor = remember(hue, saturation, brightness) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness)))
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Sombra)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text(
                    text       = "🎨 Color personalizado",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Reversa
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentColor)
                        .border(1.dp, AppColors.Reversa.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )

                ColorSliderRow(label = "Tono",        value = hue,        range = 0f..360f, onValue = { hue = it })
                ColorSliderRow(label = "Saturación",  value = saturation, range = 0f..1f,   onValue = { saturation = it })
                ColorSliderRow(label = "Brillo",      value = brightness, range = 0f..1f,   onValue = { brightness = it })

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = AppColors.Eco)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onColorSelected(currentColor)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Maldicion)
                    ) {
                        Text("Usar color", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, color = AppColors.Eco)
        Slider(
            value         = value,
            onValueChange = onValue,
            valueRange    = range,
            colors = SliderDefaults.colors(
                thumbColor         = AppColors.Tecnica,
                activeTrackColor   = AppColors.Maldicion,
                inactiveTrackColor = AppColors.Dominio
            )
        )
    }
}