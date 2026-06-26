package com.example.trazoatrazo.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// ── Modelo de una creación guardada ────────────────────────────────────────────
@Immutable
data class PixelArtwork(
    val id: String,
    val name: String,
    val gridSize: Int,
    val pixels: List<Color?>,       // null = transparente
    val paintOrder: List<Int> = emptyList(),   // orden en que se pintó cada índice
    val createdAt: Long
)

// ── DataStore ─────────────────────────────────────────────────────────────────
private val Context.pixelArtDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_pixel_art"
)

private object PixelArtKeys {
    val ARTWORKS = stringPreferencesKey("pixel_artworks")
}

private const val FIELD_SEP  = '\u001F'
private const val RECORD_SEP = '\u001E'

class PixelArtworkPreferences(private val context: Context) {

    fun getArtworksFlow(): Flow<List<PixelArtwork>> =
        context.pixelArtDataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { prefs -> decodeAll(prefs[PixelArtKeys.ARTWORKS] ?: "") }

    suspend fun saveArtwork(artwork: PixelArtwork) {
        context.pixelArtDataStore.edit { prefs ->
            val current = decodeAll(prefs[PixelArtKeys.ARTWORKS] ?: "")
            val updated = current.filterNot { it.id == artwork.id } + artwork
            prefs[PixelArtKeys.ARTWORKS] = encodeAll(updated)
        }
    }

    suspend fun deleteArtwork(id: String) {
        context.pixelArtDataStore.edit { prefs ->
            val current = decodeAll(prefs[PixelArtKeys.ARTWORKS] ?: "")
            prefs[PixelArtKeys.ARTWORKS] = encodeAll(current.filterNot { it.id == id })
        }
    }

    private fun encodeAll(list: List<PixelArtwork>): String =
        list.joinToString(RECORD_SEP.toString()) { encodeOne(it) }

    private fun decodeAll(raw: String): List<PixelArtwork> =
        if (raw.isBlank()) emptyList()
        else raw.split(RECORD_SEP).mapNotNull { decodeOne(it) }

    private fun encodeOne(artwork: PixelArtwork): String {
        val pixelsHex = artwork.pixels.joinToString("") { color ->
            String.format("%08X", (color ?: Color.Transparent).toArgb())
        }
        val paintOrderCsv = artwork.paintOrder.joinToString(",")
        return listOf(
            artwork.id,
            artwork.name,
            artwork.gridSize.toString(),
            artwork.createdAt.toString(),
            pixelsHex,
            paintOrderCsv
        ).joinToString(FIELD_SEP.toString())
    }

    private fun decodeOne(raw: String): PixelArtwork? {
        val parts = raw.split(FIELD_SEP)
        // Soporte para versiones anteriores (5 campos) y la actual (6 campos)
        if (parts.size < 5) return null
        
        return try {
            PixelArtwork(
                id         = parts[0],
                name       = parts[1],
                gridSize   = parts[2].toInt(),
                createdAt  = parts[3].toLongOrNull() ?: System.currentTimeMillis(),
                pixels     = parts[4].chunked(8).map { hex ->
                    try {
                        val argb = hex.toLong(16).toInt()
                        if (argb == 0) null else Color(argb)
                    } catch (e: Exception) { null }
                },
                // El campo 5 (paintOrder) es opcional para compatibilidad con versiones previas
                paintOrder = if (parts.size >= 6 && parts[5].isNotBlank()) {
                    parts[5].split(",").mapNotNull { it.toIntOrNull() }
                } else {
                    emptyList()
                }
            )
        } catch (e: Exception) {
            null
        }
    }
}