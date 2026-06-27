package com.example.trazoatrazo.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trazoatrazo.data.local.dao.PixelDrawingDao
import com.example.trazoatrazo.data.local.entity.PixelDrawingEntity
import com.example.trazoatrazo.data.local.entity.StrokeEntity

/**
 * Base de datos Room para el editor de píxeles.
 *
 * Versión 1 — tablas iniciales:
 *   · pixel_drawings  → metadata de cada dibujo
 *   · drawing_strokes → historial de trazos (orden de pintado)
 *
 * Singleton: una sola instancia para toda la app.
 * Se crea en [AppNavigation] (o en Application) y se pasa al Repository.
 */
@Database(
    entities = [
        PixelDrawingEntity::class,
        StrokeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PixelArtDatabase : RoomDatabase() {

    abstract fun pixelDrawingDao(): PixelDrawingDao

    companion object {
        @Volatile
        private var INSTANCE: PixelArtDatabase? = null

        fun getInstance(context: Context): PixelArtDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PixelArtDatabase::class.java,
                    "pixel_art.db"
                )
                    .fallbackToDestructiveMigration() // en desarrollo: OK borrar y recrear
                    .build()
                    .also { INSTANCE = it }
            }
    }
}