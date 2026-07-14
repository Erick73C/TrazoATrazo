package com.example.trazoatrazo.presentation.usage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.AppUsagePreferences

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Expone la actividad del usuario en el tiempo (racha de días abiertos e
 * ids de contenido ya notificado) como estado reactivo para la UI.
 *
 * Es la capa de presentación sobre [AppUsagePreferences] — no contiene
 * lógica de persistencia propia, solo traduce los `Flow` del DataStore a
 * `StateFlow` consumibles desde Composables, siguiendo el mismo patrón que
 * [com.example.trazoatrazo.presentation.settings.SettingsViewModel].
 *
 * Se instancia una sola vez desde `AppNavigation` (compartido entre
 * pantallas), igual que `SettingsViewModel` y `PixelArtViewModel`.
 */
class AppUsageViewModel(application: Application) : AndroidViewModel(application) {

    private val usagePrefs = AppUsagePreferences(application)

    /**
     * Cantidad de días *distintos* en que el usuario abrió la app.
     * Es el valor que consumen las tarjetas bloqueadas por
     * [com.example.trazoatrazo.domain.model.UnlockRequirement] (Fase 4).
     */
    val daysOpenedCount: StateFlow<Int> = usagePrefs.getDaysOpenedCountFlow()
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    /**
     * Ids de contenido (cápsulas o dibujos desbloqueables) para los que ya
     * se mostró el banner "🌻 Nuevo recuerdo desbloqueado". Evita que el
     * mismo aviso se repita en cada apertura.
     */
    val notifiedIds: StateFlow<Set<String>> = usagePrefs.getNotifiedIdsFlow()
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    /**
     * Registra la apertura de hoy en la racha. Debe llamarse **una sola vez
     * por sesión** — típicamente en un `LaunchedEffect(Unit)` a nivel de
     * `AppNavigation` o `MainActivity`, nunca dentro de un Composable que
     * se recomponga seguido.
     */
    fun registerAppOpenToday() {
        viewModelScope.launch {
            usagePrefs.registerAppOpenToday()
        }
    }

    /**
     * Marca [id] como ya notificado para que el banner de desbloqueo no
     * vuelva a mostrarse para ese mismo contenido.
     */
    fun markAsNotified(id: String) {
        viewModelScope.launch {
            usagePrefs.markAsNotified(id)
        }
    }

    /**
     * Verificación síncrona sobre el estado actual ya cargado en memoria
     * (usa [notifiedIds], no vuelve a leer DataStore). Útil para chequear
     * dentro de un `LaunchedEffect` sin lanzar una nueva suspensión.
     */
    fun isAlreadyNotified(id: String): Boolean =
        notifiedIds.value.contains(id)

    // ── Eventos Especiales ──────────────────────────────────────────────────

    /** ID del último evento especial que se mostró automáticamente. */
    val lastShownEventId: StateFlow<String?> = usagePrefs.getLastShownEventIdFlow()
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    /** Marca el evento con [id] como ya mostrado automáticamente. */
    fun markEventAsShown(id: String) {
        viewModelScope.launch {
            usagePrefs.markEventAsShown(id)
        }
    }
}
