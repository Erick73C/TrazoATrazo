package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore específico para configuraciones del sistema
private val Context.systemDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_system"
)

private object SystemPrefKeys {
    val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
}

class SystemPreferences(private val context: Context) {

    fun getImmersiveModeFlow(): Flow<Boolean> =
        context.systemDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                prefs[SystemPrefKeys.IMMERSIVE_MODE] ?: false
            }

    suspend fun saveImmersiveMode(enabled: Boolean) {
        context.systemDataStore.edit { prefs ->
            prefs[SystemPrefKeys.IMMERSIVE_MODE] = enabled
        }
    }
}
