package com.example.trazoatrazo.utils

import com.example.trazoatrazo.data.drawingCatalog
import com.example.trazoatrazo.domain.model.UnlockRequirement

/**
 * Funciones de consulta puras sobre los [UnlockRequirement] declarados
 * dentro de cada `DrawingItem` en [drawingCatalog]
 * Misma lógica de que [CapsuleUtils]: sin DataStore, sin
 * ViewModel, solo comparaciones directas — la racha real de días (
 * [com.example.trazoatrazo.presentation.usage.AppUsageViewModel.daysOpenedCount])
 * se recibe como parámetro en vez de leerse aquí adentro.
 */
object UnlockUtils {

    /**
     * Busca el [UnlockRequirement] asociado a [drawingId] recorriendo todas
     * las categorías del catálogo. Devuelve `null` si ese dibujo no tiene
     * requisito de racha (siempre accesible).
     */
    fun findRequirementFor(drawingId: String): UnlockRequirement? =
        drawingCatalog.values
            .flatten()
            .firstOrNull { it.id == drawingId }
            ?.unlockRequirement

    /**
     * true si [drawingId] tiene un [UnlockRequirement] y la racha actual
     * ([daysOpened]) todavía no lo alcanza. Si el dibujo no tiene requisito,
     * siempre devuelve `false` (dibujo normal, sin restricción).
     */
    fun isDrawingLocked(drawingId: String, daysOpened: Int): Boolean {
        val requirement = findRequirementFor(drawingId) ?: return false
        return daysOpened < requirement.requiredDaysOpened
    }

    /**
     * Días de racha que faltan para desbloquear [drawingId]. Devuelve 0 si
     * ya está desbloqueado o si no tiene requisito asociado.
     */
    fun daysRemainingToUnlock(drawingId: String, daysOpened: Int): Int {
        val requirement = findRequirementFor(drawingId) ?: return 0
        return (requirement.requiredDaysOpened - daysOpened).coerceAtLeast(0)
    }

    /**
     * Todos los `drawingId` que tienen requisito de racha y que [daysOpened]
     * ya alcanza en este momento. Se usa desde `HomeScreen` para comparar
     * contra los ids ya notificados y detectar desbloqueos nuevos sin tener
     * que recorrer manualmente cada categoría del catálogo.
     */
    fun currentlyUnlockedDrawingIds(daysOpened: Int): List<String> =
        drawingCatalog.values
            .flatten()
            .filter { item ->
                val req = item.unlockRequirement
                req != null && daysOpened >= req.requiredDaysOpened
            }
            .map { it.id }
}