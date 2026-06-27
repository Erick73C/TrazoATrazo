package com.example.trazoatrazo.presentation.pixeleditor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.PixelArtwork
import com.example.trazoatrazo.data.PixelArtworkPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.local.db.PixelArtDatabase
import com.example.trazoatrazo.data.local.repository.PixelArtRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── UiState del editor ────────────────────────────────────────────────────────

/**
 * Estado completo del editor de píxeles.
 *
 * [drawingId]      : null = dibujo nuevo aún no persistido.
 * [pixels]         : estado visual actual del lienzo.
 * [paintOrder]     : historial de trazos para el reproductor.
 * [selectedColor]  : color activo en la paleta.
 * [canvasSize]     : 8, 16 ó 32.
 * [isDirty]        : true si hay cambios sin guardar.
 * [isSaving]       : true mientras se ejecuta el guardado en Room.
 */
data class PixelEditorUiState(
    val drawingId: Long? = null,
    val title: String = "Mi pixel art",
    val canvasSize: Int = 16,
    val themeId: String = "",
    val pixels: List<Color?> = emptyList(),
    val paintOrder: List<PixelArtRepository.StrokeStep> = emptyList(),
    val selectedColor: Color = Color.White,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false
)

/**
 * Estado de la galería (MyCreationsScreen).
 * Solo metadata — sin trazos. Ligero y rápido.
 */
data class GalleryUiState(
    val artworks: List<PixelArtRepository.PixelArtworkDomain> = emptyList(),
    val isLoading: Boolean = true
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class PixelArtViewModel(application: Application) : AndroidViewModel(application) {

    // ── Galería ──────────────────────────────────────────────────────────────

    public val repository = PixelArtRepository(
        PixelArtDatabase.getInstance(application).pixelDrawingDao()
    )

    private val _galleryState = MutableStateFlow(GalleryUiState())
    val galleryState: StateFlow<GalleryUiState> = _galleryState.asStateFlow()

    // ── Editor ───────────────────────────────────────────────────────────────

    private val _editorState = MutableStateFlow(PixelEditorUiState())
    val editorState: StateFlow<PixelEditorUiState> = _editorState.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────

    init {
        // Colectar la galería desde Room en cuanto se crea el ViewModel
        viewModelScope.launch {
            repository.getAllDrawingsFlow().collect { list ->
                _galleryState.update {
                    it.copy(artworks = list, isLoading = false)
                }
            }
        }
    }

    // ── API del editor ───────────────────────────────────────────────────────

    /**
     * Prepara el editor para un dibujo NUEVO.
     * Llama antes de navegar a PixelEditorScreen sin drawingId.
     *
     * [themeId] : tema activo en el momento de crear (ej. "CYBERPUNK").
     */
    fun initNewDrawing(canvasSize: Int, themeId: String) {
        _editorState.value = PixelEditorUiState(
            canvasSize = canvasSize,
            themeId    = themeId,
            pixels     = List(canvasSize * canvasSize) { null },
            paintOrder = emptyList()
        )
    }

    /**
     * Carga un dibujo existente en el editor.
     * Llama cuando el usuario abre uno de sus creaciones desde MyCreationsScreen.
     */
    fun loadDrawing(drawingId: Long) {
        viewModelScope.launch {
            val artwork = repository.getDrawingForPlayback(drawingId) ?: return@launch
            _editorState.value = PixelEditorUiState(
                drawingId  = artwork.id,
                title      = artwork.title,
                canvasSize = artwork.canvasSize,
                themeId    = artwork.themeId,
                pixels     = artwork.pixels.toMutableList(),
                paintOrder = artwork.paintOrder,
                isDirty    = false
            )
        }
    }

    /**
     * El usuario pinta (o borra) un píxel.
     *
     * Actualiza el estado del lienzo en memoria de forma inmediata
     * para que la UI sea reactiva sin esperar a Room.
     * El trazo se registra en [paintOrder] para el reproductor.
     *
     * [pixelIndex] : posición en el lienzo (0 .. canvasSize² - 1).
     * [color]      : null = borrador.
     */
    fun paintPixel(pixelIndex: Int, color: Color?) {
        _editorState.update { state ->
            val newPixels = state.pixels.toMutableList().also { it[pixelIndex] = color }
            val newStep   = PixelArtRepository.StrokeStep(pixelIndex = pixelIndex, color = color)
            state.copy(
                pixels     = newPixels,
                paintOrder = state.paintOrder + newStep,
                isDirty    = true
            )
        }
    }

    /** Cambia el color activo en la paleta. */
    fun selectColor(color: Color) {
        _editorState.update { it.copy(selectedColor = color) }
    }

    /** Cambia el título del dibujo (desde el campo de texto en el editor). */
    fun updateTitle(title: String) {
        _editorState.update { it.copy(title = title, isDirty = true) }
    }

    /**
     * Limpia el lienzo completamente.
     * Registra un borrador sobre cada píxel pintado para que el reproductor
     * también muestre el "reset" si el usuario repite la animación.
     */
    fun clearCanvas() {
        _editorState.update { state ->
            val eraserSteps = state.pixels
                .mapIndexedNotNull { index, color ->
                    if (color != null) PixelArtRepository.StrokeStep(
                        pixelIndex = index,
                        color = null
                    ) else null
                }
            state.copy(
                pixels     = List(state.canvasSize * state.canvasSize) { null },
                paintOrder = state.paintOrder + eraserSteps,
                isDirty    = true
            )
        }
    }

    /**
     * Guarda el dibujo actual en Room.
     *
     * Si es nuevo ([drawingId] == null) Room genera el ID automáticamente
     * y lo persiste en el estado para guardados posteriores del mismo dibujo.
     *
     * Activa [isSaving] durante la operación para bloquear el botón
     * y evitar guardados duplicados.
     */
    /**
     * Guarda el dibujo tomando el estado final del lienzo directamente desde el editor.
     * El editor mantiene [pixelsSnapshot] y [paintOrderSnapshot] como fuente de verdad
     * local durante la sesión; aquí los persistimos en Room.
     */
    fun saveCurrentDrawing(
        canvasSize: Int,
        pixelsSnapshot: List<Color?>,
        paintOrderSnapshot: List<PixelArtRepository.StrokeStep>
    ) {
        val state = _editorState.value
        _editorState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val artwork = PixelArtRepository.PixelArtworkDomain(
                id = state.drawingId ?: 0L,
                title = state.title,
                canvasSize = canvasSize,
                themeId = state.themeId,
                pixels = pixelsSnapshot,
                paintOrder = paintOrderSnapshot
            )
            val savedId = repository.saveDrawing(artwork)

            // Persistir el ID generado por Room para guardados futuros
            _editorState.update {
                it.copy(
                    drawingId  = savedId,
                    canvasSize = canvasSize,
                    pixels     = pixelsSnapshot,
                    paintOrder = paintOrderSnapshot,
                    isDirty    = false,
                    isSaving   = false
                )
            }
        }
    }

    // ── API del reproductor ──────────────────────────────────────────────────

    /**
     * Busca un dibujo completo (metadata + trazos) por su ID.
     */
    suspend fun findById(drawingId: Long): PixelArtRepository.PixelArtworkDomain? =
        repository.getDrawingForPlayback(drawingId)

    /**
     * Devuelve la secuencia de trazos para el reproductor "▶️ Ver mi trazo".
     *
     * El reproductor puede consumir esta lista directamente en su
     * LaunchedEffect(repetir) para animar píxel a píxel con Animatable,
     * exactamente igual que Girasol, Corazón, Tortuga, etc.
     *
     * Retorna null si el dibujo no existe en Room.
     */
    suspend fun getPlaybackSteps(drawingId: Long): List<PixelArtRepository.StrokeStep>? =
        repository.getDrawingForPlayback(drawingId)?.paintOrder

    // ── API de la galería ────────────────────────────────────────────────────

    /**
     * Borra un dibujo de Room.
     * El CASCADE en ForeignKey elimina automáticamente todos sus trazos.
     */
    fun deleteDrawing(drawingId: Long) {
        viewModelScope.launch {
            repository.deleteDrawing(drawingId)
        }
    }
}