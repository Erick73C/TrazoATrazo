package com.example.trazoatrazo.domain.model

import androidx.compose.runtime.Immutable

/**
 * Requisito de desbloqueo por racha de uso para un dibujo específico.
 *
 * Se define en código (mismo patrón que [TimeCapsule] y [SpecialEvent]),
 * como un campo opcional dentro de `DrawingItem` en `DrawingsData.kt`
 * no como catálogo separado — cada dibujo desbloqueable declara
 * su propio requisito junto a su definición normal.
 *
 * El criterio de "días" es la **racha real de días distintos en que se
 * abrió la app** (no días de calendario transcurridos desde la primera
 * apertura), calculada a partir del `Set<String>` de fechas que guarda
 * [com.example.trazoatrazo.data.AppUsagePreferences].
 *
 * @property drawingId         Id del dibujo (`Routes.Drawings.*`) al que aplica este requisito.
 * @property requiredDaysOpened Cantidad de días distintos que el usuario debe haber
 *                               abierto la app para que el dibujo se desbloquee.
 * @property lockedMessage      Texto mostrado en la tarjeta mientras sigue bloqueado,
 *                               ej: "Se desbloquea en 3 días más".
 * @property unlockedMessage    Texto del banner mostrado la primera vez que se cruza
 *                               el umbral, ej: "🌻 Nuevo recuerdo desbloqueado".
 */
@Immutable
data class UnlockRequirement(
    val drawingId:          String,
    val requiredDaysOpened: Int,
    val lockedMessage:      String = "Sigue abriendo la app para desbloquear este recuerdo",
    val unlockedMessage:    String = "🌻 Nuevo recuerdo desbloqueado"
)