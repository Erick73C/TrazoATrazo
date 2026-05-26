package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


// ── DataStore (extensión a nivel de archivo — no puede estar dentro de clase) ─
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_preferences"
)

// ── Keys ──────────────────────────────────────────────────────────────────────
private object PrefKeys {
    val SELECTED_THEME = stringPreferencesKey("selected_theme")
}

// ── Clase de acceso ───────────────────────────────────────────────────────────
class ThemePreferences(private val context: Context) {

    // ── Leer tema guardado (Flow reactivo) ────────────────────────────────────
    fun getThemeFlow(): Flow<AppTheme> =
        context.dataStore.data
            .catch { exception ->
                // Si el DataStore falla (archivo corrupto etc), devuelve defaults
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                val savedName = prefs[PrefKeys.SELECTED_THEME]
                if (savedName != null) {
                    try { AppTheme.valueOf(savedName) }
                    catch (e: IllegalArgumentException) { AppTheme.JJK_DARK }
                } else {
                    AppTheme.JJK_DARK  // Tema por defecto
                }
            }

    // ── Guardar tema seleccionado ─────────────────────────────────────────────
    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.SELECTED_THEME] = theme.name
        }
    }
}