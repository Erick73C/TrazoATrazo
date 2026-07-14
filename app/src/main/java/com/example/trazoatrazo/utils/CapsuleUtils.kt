package com.example.trazoatrazo.utils

import com.example.trazoatrazo.data.capsulesList
import com.example.trazoatrazo.domain.model.TimeCapsule

/**
 * Funciones de consulta puras sobre [capsulesList] — centraliza toda la
 * lógica de "¿esta cápsula/dibujo está bloqueado?" para que [com.example.trazoatrazo.ui.components.DrawingCard]
 * y `AppNavigation` no dupliquen la comparación de fechas.
 *
 * No depende de DataStore ni de ningún ViewModel: solo compara
 * [TimeCapsule.unlockDate] contra la fecha actual vía [DateProvider], así
 * que puede llamarse directamente desde un Composable sin efectos
 * secundarios ni recomposición innecesaria (aunque el resultado puede
 * cambiar si la app queda abierta cruzando la medianoche — ver nota en
 * [isCapsuleLocked]).
 */
object CapsuleUtils {

    /**
     * Busca la cápsula asociada a [drawingId], si existe.
     * Devuelve `null` si ese dibujo no está ligado a ninguna cápsula
     * (es decir, es un dibujo normal, siempre accesible).
     */
    fun findCapsuleFor(drawingId: String): TimeCapsule? =
        capsulesList.firstOrNull { it.drawingId == drawingId }

    /**
     * Busca una cápsula por su propio [capsuleId] (no por el drawingId que
     * desbloquea). Útil cuando ya se tiene la referencia a la cápsula, por
     * ejemplo desde el catálogo de notificaciones pendientes.
     */
    fun findCapsuleById(capsuleId: String): TimeCapsule? =
        capsulesList.firstOrNull { it.id == capsuleId }

    /**
     * true si [drawingId] pertenece a una cápsula y esa cápsula **todavía
     * no llegó** a su [TimeCapsule.unlockDate]. Si [drawingId] no tiene
     * cápsula asociada, siempre devuelve `false` (dibujo normal, sin
     * restricción).
     *
     * Nota: como no hay un observador de "cambio de día" en tiempo real,
     * si la app se queda abierta exactamente cuando cruza la medianoche de
     * la fecha de desbloqueo, el estado no se actualiza solo — se refleja
     * en la siguiente recomposición/navegación. Para el caso de uso de esta
     * app (revisar cápsulas ocasionalmente) esto es aceptable y evita
     * complejidad innecesaria con temporizadores.
     */
    fun isCapsuleLocked(drawingId: String): Boolean {
        val capsule = findCapsuleFor(drawingId) ?: return false
        return !DateProvider.hasReached(capsule.unlockDate)
    }

    /**
     * Días restantes para que [drawingId] se desbloquee. Devuelve 0 si ya
     * está desbloqueado o si no tiene cápsula asociada.
     */
    fun daysRemainingFor(drawingId: String): Long {
        val capsule = findCapsuleFor(drawingId) ?: return 0L
        return DateProvider.daysUntil(capsule.unlockDate).coerceAtLeast(0L)
    }

    /**
     * Todas las cápsulas que ya están desbloqueadas hoy. Útil para pantallas
     * de resumen/galería que quieran listar solo lo accesible.
     */
    fun unlockedCapsules(): List<TimeCapsule> =
        capsulesList.filter { DateProvider.hasReached(it.unlockDate) }

    /**
     * Todas las cápsulas que aún están bloqueadas, ordenadas por la más
     * próxima a desbloquearse primero. Útil para un futuro "próximas
     * sorpresas" en Ajustes o Home.
     */
    fun lockedCapsulesSortedByClosest(): List<TimeCapsule> =
        capsulesList
            .filter { !DateProvider.hasReached(it.unlockDate) }
            .sortedBy { it.unlockDate }
}