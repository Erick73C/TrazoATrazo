package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.trazoatrazo.ui.theme.AppFont
import com.example.trazoatrazo.ui.theme.MessageStyle
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
    val MESSAGE_STYLE = stringPreferencesKey("message_style")
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

    // ── Leer estilo de mensaje ────────────────────────────────────────────────
    fun getMessageStyleFlow(): Flow<MessageStyle> =
        context.fontDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                val savedName = prefs[FontKeys.MESSAGE_STYLE]
                if (savedName != null) {
                    try { MessageStyle.valueOf(savedName) }
                    catch (e: IllegalArgumentException) { MessageStyle.NORMAL }
                } else {
                    MessageStyle.NORMAL
                }
            }

    // ── Guardar estilo de mensaje ─────────────────────────────────────────────
    suspend fun saveMessageStyle(style: MessageStyle) {
        context.fontDataStore.edit { prefs ->
            prefs[FontKeys.MESSAGE_STYLE] = style.name
        }
    }
}
