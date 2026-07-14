package com.example.trazoatrazo.utils

import com.example.trazoatrazo.data.specialEventsList
import com.example.trazoatrazo.domain.model.SpecialEvent

/**
 * Funciones de consulta puras sobre [specialEventsList] — determina si hay
 * un [SpecialEvent] activo hoy, comparando la fecha actual (vía
 * [DateProvider]) contra los rangos mes/día definidos en el catálogo.
 *
 * No depende de DataStore ni de ningún ViewModel: es una consulta pura,
 * igual que [CapsuleUtils], pensada para llamarse directamente desde un
 * Composable (por ejemplo `remember(Unit) { EventDetector.activeEventToday() }`
 * en `HomeScreen`).
 */
object EventDetector {

    /**
     * Devuelve el [SpecialEvent] activo hoy, o `null` si no hay ninguno.
     *
     * Si dos eventos llegaran a traslaparse en el catálogo (no debería
     * pasar si los rangos se definen con cuidado, ver comentario en
     * [com.example.trazoatrazo.data.specialEventsList]), se devuelve el
     * primero que coincida en orden de declaración — por eso es importante
     * mantener los rangos sin traslape al agregar eventos nuevos.
     */
    fun activeEventToday(): SpecialEvent? =
        specialEventsList.firstOrNull { event ->
            DateProvider.isTodayWithin(
                startMonth = event.startMonth,
                startDay   = event.startDay,
                endMonth   = event.endMonth,
                endDay     = event.endDay
            )
        }

    /**
     * Busca un evento por su [SpecialEvent.id], esté activo o no. Útil para
     * pantallas futuras (ej. un calendario de "próximos eventos") que
     * quieran mostrar el catálogo completo sin depender de la fecha actual.
     */
    fun findEventById(id: String): SpecialEvent? =
        specialEventsList.firstOrNull { it.id == id }

    /**
     * true si actualmente hay algún evento activo. Atajo equivalente a
     * `activeEventToday() != null`, usado en condiciones simples dentro de
     * Composables donde no se necesita el objeto completo.
     */
    fun hasActiveEvent(): Boolean = activeEventToday() != null
}