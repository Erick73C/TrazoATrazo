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
 */
class PixelArtRepository(private val dao: PixelDrawingDao) {

    // ── Modelo de dominio ────────────────────────────────────────────────────

    data class PixelArtworkDomain(
        val id: Long = 0,
        val title: String,
        val canvasSize: Int,
        val themeId: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val pixels: List<Color?>,
        val paintOrder: List<StrokeStep>
    )

    data class StrokeStep(
        val pixelIndex: Int,
        val color: Color?
    )

    // ── Galería ──────────────────────────────────────────────────────────────

    fun getAllDrawingsFlow(): Flow<List<PixelArtworkDomain>> =
        dao.getAllDrawings().map { list ->
            list.map { entity -> entity.toEmptyDomain() }
        }

    // ── Carga ────────────────────────────────────────────────────────────────

    suspend fun getDrawingForPlayback(drawingId: Long): PixelArtworkDomain? {
        val result = dao.getDrawingWithStrokes(drawingId) ?: return null
        val sortedStrokes = result.strokes.sortedBy { it.timestamp }
        return result.drawing.toDomain(sortedStrokes)
    }

    fun observeDrawing(drawingId: Long): Flow<PixelArtworkDomain?> =
        dao.observeDrawingWithStrokes(drawingId).map { result ->
            result?.let {
                val sortedStrokes = it.strokes.sortedBy { s -> s.timestamp }
                it.drawing.toDomain(sortedStrokes)
            }
        }

    // ── Guardado ─────────────────────────────────────────────────────────────

    suspend fun saveDrawing(artwork: PixelArtworkDomain): Long {
        val entity = artwork.toEntity()
        val strokes = artwork.paintOrder.mapIndexed { index, step ->
            StrokeEntity(
                drawingParentId = entity.drawingId,
                pixelIndex      = step.pixelIndex,
                colorArgb       = step.color?.toArgb() ?: 0,
                timestamp       = index.toLong()
            )
        }
        return dao.saveFullDrawing(entity, strokes)
    }

    suspend fun addStroke(drawingId: Long, pixelIndex: Int, color: Color?) {
        dao.insertStroke(
            StrokeEntity(
                drawingParentId = drawingId,
                pixelIndex      = pixelIndex,
                colorArgb       = color?.toArgb() ?: 0
            )
        )
    }

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

    suspend fun deleteDrawing(drawingId: Long) {
        dao.deleteDrawing(PixelDrawingEntity(
            drawingId  = drawingId,
            title      = "",
            canvasSize = 0,
            themeId    = ""
        ))
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun PixelDrawingEntity.toDomain(
        sortedStrokes: List<StrokeEntity>
    ): PixelArtworkDomain {
        val rawCount = if (pixelsRaw.isBlank()) 0 else pixelsRaw.split(",").size
        
        // Detección estricta de tamaño basada en el contenido real
        val finalCanvasSize = when (rawCount) {
            64   -> 8
            256  -> 16
            1024 -> 32
            else -> {
                val maxIndex = sortedStrokes.maxOfOrNull { it.pixelIndex } ?: 0
                when {
                    maxIndex < 64   -> 8
                    maxIndex < 256  -> 16
                    else            -> 32
                }
            }
        }

        val pixels = decodePixels(pixelsRaw, finalCanvasSize).toMutableList()
        sortedStrokes.forEach { stroke ->
            if (stroke.pixelIndex in pixels.indices) {
                pixels[stroke.pixelIndex] = if (stroke.colorArgb == 0) null else Color(stroke.colorArgb)
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

    private fun PixelDrawingEntity.toEmptyDomain(): PixelArtworkDomain {
        val rawCount = if (pixelsRaw.isBlank()) 0 else pixelsRaw.split(",").size
        val finalSize = when {
            rawCount > 256 -> 32
            rawCount > 64  -> 16
            rawCount > 0   -> 8
            else           -> canvasSize
        }

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

    private fun PixelArtworkDomain.toEntity() = PixelDrawingEntity(
        drawingId  = id,
        title      = title,
        canvasSize = canvasSize,
        themeId    = themeId,
        createdAt  = createdAt,
        updatedAt  = System.currentTimeMillis(),
        pixelsRaw  = encodePixels(pixels)
    )

    private fun encodePixels(pixels: List<Color?>): String =
        pixels.joinToString(",") { it?.toArgb()?.toString() ?: "0" }

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
