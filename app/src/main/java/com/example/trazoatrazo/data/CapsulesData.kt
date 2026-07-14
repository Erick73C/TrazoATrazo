package com.example.trazoatrazo.data

import com.example.trazoatrazo.domain.model.TimeCapsule
import com.example.trazoatrazo.navigation.Routes
import java.time.LocalDate
//TODO CAMBIAR LAS FECHAS A 26 LAS QUE YA PASARON Y ASIGNARLES UNA PANTALLA
/**
 * Catálogo estático de Cápsulas del Tiempo — mismo patrón que [MemoriesData].
 *
 * Cada cápsula se desbloquea **una sola vez** al llegar [TimeCapsule.unlockDate]
 * y permanece desbloqueada para siempre después. Para fechas recurrentes cada
 * año (Navidad, cumpleaños) que deban repetirse anualmente, usar
 * [com.example.trazoatrazo.domain.model.SpecialEvent] (Fase 3) en vez de esto.
 */
val capsulesList: List<TimeCapsule> = listOf(

    // ── Navidad 🎄 (25 dic 2026) — usa el árbol de Navidad ya existente ────
    TimeCapsule(
        id            = "capsula_navidad",
        title         = "Un regalo navideño",
        emoji         = "🎄",
        unlockDate    = LocalDate.of(2026, 12, 25),
        categoryId    = Routes.Category.WINTER,
        drawingId     = Routes.Drawings.CHRISTMAS_TREE,
        lockedMessage = "Falta para Navidad... 🎁",
        unlockedMessage = "🌟 ¡Feliz Navidad! Esta cápsula ya se abrió"
    ),

    // ── Año Nuevo 🎆 (1 ene 2027 — ya pasó este año) ───────────────────────
    TimeCapsule(
        id            = "capsula_anio_nuevo",
        title         = "Año Nuevo",
        emoji         = "🎆",
        unlockDate    = LocalDate.of(2027, 1, 1),
        categoryId    = Routes.Category.WINTER,
        drawingId     = "anio_nuevo",              // PENDIENTE: crear pantalla
        lockedMessage = "Esta cápsula abre con el nuevo año...",
        unlockedMessage = "🎇 ¡Feliz Año Nuevo!"
    ),

    // ── Día de Reyes Magos 👑 (6 ene 2027 — ya pasó este año) ──────────────
    TimeCapsule(
        id            = "capsula_reyes_magos",
        title         = "Día de Reyes",
        emoji         = "👑",
        unlockDate    = LocalDate.of(2027, 1, 6),
        categoryId    = Routes.Category.WINTER,
        drawingId     = "reyes_magos",              // PENDIENTE: crear pantalla
        lockedMessage = "Los Reyes Magos aún no llegan...",
        unlockedMessage = "👑 ¡Feliz Día de Reyes!"
    ),

    // ── San Valentín 💘 (14 feb 2027 — ya pasó este año) — usa el corazón ──
    TimeCapsule(
        id            = "capsula_san_valentin",
        title         = "San Valentín",
        emoji         = "💘",
        unlockDate    = LocalDate.of(2026, 7, 13),
        categoryId    = Routes.Category.CARTOONS,
        drawingId     = Routes.Drawings.HEART,
        lockedMessage = "Este corazón espera el 14 de febrero...",
        unlockedMessage = "❤️ ¡Feliz San Valentín!"
    ),

    // ── 21 de marzo — Día de regalar flores amarillas 🌻 (ya pasó este año) ─
    TimeCapsule(
        id            = "capsula_flores_amarillas",
        title         = "Flores amarillas",
        emoji         = "🌻",
        unlockDate    = LocalDate.of(2027, 3, 21),
        categoryId    = Routes.Category.FLOWERS,
        drawingId     = "flores_amarillas",         // PENDIENTE: crear pantalla
        lockedMessage = "Esta flor espera el 21 de marzo...",
        unlockedMessage = "🌻 ¡Día de regalar flores amarillas!"
    ),

    // Independencia de México 🇲🇽 (16 sep 2026 — falta este año)
    TimeCapsule(
        id            = "capsula_independencia_mx",
        title         = "Independencia de México",
        emoji         = "🇲🇽",
        unlockDate    = LocalDate.of(2026, 9, 16),
        categoryId    = Routes.Category.SPECIAL,
        drawingId     = "independencia_mx",         // PENDIENTE: crear pantalla
        lockedMessage = "Esta cápsula abre el 16 de septiembre...",
        unlockedMessage = "🇲🇽 ¡Viva México!"
    ),

    // Día de Muertos 💀🌼 (1 nov 2026 — falta este año)
    TimeCapsule(
        id            = "capsula_dia_muertos",
        title         = "Día de Muertos",
        emoji         = "💀",
        unlockDate    = LocalDate.of(2026, 11, 1),
        categoryId    = Routes.Category.SPECIAL,
        drawingId     = "dia_de_muertos",           // PENDIENTE: crear pantalla
        lockedMessage = "Esta cápsula abre el 1 de noviembre...",
        unlockedMessage = "🌼 Día de Muertos"
    ),

    //  Cumpleaños 🎂 (23 nov 2026 — falta este año)
    TimeCapsule(
        id            = "capsula_cumpleanos",
        title         = "Cumpleaños",
        emoji         = "🎂",
        unlockDate    = LocalDate.of(2026, 11, 23),
        categoryId    = Routes.Category.SPECIAL,
        drawingId     = "cumpleanos",                // PENDIENTE: crear pantalla
        lockedMessage = "Esta cápsula abre en tu día especial...",
        unlockedMessage = "🎂 ¡Feliz cumpleaños!"
    ),

    // ── Día que nos conocimos 🤍 (29 nov 2026 — falta este año)
    TimeCapsule(
        id            = "capsula_aniversario_conocernos",
        title         = "El día que nos conocimos",
        emoji         = "🤍",
        unlockDate    = LocalDate.of(2026, 11, 29),
        categoryId    = Routes.Category.SPECIAL,
        drawingId     = "aniversario_conocernos",    // PENDIENTE: crear pantalla
        lockedMessage = "Esta cápsula guarda un recuerdo especial...",
        unlockedMessage = "🤍 Un día que siempre recordaré"
    ),
)