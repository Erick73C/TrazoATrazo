package com.example.trazoatrazo.data.local.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.trazoatrazo.data.local.dao.PixelDrawingDao
import com.example.trazoatrazo.data.local.entity.PixelDrawingEntity
import com.example.trazoatrazo.data.local.entity.StrokeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

/**
 * Capa entre el DAO y el ViewModel.
 *
 * Responsabilidades:
 *   1. Traducir entre entidades Room y el modelo de dominio [PixelArtworkDomain].
 *   2. Garantizar el orden cronológico de los trazos para el reproductor.
 *   3. Guardar un dibujo de forma atómica (metadata + trazos en la misma transacción).
 *   4. Exponer Flows listos para collectAsStateWithLifecycle() en el ViewModel.
 */
class PixelArtRepository(private val dao: PixelDrawingDao) {

    // ── Modelo de dominio ────────────────────────────────────────────────────

    /**
     * Modelo limpio que usa el ViewModel y los composables.
     * No tiene dependencias de Room — desacoplado por completo.
     *
     * [pixels]     : estado final del lienzo. null = transparente.
     * [paintOrder] : lista de índices en el orden exacto en que se pintaron.
     *                Cada entrada es un [StrokeStep] con índice + color final.
     */
    data class PixelArtworkDomain(
        val id: Long = 0,
        val title: String,
        val canvasSize: Int,
        val themeId: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val pixels: List<Color?>,           // estado final del lienzo
        val paintOrder: List<StrokeStep>    // secuencia para el reproductor
    )

    data class StrokeStep(
        val pixelIndex: Int,
        val color: Color?   // null = borrador aplicado en ese paso
    )

    // ── Galería (Flow ligero, sin trazos) ────────────────────────────────────

    /**
     * Lista de miniaturas para MyCreationsScreen.
     * Solo carga metadata — no trae los trazos. Rápido y eficiente.
     */
    fun getAllDrawingsFlow(): Flow<List<PixelArtworkDomain>> =
        dao.getAllDrawings().map { list ->
            list.map { entity -> entity.toEmptyDomain() }
        }

    // ── Carga completa (editor + reproductor) ────────────────────────────────

    /**
     * Devuelve el dibujo con todos sus trazos, ordenados cronológicamente.
     *
     * El sort se aplica aquí (no en el DAO) porque @Relation de Room
     * no garantiza el orden de la lista secundaria. El índice en `timestamp`
     * hace que la operación sea O(log n) igualmente.
     *
     * Devuelve null si el drawingId no existe.
     */
    suspend fun getDrawingForPlayback(drawingId: Long): PixelArtworkDomain? {
        val result = dao.getDrawingWithStrokes(drawingId) ?: return null
        val sortedStrokes = result.strokes.sortedBy { it.timestamp }
        return result.drawing.toDomain(sortedStrokes)
    }

    /**
     * Flow reactivo para el editor: re-emite si el dibujo cambia.
     * Los trazos también se ordenan aquí por la misma razón.
     */
    fun observeDrawing(drawingId: Long): Flow<PixelArtworkDomain?> =
        dao.observeDrawingWithStrokes(drawingId).map { result ->
            result?.let {
                val sortedStrokes = it.strokes.sortedBy { s -> s.timestamp }
                it.drawing.toDomain(sortedStrokes)
            }
        }

    // ── Guardado atómico ─────────────────────────────────────────────────────

    /**
     * Guarda (o actualiza) un dibujo completo de forma atómica:
     *   1. Upsert de la metadata → obtiene el drawingId.
     *   2. Borra los trazos anteriores (si es una actualización).
     *   3. Inserta todos los trazos nuevos en orden.
     *
     * Llamar cuando el usuario pulsa "Guardar" en el editor.
     * Devuelve el drawingId final (útil si es una creación nueva).
     */
    suspend fun saveDrawing(artwork: PixelArtworkDomain): Long {
        val entity = artwork.toEntity()

        // Si es un dibujo nuevo (id == 0), INSERT explícito para obtener el ID generado.
        // Si ya existe, UPDATE la metadata y reusar el mismo ID.
        val drawingId: Long = if (entity.drawingId == 0L) {
            dao.insertDrawing(entity)          // ← INSERT puro, devuelve el ROWID real
        } else {
            dao.updateDrawing(entity)          // ← UPDATE, devuelve Unit
            entity.drawingId
        }

        // Con el drawingId confirmado en la DB, ahora sí insertamos los strokes
        dao.clearStrokes(drawingId)
        artwork.paintOrder.forEachIndexed { index, step ->
            dao.insertStroke(
                StrokeEntity(
                    drawingParentId = drawingId,
                    pixelIndex      = step.pixelIndex,
                    colorArgb       = step.color?.toArgb() ?: 0,
                    timestamp       = index.toLong()
                )
            )
        }

        return drawingId
    }

    /**
     * Inserta un único trazo en tiempo real (mientras el usuario pinta).
     * Más eficiente que guardar todo el dibujo en cada pincelada.
     *
     * Requiere que el dibujo ya exista (llama primero a [saveDrawing] o
     * [upsertDrawingMetadata] para obtener el drawingId).
     */
    suspend fun addStroke(drawingId: Long, pixelIndex: Int, color: Color?) {
        dao.insertStroke(
            StrokeEntity(
                drawingParentId = drawingId,
                pixelIndex      = pixelIndex,
                colorArgb       = color?.toArgb() ?: 0
                // timestamp usa System.nanoTime() por defecto
            )
        )
    }

    /**
     * Crea o actualiza solo la metadata (sin tocar los trazos).
     * Útil al crear un dibujo nuevo para obtener el drawingId antes de pintar.
     */
    suspend fun upsertDrawingMetadata(
        id: Long = 0,
        title: String,
        canvasSize: Int,
        themeId: String
    ): Long = dao.upsertDrawing(
        PixelDrawingEntity(
            drawingId  = id,
            title      = title,
            canvasSize = canvasSize,
            themeId    = themeId,
            updatedAt  = System.currentTimeMillis()
        )
    )

    // ── Borrado ──────────────────────────────────────────────────────────────

    /**
     * Borra el dibujo y todos sus trazos (CASCADE en FK lo hace automáticamente).
     */
    suspend fun deleteDrawing(drawingId: Long) {
        dao.deleteDrawing(PixelDrawingEntity(
            drawingId  = drawingId,
            title      = "",
            canvasSize = 0,
            themeId    = ""
        ))
    }

    // ── Mappers privados ─────────────────────────────────────────────────────

    /** Entidad Room → dominio con trazos ya ordenados */
    private fun PixelDrawingEntity.toDomain(
        sortedStrokes: List<StrokeEntity>
    ): PixelArtworkDomain {
        // Encontrar el índice máximo para determinar el tamaño real del lienzo
        // Si hay trazos fuera de los límites (ej. índice 741 en un lienzo de 16x16 que llega hasta 255)
        // significa que el canvasSize guardado es erróneo.
        val maxIndex = sortedStrokes.maxOfOrNull { it.pixelIndex } ?: 0
        
        // También verificamos la cantidad de píxeles en pixelsRaw por si los trazos fallaron
        val rawCount = if (pixelsRaw.isBlank()) 0 else pixelsRaw.split(",").size
        val maxDetectedIndex = maxOf(maxIndex, rawCount - 1)

        val finalCanvasSize = when {
            rawCount == 64   || maxIndex < 64  -> 8
            rawCount == 256  || maxIndex < 256 -> 16
            rawCount == 1024 || maxIndex < 1024 -> 32
            else -> canvasSize // Confiar en el guardado si no hay coincidencia exacta
        }

        // IMPORTANTE: Cargamos el estado desde pixelsRaw como base. 
        // Esto evita que el lienzo salga vacío si no hay trazos guardados.
        val pixels = decodePixels(pixelsRaw, finalCanvasSize).toMutableList()
        
        // Si hay trazos, los aplicamos (sobrescribiendo si es necesario) para asegurar consistencia
        sortedStrokes.forEach { stroke ->
            if (stroke.pixelIndex in pixels.indices) {
                pixels[stroke.pixelIndex] = if (stroke.colorArgb == 0) null
                else Color(stroke.colorArgb)
            }
        }

        val paintOrder = sortedStrokes.map { stroke ->
            StrokeStep(
                pixelIndex = stroke.pixelIndex,
                color      = if (stroke.colorArgb == 0) null else Color(stroke.colorArgb)
            )
        }

        return PixelArtworkDomain(
            id         = drawingId,
            title      = title,
            canvasSize = finalCanvasSize,
            themeId    = themeId,
            createdAt  = createdAt,
            updatedAt  = updatedAt,
            pixels     = pixels,
            paintOrder = paintOrder
        )
    }

    /** Entidad Room → dominio sin trazos (para la galería) */
    private fun PixelDrawingEntity.toEmptyDomain(): PixelArtworkDomain {
        // Detectar tamaño real basado en la cantidad de píxeles guardados en el string
        val rawCount = if (pixelsRaw.isBlank()) 0 else pixelsRaw.split(",").size
        val detectedSize = when {
            rawCount <= 8 * 8   -> 8
            rawCount <= 16 * 16 -> 16
            else                -> 32
        }
        val finalSize = detectedSize

        return PixelArtworkDomain(
            id         = drawingId,
            title      = title,
            canvasSize = finalSize,
            themeId    = themeId,
            createdAt  = createdAt,
            updatedAt  = updatedAt,
            pixels     = decodePixels(pixelsRaw, finalSize),
            paintOrder = emptyList()
        )
    }

    /** Dominio → entidad Room */
    private fun PixelArtworkDomain.toEntity() = PixelDrawingEntity(
        drawingId  = id,
        title      = title,
        canvasSize = canvasSize,
        themeId    = themeId,
        createdAt  = createdAt,
        updatedAt  = System.currentTimeMillis(),
        pixelsRaw  = encodePixels(pixels)
    )

    private fun encodePixels(pixels: List<Color?>): String {
        return pixels.joinToString(",") { it?.toArgb()?.toString() ?: "0" }
    }

    private fun decodePixels(raw: String, canvasSize: Int): List<Color?> {
        val total = canvasSize * canvasSize
        if (raw.isBlank()) return List(total) { null }
        val decoded = raw.split(",").map {
            val argb = it.toLongOrNull()?.toInt() ?: 0
            if (argb == 0) null else Color(argb)
        }
        return if (decoded.size >= total) decoded.take(total)
        else decoded + List(total - decoded.size) { null }
    }
}