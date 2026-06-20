package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.trazoatrazo.ui.theme.AppFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// ── DataStore separado para preferencias de tipografía ────────────────────────
private val Context.fontDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_font"
)

// ── Keys ──────────────────────────────────────────────────────────────────────
private object FontKeys {
    val SELECTED_FONT = stringPreferencesKey("selected_font")
}

// ── Clase de acceso ───────────────────────────────────────────────────────────
class FontPreferences(private val context: Context) {

    // ── Leer fuente guardada (Flow reactivo) ──────────────────────────────────
    fun getFontFlow(): Flow<AppFont> =
        context.fontDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                val savedName = prefs[FontKeys.SELECTED_FONT]
                if (savedName != null) {
                    try { AppFont.valueOf(savedName) }
                    catch (e: IllegalArgumentException) { AppFont.QUICKSAND }
                } else {
                    AppFont.QUICKSAND   // Fuente por defecto
                }
            }

    // ── Guardar fuente seleccionada ───────────────────────────────────────────
    suspend fun saveFont(font: AppFont) {
        context.fontDataStore.edit { prefs ->
            prefs[FontKeys.SELECTED_FONT] = font.name
        }
    }
}