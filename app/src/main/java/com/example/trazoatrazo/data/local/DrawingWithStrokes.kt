package com.example.trazoatrazo.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.trazoatrazo.data.local.entity.PixelDrawingEntity
import com.example.trazoatrazo.data.local.entity.StrokeEntity

/**
 * Paquete completo: metadata del dibujo + lista de trazos ordenada cronológicamente.
 *
 * Room resuelve la relación automáticamente con @Transaction en el DAO.
 * Los trazos ya llegan ordenados por timestamp ASC desde la query — no hace
 * falta ningún .sortedBy {} extra en Kotlin.
 *
 * Uso principal: reproductor "▶️ Ver mi trazo" y carga del editor para continuar.
 */
data class DrawingWithStrokes(
    @Embedded
    val drawing: PixelDrawingEntity,

    @Relation(
        parentColumn = "drawingId",
        entityColumn = "drawingParentId",
        entity = StrokeEntity::class
    )
    val strokes: List<StrokeEntity>   // ordenados por timestamp ASC desde el DAO
)
