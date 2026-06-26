package com.example.trazoatrazo.drawings.special

import android.graphics.Color as AndroidColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.trazoatrazo.data.PixelArtwork
import com.example.trazoatrazo.presentation.pixeleditor.PixelArtViewModel
import com.example.trazoatrazo.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlin.math.*

// ── Paleta visual del editor ───────────────────────────────────────────────────
private val CheckerLight  = Color(0xFFE8E8E8)
private val CheckerDark   = Color(0xFFCCCCCC)
private val GridLineColor = Color.Black.copy(alpha = 0.12f)
private val SelectionBorderColor = Color(0xFF29B6F6)

private val basePalette = listOf(
    Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFFE53935), Color(0xFFFB8C00),
    Color(0xFFFFEB3B), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA),
    Color(0xFFEC407A), Color(0xFF6D4C41), Color(0xFF9E9E9E)
)

private val gridSizes = listOf(8, 16, 32)
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 4f

// ── Herramientas disponibles ───────────────────────────────────────────────────
private enum class PixelTool(val emoji: String, val label: String) {
    PENCIL("✏️", "Lápiz"),
    LINE("📏", "Línea"),
    SQUARE("⬛", "Cuadrado"),
    CIRCLE("⚪", "Círculo"),
    SELECT("⬚", "Selección"),
    FILL("🪣", "Relleno"),
    ERASER("🧽", "Borrador"),
    EYEDROPPER("💧", "Gotero")
}

private enum class PostSaveAction { EXIT, REPLAY }

// ── Snapshot para deshacer/rehacer (pixels + orden de trazo, siempre juntos) ───
private data class EditorSnapshot(val pixels: List<Color?>, val paintOrder: List<Int>)

// ── Rectángulo de selección, en coordenadas de fila/columna del grid ───────────
private data class SelRect(val minRow: Int, val maxRow: Int, val minCol: Int, val maxCol: Int)

// ── Lo que se está "moviendo" en este instante: qué se oculta y qué flota ──────
private data class MoveOverlay(val hiddenIndices: Set<Int>, val floatingPixels: List<Pair<Int, Color>>)

// ── Pantalla principal ─────────────────────────────────────────────────────────
@Composable
fun PixelEditorScreen(
    viewModel: PixelArtViewModel,
    existingArtworkId: String? = null,
    onBack: () -> Unit,
    onReplay: (artworkId: String) -> Unit
) {
    val existing = remember(existingArtworkId) { existingArtworkId?.let(viewModel::findById) }
    val artworkId = remember { existing?.id ?: viewModel.newId() }

    var gridSize by remember { mutableIntStateOf(existing?.gridSize ?: 16) }
    var pendingGridSize by remember { mutableStateOf<Int?>(null) }

    val pixels = remember(gridSize) {
        mutableStateListOf<Color?>().apply {
            if (existing != null && existing.gridSize == gridSize) addAll(existing.pixels)
            else repeat(gridSize * gridSize) { add(null) }
        }
    }
    // Orden en que se pintó cada índice por primera vez (para "trazo a trazo")
    val paintOrder = remember(gridSize) {
        mutableStateListOf<Int>().apply {
            if (existing != null && existing.gridSize == gridSize) addAll(existing.paintOrder)
        }
    }
    val undoStack = remember(gridSize) { mutableStateListOf<EditorSnapshot>() }
    val redoStack = remember(gridSize) { mutableStateListOf<EditorSnapshot>() }

    var selectedColor   by remember { mutableStateOf(Color.Black) }
    var selectedTool    by remember { mutableStateOf(PixelTool.PENCIL) }
    var showGridLines   by remember { mutableStateOf(true) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSaveDialog  by remember { mutableStateOf(false) }
    var postSaveAction  by remember { mutableStateOf(PostSaveAction.EXIT) }

    // Zoom y desplazamiento del lienzo
    var zoomLevel by remember(gridSize) { mutableFloatStateOf(1f) }
    val hScrollState = remember(gridSize) { ScrollState(0) }
    val vScrollState = remember(gridSize) { ScrollState(0) }

    // Estado de las herramientas de figura (línea / cuadrado / círculo)
    var shapeStartIndex by remember(gridSize) { mutableStateOf<Int?>(null) }
    var shapeEndIndex   by remember(gridSize) { mutableStateOf<Int?>(null) }
    var shapeFilled     by remember { mutableStateOf(false) }

    // Estado de selección + mover
    var selectionRect       by remember(gridSize) { mutableStateOf<SelRect?>(null) }
    var selDragStartIndex   by remember(gridSize) { mutableStateOf<Int?>(null) }
    var selDragCurrentIndex by remember(gridSize) { mutableStateOf<Int?>(null) }
    var moveAnchorIndex     by remember(gridSize) { mutableStateOf<Int?>(null) }
    var moveDelta           by remember(gridSize) { mutableStateOf(0 to 0) }

    val screenAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        screenAnim.animateTo(1f, tween(500, easing = EaseOutCubic))
    }

    // ── Registro de orden de trazo ──────────────────────────────────────────────
    fun recordPaint(index: Int) {
        if (index !in paintOrder) paintOrder.add(index)
    }
    fun recordErase(index: Int) {
        paintOrder.remove(index)
    }

    // ── Deshacer / Rehacer ──────────────────────────────────────────────────────
    fun pushUndo() {
        if (undoStack.size >= 25) undoStack.removeAt(0)
        undoStack.add(EditorSnapshot(pixels.toList(), paintOrder.toList()))
        redoStack.clear()
    }
    fun undo() {
        val last = undoStack.removeLastOrNull() ?: return
        redoStack.add(EditorSnapshot(pixels.toList(), paintOrder.toList()))
        pixels.clear(); pixels.addAll(last.pixels)
        paintOrder.clear(); paintOrder.addAll(last.paintOrder)
    }
    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.add(EditorSnapshot(pixels.toList(), paintOrder.toList()))
        pixels.clear(); pixels.addAll(next.pixels)
        paintOrder.clear(); paintOrder.addAll(next.paintOrder)
    }
    fun clearCanvas() {
        if (pixels.any { it != null }) pushUndo()
        pixels.clear()
        repeat(gridSize * gridSize) { pixels.add(null) }
        paintOrder.clear()
        selectionRect = null
    }

    fun requestGridSizeChange(newSize: Int) {
        if (newSize == gridSize) return
        if (pixels.any { it != null }) pendingGridSize = newSize else gridSize = newSize
    }

    // Cuando un segundo dedo entra a medio gesto, cancela el toque/arrastre accidental
    fun cancelAccidentalFirstTouch() {
        when (selectedTool) {
            PixelTool.PENCIL, PixelTool.ERASER, PixelTool.FILL -> undo()
            PixelTool.LINE, PixelTool.SQUARE, PixelTool.CIRCLE -> {
                shapeStartIndex = null; shapeEndIndex = null
            }
            PixelTool.SELECT -> {
                selDragStartIndex = null; selDragCurrentIndex = null
                moveAnchorIndex = null; moveDelta = 0 to 0
            }
            PixelTool.EYEDROPPER -> { /* nada que cancelar de forma segura */ }
        }
    }

    fun applyToolAt(index: Int, isFirstTouch: Boolean) {
        if (index !in pixels.indices) return
        when (selectedTool) {
            PixelTool.PENCIL -> {
                if (isFirstTouch) pushUndo()
                pixels[index] = selectedColor
                recordPaint(index)
            }
            PixelTool.ERASER -> {
                if (isFirstTouch) pushUndo()
                pixels[index] = null
                recordErase(index)
            }
            PixelTool.FILL -> {
                if (isFirstTouch) {
                    pushUndo()
                    val targetColor = pixels.getOrNull(index)
                    if (targetColor != selectedColor) {
                        floodFillIndices(pixels, gridSize, index).forEach { idx ->
                            pixels[idx] = selectedColor
                            recordPaint(idx)
                        }
                    }
                }
            }
            PixelTool.EYEDROPPER -> {
                if (isFirstTouch) {
                    pixels[index]?.let { selectedColor = it }
                    selectedTool = PixelTool.PENCIL
                }
            }
            PixelTool.LINE, PixelTool.SQUARE, PixelTool.CIRCLE -> {
                if (isFirstTouch) shapeStartIndex = index
                shapeEndIndex = index
            }
            PixelTool.SELECT -> { /* manejado por handleSelectDown/Move */ }
        }
    }

    // Sella la figura (línea/cuadrado/círculo) al soltar el dedo
    fun commitShapeIfNeeded() {
        val start = shapeStartIndex
        val end   = shapeEndIndex
        if (start != null && end != null) {
            val indices = when (selectedTool) {
                PixelTool.LINE   -> bresenhamLine(start, end, gridSize)
                PixelTool.SQUARE -> rectangleIndices(start, end, gridSize, shapeFilled)
                PixelTool.CIRCLE -> ellipseIndices(start, end, gridSize, shapeFilled)
                else -> emptyList()
            }
            if (indices.isNotEmpty()) {
                pushUndo()
                indices.forEach { idx ->
                    if (idx in pixels.indices) {
                        pixels[idx] = selectedColor
                        recordPaint(idx)
                    }
                }
            }
        }
        shapeStartIndex = null
        shapeEndIndex   = null
    }

    // ── Selección: empezar selección nueva o empezar a mover la existente ──────
    fun handleSelectDown(index: Int) {
        val rect = selectionRect
        val row = index / gridSize
        val col = index % gridSize
        if (rect != null && row in rect.minRow..rect.maxRow && col in rect.minCol..rect.maxCol) {
            moveAnchorIndex = index
            moveDelta = 0 to 0
        } else {
            selectionRect = null
            selDragStartIndex = index
            selDragCurrentIndex = index
        }
    }

    fun handleSelectMove(index: Int) {
        val anchor = moveAnchorIndex
        val rect = selectionRect
        if (anchor != null && rect != null) {
            val anchorRow = anchor / gridSize; val anchorCol = anchor % gridSize
            val curRow = index / gridSize;     val curCol = index % gridSize
            val dRow = (curRow - anchorRow).coerceIn(-rect.minRow, (gridSize - 1) - rect.maxRow)
            val dCol = (curCol - anchorCol).coerceIn(-rect.minCol, (gridSize - 1) - rect.maxCol)
            moveDelta = dRow to dCol
        } else if (selDragStartIndex != null) {
            selDragCurrentIndex = index
        }
    }

    // Aplica el movimiento de verdad sobre los píxeles
    fun commitMove() {
        val rect = selectionRect ?: return
        val (dRow, dCol) = moveDelta
        if (dRow == 0 && dCol == 0) return

        pushUndo()

        val moved = buildList {
            for (row in rect.minRow..rect.maxRow) {
                for (col in rect.minCol..rect.maxCol) {
                    add(Triple(row, col, pixels.getOrNull(row * gridSize + col)))
                }
            }
        }
        moved.forEach { (row, col, _) ->
            val idx = row * gridSize + col
            pixels[idx] = null
            paintOrder.remove(idx)
        }
        moved.forEach { (row, col, color) ->
            val newIdx = (row + dRow) * gridSize + (col + dCol)
            if (newIdx in pixels.indices) {
                pixels[newIdx] = color
                if (color != null) recordPaint(newIdx) else paintOrder.remove(newIdx)
            }
        }
        selectionRect = SelRect(
            minRow = rect.minRow + dRow, maxRow = rect.maxRow + dRow,
            minCol = rect.minCol + dCol, maxCol = rect.maxCol + dCol
        )
    }

    fun handleSelectUp() {
        if (moveAnchorIndex != null) {
            commitMove()
            moveAnchorIndex = null
            moveDelta = 0 to 0
        } else if (selDragStartIndex != null && selDragCurrentIndex != null) {
            val r0 = selDragStartIndex!! / gridSize; val c0 = selDragStartIndex!! % gridSize
            val r1 = selDragCurrentIndex!! / gridSize; val c1 = selDragCurrentIndex!! % gridSize
            selectionRect = SelRect(minOf(r0, r1), maxOf(r0, r1), minOf(c0, c1), maxOf(c0, c1))
            selDragStartIndex = null
            selDragCurrentIndex = null
        }
    }

    // ── Vista previa de figura (línea/cuadrado/círculo) ─────────────────────────
    val previewIndices =
        if (shapeStartIndex != null && shapeEndIndex != null) {
            when (selectedTool) {
                PixelTool.LINE   -> bresenhamLine(shapeStartIndex!!, shapeEndIndex!!, gridSize)
                PixelTool.SQUARE -> rectangleIndices(shapeStartIndex!!, shapeEndIndex!!, gridSize, shapeFilled)
                PixelTool.CIRCLE -> ellipseIndices(shapeStartIndex!!, shapeEndIndex!!, gridSize, shapeFilled)
                else -> emptyList()
            }
        } else emptyList()

    // ── Rectángulo de selección a dibujar (guardado, en vivo, o desplazándose) ──
    val displaySelectionRect: SelRect? = when {
        selDragStartIndex != null && selDragCurrentIndex != null -> {
            val r0 = selDragStartIndex!! / gridSize; val c0 = selDragStartIndex!! % gridSize
            val r1 = selDragCurrentIndex!! / gridSize; val c1 = selDragCurrentIndex!! % gridSize
            SelRect(minOf(r0, r1), maxOf(r0, r1), minOf(c0, c1), maxOf(c0, c1))
        }
        moveAnchorIndex != null && selectionRect != null -> {
            val rect = selectionRect!!
            val (dRow, dCol) = moveDelta
            SelRect(rect.minRow + dRow, rect.maxRow + dRow, rect.minCol + dCol, rect.maxCol + dCol)
        }
        else -> selectionRect
    }

    // ── Mientras se mueve: qué se oculta del lienzo y qué flota encima ──────────
    val moveOverlay: MoveOverlay? = if (moveAnchorIndex != null && selectionRect != null) {
        val rect = selectionRect!!
        val (dRow, dCol) = moveDelta
        val hidden = mutableSetOf<Int>()
        val floating = mutableListOf<Pair<Int, Color>>()
        for (row in rect.minRow..rect.maxRow) {
            for (col in rect.minCol..rect.maxCol) {
                val origIdx = row * gridSize + col
                hidden.add(origIdx)
                pixels.getOrNull(origIdx)?.let { color ->
                    val newIdx = (row + dRow) * gridSize + (col + dCol)
                    if (newIdx in pixels.indices) floating.add(newIdx to color)
                }
            }
        }
        MoveOverlay(hidden, floating)
    } else null

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
                onBack        = onBack,
                gridSize      = gridSize,
                canReplay     = paintOrder.isNotEmpty(),
                onSaveClick   = { postSaveAction = PostSaveAction.EXIT; showSaveDialog = true },
                onReplayClick = { postSaveAction = PostSaveAction.REPLAY; showSaveDialog = true }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Lienzo con zoom + scroll ────────────────────────────────────
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.95f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.2.dp, AppColors.Maldicion.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                ) {
                    val scope = this
                    val canvasSize = scope.maxWidth * zoomLevel

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(hScrollState)
                            .verticalScroll(vScrollState)
                    ) {
                        PixelCanvas(
                            pixels         = pixels,
                            gridSize       = gridSize,
                            showGridLines  = showGridLines,
                            hScrollState   = hScrollState,
                            vScrollState   = vScrollState,
                            previewIndices = previewIndices,
                            previewColor   = selectedColor,
                            selectionRect  = displaySelectionRect,
                            moveOverlay    = moveOverlay,
                            onPointerDown  = { idx ->
                                if (selectedTool == PixelTool.SELECT) handleSelectDown(idx)
                                else applyToolAt(idx, isFirstTouch = true)
                            },
                            onPointerMove  = { idx ->
                                when {
                                    selectedTool == PixelTool.SELECT -> handleSelectMove(idx)
                                    selectedTool == PixelTool.PENCIL || selectedTool == PixelTool.ERASER ||
                                            selectedTool == PixelTool.LINE   || selectedTool == PixelTool.SQUARE ||
                                            selectedTool == PixelTool.CIRCLE -> applyToolAt(idx, isFirstTouch = false)
                                }
                            },
                            onPointerUp   = {
                                if (selectedTool == PixelTool.SELECT) handleSelectUp() else commitShapeIfNeeded()
                            },
                            onPanDetected = { cancelAccidentalFirstTouch() },
                            modifier      = Modifier.size(canvasSize)
                        )
                    }
                }

                // ── Zoom ──────────────────────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconActionButton(
                        emoji   = "➖",
                        enabled = zoomLevel > MIN_ZOOM,
                        onClick = { zoomLevel = (zoomLevel - 0.5f).coerceAtLeast(MIN_ZOOM) }
                    )
                    Text(
                        text       = "${(zoomLevel * 100).toInt()}%",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = AppColors.Eco
                    )
                    IconActionButton(
                        emoji   = "➕",
                        enabled = zoomLevel < MAX_ZOOM,
                        onClick = { zoomLevel = (zoomLevel + 0.5f).coerceAtMost(MAX_ZOOM) }
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

                // ── Herramientas (scrollable) ─────────────────────────────────
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(PixelTool.entries) { tool ->
                        ToolButton(
                            emoji      = tool.emoji,
                            label      = tool.label,
                            isSelected = tool == selectedTool,
                            onClick    = {
                                selectedTool = tool
                                shapeStartIndex = null; shapeEndIndex = null
                                selDragStartIndex = null; selDragCurrentIndex = null
                                moveAnchorIndex = null; moveDelta = 0 to 0
                            }
                        )
                    }
                }

                // ── Borde / Relleno (solo para Cuadrado y Círculo) ────────────
                if (selectedTool == PixelTool.SQUARE || selectedTool == PixelTool.CIRCLE) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Modo:", fontSize = 12.sp, color = AppColors.Eco)
                        ShapeModeChip(label = "Borde",   selected = !shapeFilled, onClick = { shapeFilled = false })
                        ShapeModeChip(label = "Relleno", selected = shapeFilled,  onClick = { shapeFilled = true })
                    }
                }

                // ── Aviso + quitar selección (solo para Selección) ────────────
                if (selectedTool == PixelTool.SELECT && selectionRect != null) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text     = "Arrastra dentro del recuadro para moverlo",
                            fontSize = 11.sp,
                            color    = AppColors.Eco
                        )
                        IconActionButton(emoji = "✕", onClick = { selectionRect = null })
                    }
                }

                // ── Acciones: deshacer · rehacer · cuadrícula · limpiar ───────
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconActionButton(
                        emoji   = "↩️",
                        enabled = undoStack.isNotEmpty(),
                        onClick = { undo() }
                    )
                    IconActionButton(
                        emoji   = "↪️",
                        enabled = redoStack.isNotEmpty(),
                        onClick = { redo() }
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedColor)
                            .border(2.dp, AppColors.Reversa.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
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
            onConfirm  = { gridSize = target; pendingGridSize = null },
            onDismiss  = { pendingGridSize = null }
        )
    }

    if (showSaveDialog) {
        SaveArtworkDialog(
            initialName = existing?.name ?: "",
            onConfirm   = { name ->
                viewModel.save(
                    PixelArtwork(
                        id         = artworkId,
                        name       = name,
                        gridSize   = gridSize,
                        pixels     = pixels.toList(),
                        paintOrder = paintOrder.toList(),
                        createdAt  = existing?.createdAt ?: System.currentTimeMillis()
                    )
                )
                showSaveDialog = false
                when (postSaveAction) {
                    PostSaveAction.EXIT   -> onBack()
                    PostSaveAction.REPLAY -> onReplay(artworkId)
                }
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

// ── Diálogo para nombrar y guardar ───────────────────────────────────────────────
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
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = AppColors.Eco) }
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

// ── Header (atrás · título · ver trazo · guardar) ────────────────────────────────
@Composable
private fun PixelEditorHeader(
    onBack: () -> Unit,
    gridSize: Int,
    canReplay: Boolean,
    onSaveClick: () -> Unit,
    onReplayClick: () -> Unit
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .alpha(if (canReplay) 1f else 0.4f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Dominio)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        enabled           = canReplay,
                        onClick           = onReplayClick
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("▶️", fontSize = 15.sp)
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
}

// ── Canvas del lienzo + gestos de pintar / figura / selección / pan con 2 dedos ─
@Composable
private fun PixelCanvas(
    pixels: List<Color?>,
    gridSize: Int,
    showGridLines: Boolean,
    hScrollState: ScrollState,
    vScrollState: ScrollState,
    previewIndices: List<Int>,
    previewColor: Color,
    selectionRect: SelRect?,
    moveOverlay: MoveOverlay?,
    onPointerDown: (Int) -> Unit,
    onPointerMove: (Int) -> Unit,
    onPointerUp: () -> Unit,
    onPanDetected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Canvas(
        modifier = modifier
            .pointerInput(gridSize) {
                awaitEachGesture {
                    val canvasPx = size.width.toFloat()
                    var enteredPanMode = false
                    var lastCentroid: Offset? = null

                    val down = awaitFirstDown()
                    offsetToIndex(down.position, canvasPx, gridSize)?.let(onPointerDown)

                    while (true) {
                        val event   = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }

                        if (pressed.isEmpty()) break

                        if (pressed.size >= 2) {
                            if (!enteredPanMode) onPanDetected()
                            enteredPanMode = true
                            val centroid = pressed.fold(Offset.Zero) { acc, c -> acc + c.position } / pressed.size.toFloat()
                            lastCentroid?.let { prev ->
                                val delta = centroid - prev
                                coroutineScope.launch {
                                    hScrollState.scrollBy(-delta.x)
                                    vScrollState.scrollBy(-delta.y)
                                }
                            }
                            lastCentroid = centroid
                            pressed.forEach { it.consume() }
                        } else {
                            lastCentroid = null
                            if (!enteredPanMode) {
                                val c = pressed.first()
                                offsetToIndex(c.position, canvasPx, gridSize)?.let(onPointerMove)
                                c.consume()
                            }
                        }
                    }
                    onPointerUp()
                }
            }
    ) {
        val cell = size.width / gridSize
        val hiddenIndices = moveOverlay?.hiddenIndices ?: emptySet()

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val idx     = row * gridSize + col
                val checker = if ((row + col) % 2 == 0) CheckerLight else CheckerDark
                val color   = if (idx in hiddenIndices) null else pixels.getOrNull(idx)
                drawRect(
                    color   = color ?: checker,
                    topLeft = Offset(col * cell, row * cell),
                    size    = Size(cell, cell)
                )
            }
        }

        // Vista previa de figura (línea/cuadrado/círculo) mientras se arrastra
        previewIndices.forEach { idx ->
            val row = idx / gridSize
            val col = idx % gridSize
            drawRect(
                color   = previewColor.copy(alpha = 0.55f),
                topLeft = Offset(col * cell, row * cell),
                size    = Size(cell, cell)
            )
        }

        // Bloque "flotante" de la selección mientras se mueve
        moveOverlay?.floatingPixels?.forEach { (idx, color) ->
            val row = idx / gridSize
            val col = idx % gridSize
            drawRect(
                color   = color.copy(alpha = 0.92f),
                topLeft = Offset(col * cell, row * cell),
                size    = Size(cell, cell)
            )
        }

        if (showGridLines) {
            for (i in 0..gridSize) {
                val pos = i * cell
                drawLine(GridLineColor, Offset(pos, 0f), Offset(pos, size.height), 1f)
                drawLine(GridLineColor, Offset(0f, pos), Offset(size.width, pos), 1f)
            }
        }

        // Borde punteado de la selección activa
        selectionRect?.let { rect ->
            drawRect(
                color   = SelectionBorderColor,
                topLeft = Offset(rect.minCol * cell, rect.minRow * cell),
                size    = Size((rect.maxCol - rect.minCol + 1) * cell, (rect.maxRow - rect.minRow + 1) * cell),
                style   = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
            )
        }
    }
}

// ── Helpers de mapeo, relleno y figuras ──────────────────────────────────────────
private fun offsetToIndex(offset: Offset, canvasPx: Float, gridSize: Int): Int? {
    if (canvasPx <= 0f) return null
    val cell = canvasPx / gridSize
    val col  = (offset.x / cell).toInt().coerceIn(0, gridSize - 1)
    val row  = (offset.y / cell).toInt().coerceIn(0, gridSize - 1)
    return row * gridSize + col
}

private fun floodFillIndices(list: List<Color?>, gridSize: Int, startIndex: Int): List<Int> {
    if (startIndex !in list.indices) return emptyList()
    val target = list[startIndex]
    val visited = BooleanArray(list.size)
    val order = mutableListOf<Int>()
    val queue = ArrayDeque<Int>()
    queue.add(startIndex)
    visited[startIndex] = true

    while (queue.isNotEmpty()) {
        val idx = queue.removeFirst()
        order.add(idx)
        val row = idx / gridSize
        val col = idx % gridSize

        if (col > 0)            tryEnqueueMatch(list, queue, visited, idx - 1, target)
        if (col < gridSize - 1) tryEnqueueMatch(list, queue, visited, idx + 1, target)
        if (row > 0)            tryEnqueueMatch(list, queue, visited, idx - gridSize, target)
        if (row < gridSize - 1) tryEnqueueMatch(list, queue, visited, idx + gridSize, target)
    }
    return order
}

private fun tryEnqueueMatch(
    list: List<Color?>, queue: ArrayDeque<Int>, visited: BooleanArray, idx: Int, target: Color?
) {
    if (!visited[idx] && list[idx] == target) {
        visited[idx] = true
        queue.add(idx)
    }
}

// Línea recta entre dos índices del grid (algoritmo de Bresenham)
private fun bresenhamLine(startIdx: Int, endIdx: Int, gridSize: Int): List<Int> {
    var x0 = startIdx % gridSize
    var y0 = startIdx / gridSize
    val x1 = endIdx % gridSize
    val y1 = endIdx / gridSize

    val points = mutableListOf<Int>()
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val sx = if (x1 >= x0) 1 else -1
    val sy = if (y1 >= y0) 1 else -1
    var err = dx - dy

    while (true) {
        points.add(y0 * gridSize + x0)
        if (x0 == x1 && y0 == y1) break
        val e2 = 2 * err
        if (e2 > -dy) { err -= dy; x0 += sx }
        if (e2 < dx)  { err += dx; y0 += sy }
    }
    return points
}

// Rectángulo (borde o relleno) entre dos índices del grid
private fun rectangleIndices(startIdx: Int, endIdx: Int, gridSize: Int, filled: Boolean): List<Int> {
    val r0 = startIdx / gridSize; val c0 = startIdx % gridSize
    val r1 = endIdx / gridSize;   val c1 = endIdx % gridSize
    val minR = minOf(r0, r1); val maxR = maxOf(r0, r1)
    val minC = minOf(c0, c1); val maxC = maxOf(c0, c1)

    if (filled) {
        val result = mutableListOf<Int>()
        for (row in minR..maxR) for (col in minC..maxC) result.add(row * gridSize + col)
        return result
    }

    val ordered = LinkedHashSet<Int>()
    for (c in minC..maxC) ordered.add(minR * gridSize + c)
    for (r in minR..maxR) ordered.add(r * gridSize + maxC)
    for (c in maxC downTo minC) ordered.add(maxR * gridSize + c)
    for (r in maxR downTo minR) ordered.add(r * gridSize + minC)
    return ordered.toList()
}

// Círculo/óvalo (borde o relleno), ajustado al rectángulo entre dos índices
private fun ellipseIndices(startIdx: Int, endIdx: Int, gridSize: Int, filled: Boolean): List<Int> {
    val r0 = startIdx / gridSize; val c0 = startIdx % gridSize
    val r1 = endIdx / gridSize;   val c1 = endIdx % gridSize
    val minR = minOf(r0, r1); val maxR = maxOf(r0, r1)
    val minC = minOf(c0, c1); val maxC = maxOf(c0, c1)

    val cx = (minC + maxC) / 2f
    val cy = (minR + maxR) / 2f
    val rx = ((maxC - minC) / 2f).coerceAtLeast(0.5f)
    val ry = ((maxR - minR) / 2f).coerceAtLeast(0.5f)

    if (filled) {
        val result = mutableListOf<Int>()
        for (row in minR..maxR) {
            for (col in minC..maxC) {
                val nx = (col - cx) / rx
                val ny = (row - cy) / ry
                if (nx * nx + ny * ny <= 1f) result.add(row * gridSize + col)
            }
        }
        return result
    }

    val ordered = LinkedHashSet<Int>()
    val steps = (8 * maxOf(rx, ry)).toInt().coerceAtLeast(24)
    for (i in 0 until steps) {
        val angle = 2.0 * PI * i / steps
        val col = (cx + rx * cos(angle)).roundToInt().coerceIn(minC, maxC)
        val row = (cy + ry * sin(angle)).roundToInt().coerceIn(minR, maxR)
        ordered.add(row * gridSize + col)
    }
    return ordered.toList()
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

// ── Chip Borde / Relleno ──────────────────────────────────────────────────────
@Composable
private fun ShapeModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AppColors.Maldicion else AppColors.Sombra)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text     = label,
            fontSize = 12.sp,
            color    = if (selected) Color.White else AppColors.Eco
        )
    }
}

// ── Botón de icono (deshacer / rehacer / cuadrícula / limpiar / zoom) ───────────
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