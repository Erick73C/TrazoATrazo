package com.example.trazoatrazo.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// DateProvider — fuente única de verdad para "qué fecha/hora es ahora"

// En vez de llamar LocalDate.now() suelto por todo el código (Cápsulas,
// Eventos Especiales, racha de días abiertos), todo pasa por aquí.
object DateProvider {

    // Formato usado para persistir fechas en DataStore (ej: "2026-07-11")
    private val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Fecha y hora actuales
    fun today(): LocalDate = LocalDate.now()

    fun now(): LocalDateTime = LocalDateTime.now()

    // Conversión a/desde String (para guardar en DataStore)
    // Formato: "yyyy-MM-dd" — ordenable como texto y fácil de comparar.
    fun todayAsString(): String = today().format(ISO_DATE_FORMATTER)

    fun dateToString(date: LocalDate): String = date.format(ISO_DATE_FORMATTER)

    fun stringToDate(raw: String): LocalDate? =
        try {
            LocalDate.parse(raw, ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            null
        }

    //  Helpers de comparación (usados por Cápsulas del Tiempo)

    /** true si [target] ya llegó o ya pasó (la cápsula está desbloqueada). */
    fun hasReached(target: LocalDate): Boolean =
        !today().isBefore(target)

    /** Días restantes hasta [target]. 0 o negativo = ya se puede acceder. */
    fun daysUntil(target: LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(today(), target)

    // ── Helpers de comparación (usados por Eventos Especiales) ────────────
    // Los eventos se definen por mes/día (recurrentes cada año), no por
    // fecha completa, para que "Navidad" se repita automáticamente cada 25/12
    // sin tener que actualizar el catálogo cada año.

    /**
     * true si hoy cae dentro del rango [startMonth]/[startDay] .. [endMonth]/[endDay],
     * inclusive. Soporta rangos que cruzan fin de año (ej: 28 dic → 2 ene).
     */
    fun isTodayWithin(
        startMonth: Int, startDay: Int,
        endMonth: Int, endDay: Int
    ): Boolean {
        val current = today()
        val currentMonthDay = current.monthValue * 100 + current.dayOfMonth
        val startMonthDay   = startMonth * 100 + startDay
        val endMonthDay     = endMonth * 100 + endDay

        return if (startMonthDay <= endMonthDay) {
            // Rango normal dentro del mismo año (ej: 20/12 → 26/12)
            currentMonthDay in startMonthDay..endMonthDay
        } else {
            // Rango que cruza fin de año (ej: 28/12 → 02/01)
            currentMonthDay >= startMonthDay || currentMonthDay <= endMonthDay
        }
    }
}