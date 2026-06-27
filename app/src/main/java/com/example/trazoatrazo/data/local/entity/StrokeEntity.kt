package com.example.trazoatrazo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un trazo individual dentro de un pixel art.
 *
 * Cada vez que el usuario pinta (o borra) un píxel se inserta una fila aquí.
 * Si pinta encima de un píxel ya pintado, se agrega una fila nueva — el historial
 * nunca se sobreescribe, lo que permite la reproducción trazo a trazo exacta.
 *
 * [pixelIndex]    : posición en el lienzo (0 .. canvasSize² - 1)
 * [colorArgb]     : color en formato ARGB (Int). 0 = borrador / transparente.
 *                   Se guarda el valor exacto del color — no un índice de paleta —
 *                   para que la reproducción sea fiel aunque el tema cambie.
 * [timestamp]     : System.nanoTime() en el momento del trazo.
 *                   Garantiza orden milimétrico para la animación.
 */
@Entity(
    tableName = "drawing_strokes",
    foreignKeys = [
        ForeignKey(
            entity = PixelDrawingEntity::class,
            parentColumns = ["drawingId"],
            childColumns = ["drawingParentId"],
            onDelete = ForeignKey.CASCADE   // borrar el dibujo limpia sus trazos
        )
    ],
    indices = [
        Index(value = ["drawingParentId"]),  // JOINs rápidos
        Index(value = ["timestamp"])         // ORDER BY timestamp O(log n)
    ]
)
data class StrokeEntity(
    @PrimaryKey(autoGenerate = true)
    val strokeId: Long = 0,
    val drawingParentId: Long,
    val pixelIndex: Int,
    val colorArgb: Int,                      // 0 = transparente / borrador
    val timestamp: Long = System.nanoTime()
)