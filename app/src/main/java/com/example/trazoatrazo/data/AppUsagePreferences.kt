package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.trazoatrazo.utils.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// ── DataStore (extensión a nivel de archivo — no puede estar dentro de clase) ─
private val Context.usageDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_usage"
)

// ── Keys ──────────────────────────────────────────────────────────────────────
private object UsageKeys {
    val OPENED_DATES        = stringSetPreferencesKey("opened_dates")
    val NOTIFIED_UNLOCK_IDS = stringSetPreferencesKey("notified_unlock_ids")
    val FIRST_OPEN_TIMESTAMP = longPreferencesKey("first_open_timestamp")
    val LAST_OPEN_TIMESTAMP = longPreferencesKey("last_open_timestamp")
    val LAST_SHOWN_EVENT_ID = stringPreferencesKey("last_shown_event_id")
    val NOTIFIED_EVENT_ID   = stringPreferencesKey("notified_event_id")
}

/**
 * Acceso a DataStore para todo lo relacionado con la actividad del usuario
 * en el tiempo: racha de días distintos que abrió la app, y qué contenidos
 * desbloqueables ya le mostraron su banner de aviso.
 *
 * Es la base compartida de Cápsulas del Tiempo, Eventos Especiales y
 * Dibujos Desbloqueables. No guarda nada sobre el contenido en
 * sí (eso vive en los catálogos estáticos como [com.example.trazoatrazo.data.CapsulesData]),
 * solo el progreso del usuario.
 */
class AppUsagePreferences(private val context: Context) {

    // ── Racha de días abiertos ─────────────────────────────────────────────

    /**
     * Flow reactivo con el conjunto de fechas (formato `yyyy-MM-dd`, ver
     * [DateProvider]) en las que el usuario abrió la app al menos una vez.
     */
    fun getOpenedDatesFlow(): Flow<Set<String>> =
        context.usageDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[UsageKeys.OPENED_DATES] ?: emptySet() }

    /**
     * Flow reactivo con la cantidad de días *distintos* que el usuario abrió
     * la app (racha real, no días de calendario transcurridos). Es lo que
     * consumen las tarjetas bloqueadas de [com.example.trazoatrazo.domain.model.UnlockRequirement].
     */
    fun getDaysOpenedCountFlow(): Flow<Int> =
        getOpenedDatesFlow().map { dates -> dates.size }

    /**
     * Registra la apertura de hoy solo si han pasado al menos 24 horas desde
     * el último registro exitoso. Esto evita que el usuario aumente su racha
     * simplemente cambiando la fecha del sistema en intervalos cortos.
     *
     * Debe llamarse una sola vez por sesión (típicamente desde
     * `MainActivity` o `AppNavigation` al arrancar).
     */
    suspend fun registerAppOpenToday() {
        val currentTime = System.currentTimeMillis()
        val twentyFourHours = 24 * 60 * 60 * 1000L

        context.usageDataStore.edit { prefs ->
            val lastTime = prefs[UsageKeys.LAST_OPEN_TIMESTAMP] ?: 0L

            // Anti-bloqueo: si el reloj del sistema retrocedió manualmente,
            // reseteamos el timestamp al actual para permitir que la racha
            // siga avanzando normalmente a partir de ahora.
            if (currentTime < lastTime) {
                prefs[UsageKeys.LAST_OPEN_TIMESTAMP] = currentTime
                return@edit
            }

            // Solo registramos si pasaron 24h o es la primera vez (lastTime == 0)
            if (currentTime - lastTime >= twentyFourHours || lastTime == 0L) {
                val todayStr = DateProvider.todayAsString()
                val currentDates = prefs[UsageKeys.OPENED_DATES] ?: emptySet()

                // Si por alguna razón la fecha ya está (ej: se borró el timestamp
                // pero no las fechas), igual actualizamos el timestamp para
                // mantener la ventana de 24h.
                if (todayStr !in currentDates) {
                    prefs[UsageKeys.OPENED_DATES] = currentDates + todayStr
                }
                prefs[UsageKeys.LAST_OPEN_TIMESTAMP] = currentTime
            }

            // Registrar timestamp de la primera apertura absoluta
            if (prefs[UsageKeys.FIRST_OPEN_TIMESTAMP] == null) {
                prefs[UsageKeys.FIRST_OPEN_TIMESTAMP] = currentTime
            }
        }
    }

    /** Timestamp (epoch millis) de la primera vez que se abrió la app. Null si nunca se registró. */
    fun getFirstOpenTimestampFlow(): Flow<Long?> =
        context.usageDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[UsageKeys.FIRST_OPEN_TIMESTAMP] }

    // ── Notificaciones de "Nuevo recuerdo desbloqueado" ────────────────────

    /**
     * Flow reactivo con los ids de contenido (cápsulas o dibujos con
     * [com.example.trazoatrazo.domain.model.UnlockRequirement]) para los que
     * ya se mostró el banner de desbloqueo. Evita repetir el aviso.
     */
    fun getNotifiedIdsFlow(): Flow<Set<String>> =
        context.usageDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[UsageKeys.NOTIFIED_UNLOCK_IDS] ?: emptySet() }

    /** Verificación puntual (no reactiva) de si [id] ya fue notificado. */
    suspend fun isAlreadyNotified(id: String): Boolean =
        getNotifiedIdsFlow().first().contains(id)

    /** Marca [id] como ya notificado para que el banner no vuelva a aparecer. */
    suspend fun markAsNotified(id: String) {
        context.usageDataStore.edit { prefs ->
            val current = prefs[UsageKeys.NOTIFIED_UNLOCK_IDS] ?: emptySet()
            prefs[UsageKeys.NOTIFIED_UNLOCK_IDS] = current + id
        }
    }

    // ── Eventos Especiales ──────────────────────────────────────────────────

    /** Flow con el ID del último evento especial que se mostró automáticamente. */
    fun getLastShownEventIdFlow(): Flow<String?> =
        context.usageDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[UsageKeys.LAST_SHOWN_EVENT_ID] }

    /** Registra que el evento con [id] ya se mostró automáticamente. */
    suspend fun markEventAsShown(id: String) {
        context.usageDataStore.edit { prefs ->
            prefs[UsageKeys.LAST_SHOWN_EVENT_ID] = id
        }
    }

    // ── Notificaciones de Eventos ───────────────────────────────────────────

    /** Flow con el ID del último evento que ya envió notificación push. */
    fun getNotifiedEventIdFlow(): Flow<String?> =
        context.usageDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs -> prefs[UsageKeys.NOTIFIED_EVENT_ID] }

    /** Marca el evento [id] como ya notificado vía push. */
    suspend fun markEventAsNotified(id: String) {
        context.usageDataStore.edit { prefs ->
            prefs[UsageKeys.NOTIFIED_EVENT_ID] = id
        }
    }
}
