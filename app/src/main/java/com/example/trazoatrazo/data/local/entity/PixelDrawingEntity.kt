package com.example.trazoatrazo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata de un pixel art guardado.
 * No contiene los píxeles — esos viven en [StrokeEntity].
 *
 * [canvasSize] : 8, 16 ó 32
 * [themeId]    : ID del tema activo cuando se creó (ej. "CYBERPUNK", "VALENTINE").
 *                Se usa para renderizar la paleta correcta en la galería/reproductor.
 */
@Entity(tableName = "pixel_drawings")
data class PixelDrawingEntity(
    @PrimaryKey(autoGenerate = true)
    val drawingId: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val canvasSize: Int,
    val themeId: String,
    val pixelsRaw: String = "" // Estado aplanado del lienzo para la galería
)