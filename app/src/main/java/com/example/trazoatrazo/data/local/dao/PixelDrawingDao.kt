package com.example.trazoatrazo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.trazoatrazo.data.local.DrawingWithStrokes
import com.example.trazoatrazo.data.local.entity.PixelDrawingEntity
import com.example.trazoatrazo.data.local.entity.StrokeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PixelDrawingDao {

    // ── Dibujo (metadata) ────────────────────────────────────────────────────

    /**
     * Inserta o actualiza la metadata del dibujo.
     * @Upsert (Room 2.5+): hace INSERT si no existe, UPDATE si ya existe.
     * Devuelve el drawingId generado — necesario para asociar los trazos.
     */
    @Upsert
    suspend fun upsertDrawing(drawing: PixelDrawingEntity): Long

    /**
     * Galería: lista ligera de todos los dibujos, sin cargar sus trazos.
     * Ordenada por updatedAt DESC (más reciente primero) de forma nativa en SQL.
     */
    @Query("SELECT * FROM pixel_drawings ORDER BY updatedAt DESC")
    fun getAllDrawings(): Flow<List<PixelDrawingEntity>>

    @Delete
    suspend fun deleteDrawing(drawing: PixelDrawingEntity)

    // ── Trazos ───────────────────────────────────────────────────────────────

    /**
     * Inserta un trazo nuevo. IGNORE descarta duplicados de strokeId
     * (en la práctica no ocurre porque strokeId es autoGenerate).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStroke(stroke: StrokeEntity)

    /**
     * Borra todos los trazos de un dibujo.
     * Útil para "limpiar lienzo" o reimportar desde DataStore al migrar.
     */
    @Query("DELETE FROM drawing_strokes WHERE drawingParentId = :drawingId")
    suspend fun clearStrokes(drawingId: Long)

    // ── Relación completa (editor + reproductor) ─────────────────────────────

    /**
     * Carga el dibujo con TODOS sus trazos ordenados cronológicamente.
     *
     * @Transaction garantiza que metadata y trazos se leen en la misma
     * transacción — sin riesgo de inconsistencia si hay escrituras concurrentes.
     *
     * Los trazos vienen ordenados por timestamp ASC directamente desde SQL
     * (aprovecha el índice en timestamp → O(log n)), sin ningún sort en Kotlin.
     *
     * Uso: reproductor "▶️ Ver mi trazo" y carga del editor para continuar.
     */
    @Transaction
    @Query("""
        SELECT * FROM pixel_drawings WHERE drawingId = :drawingId
    """)
    suspend fun getDrawingWithStrokes(drawingId: Long): DrawingWithStrokes?

    /**
     * Versión Flow para el editor: re-emite automáticamente si hay cambios.
     * Útil si quieres mostrar un preview reactivo mientras se edita.
     */
    @Transaction
    @Query("SELECT * FROM pixel_drawings WHERE drawingId = :drawingId")
    fun observeDrawingWithStrokes(drawingId: Long): Flow<DrawingWithStrokes?>

    // En PixelDrawingDao.kt — reemplaza @Upsert por estos dos:

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDrawing(drawing: PixelDrawingEntity): Long   // devuelve el ID generado

    @Update
    suspend fun updateDrawing(drawing: PixelDrawingEntity)         // actualiza si ya existe

}