package com.example.trazoatrazo.domain.model
import androidx.compose.runtime.Immutable
import java.time.LocalDate

/**
 *TimeCapsule — un dibujo/recuerdo que permanece bloqueado hasta una fecha fija

 * Se define en código (catálogo estático, mismo patrón que MemoriesData.kt),
 * no en DataStore — las fechas de aniversarios/cumpleaños no cambian.
 * [id]              Identificador único de la cápsula (no confundir con drawingId).
 * [title]           Título que se muestra mientras está bloqueada, ej: "??? Sorpresa"
 *[emoji]           Emoji decorativo de la cápsula (aparece junto al candado).
 *[unlockDate]      Fecha exacta en que se desbloquea.
 *[categoryId]      Categoría del dibujo real (Routes.Category.*) al que da acceso.
 *[drawingId]       Id del dibujo real (Routes.Drawings.*) que se revela al desbloquear.
 *[lockedMessage]   Texto que se muestra en el candado animado antes de la fecha.
 * [unlockedMessage] Texto breve mostrado la primera vez que se desbloquea (opcional).
 **/
@Immutable
data class TimeCapsule(
    val id:              String,
    val title:           String,
    val emoji:           String = "🔒",
    val unlockDate:      LocalDate,
    val categoryId:      String,
    val drawingId:       String,
    val lockedMessage:   String = "Este recuerdo se abrirá pronto...",
    val unlockedMessage: String = "🌻 ¡Esta cápsula ya se abrió!"
)