package com.example.trazoatrazo.data

import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.domain.model.SpecialEvent
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.background.SpecialParticleType

/**
 * Catálogo estático de Eventos Especiales — mismo patrón que [capsulesList].
 *
 * A diferencia de [com.example.trazoatrazo.domain.model.TimeCapsule], estos
 * eventos son **recurrentes**: se definen por mes/día (sin año) y se activan
 * automáticamente cada vez que la fecha actual cae dentro de su "temporada"
 * (unos días antes/después de la fecha exacta), ver [EventDetector].
 *
 * Los colores de acento reutilizan combinaciones ya existentes en la app
 * (paletas de las pantallas de dibujo correspondientes) mientras se definen
 * paletas propias más adelante.
 */
val specialEventsList: List<SpecialEvent> = listOf(

    // ── Año Nuevo 🎆 (30 dic → 2 ene) ────────────────────────────────────────
    SpecialEvent(
        id            = "evento_anio_nuevo",
        name          = "Año Nuevo",
        startMonth    = 12, startDay = 30,
        endMonth      = 1,  endDay   = 2,
        accentColor   = Color(0xFFD4A017),   // dorado (reutilizado de Noche de Oro)
        particleType  = SpecialParticleType.SPARKLE,
        bannerEmoji   = "🎆",
        bannerMessage = "¡Un nuevo año está por comenzar! 🎆",
        extraWelcomeMessages = listOf(
            "🎆 Que este nuevo año te traiga muchas cosas bonitas",
            "✨ Un año nuevo, nuevas oportunidades",
            "🥂 Feliz año nuevo, que todo te salga increíble"
        )
    ),

    // ── Navidad 🎄 (20 dic → 26 dic) ─────────────────────────────────────────
    SpecialEvent(
        id            = "evento_navidad",
        name          = "Navidad",
        startMonth    = 12, startDay = 20,
        endMonth      = 12, endDay   = 26,
        accentColor   = Color(0xFF2E7D32),   // verde (reutilizado de ChristmasTreeScreen)
        particleType  = SpecialParticleType.SNOWFLAKE,
        bannerEmoji   = "🎄",
        bannerMessage = "¡Feliz Navidad! 🎄",
        extraWelcomeMessages = listOf(
            "🎄 Que la magia de la Navidad te acompañe",
            "🎁 Feliz Navidad, mereces todo lo bonito",
            "⛄ Que esta Navidad esté llena de momentos especiales"
        ),
        temporaryDrawingId         = Routes.Drawings.CHRISTMAS_TREE,
        temporaryDrawingCategoryId = Routes.Category.WINTER
    ),

    // ── Cumpleaños 🎂 (23 nov → 25 nov) ──────────────────────────────────────
    SpecialEvent(
        id            = "evento_cumpleanos",
        name          = "Cumpleaños",
        startMonth    = 11, startDay = 23,
        endMonth      = 11, endDay   = 25,
        accentColor   = Color(0xFFF59E0B),   // ámbar cálido (reutilizado de tema Ámbar)
        particleType  = SpecialParticleType.SPARKLE,
        bannerEmoji   = "🎂",
        bannerMessage = "¡Feliz cumpleaños! 🎂",
        extraWelcomeMessages = listOf(
            "🎂 ¡Feliz cumpleaños! Espero que sea un día increíble",
            "🎉 Un año más de vida, un año más de cosas bonitas",
            "🎈 Que se cumplan todos tus deseos hoy"
        )
    ),

    // ── Halloween 🎃 (29 oct → 1 nov) ────────────────────────────────────────
    SpecialEvent(
        id            = "evento_halloween",
        name          = "Halloween",
        startMonth    = 10, startDay = 29,
        endMonth      = 11, endDay   = 1,
        accentColor   = Color(0xFFFF6D00),   // naranja (reutilizado de ButterflyScreen)
        particleType  = SpecialParticleType.EMBER,
        bannerEmoji   = "🎃",
        bannerMessage = "¡Feliz Halloween! 🎃",
        extraWelcomeMessages = listOf(
            "🎃 Que tengas un Halloween muy divertido",
            "👻 Boo! Espero que no te haya asustado jaja",
            "🕸️ Feliz noche de brujas"
        )
    ),

    // ── San Valentín 💘 (12 feb → 16 feb) ────────────────────────────────────
    SpecialEvent(
        id            = "evento_san_valentin",
        name          = "San Valentín",
        startMonth    = 2, startDay = 12,
        endMonth      = 2, endDay   = 16,
        accentColor   = Color(0xFFE8002D),   // rojo (reutilizado de HeartScreen)
        particleType  = SpecialParticleType.HEART_S,
        bannerEmoji   = "💘",
        bannerMessage = "¡Feliz San Valentín! 💘",
        extraWelcomeMessages = listOf(
            "💘 Feliz día del amor y la amistad",
            "❤️ Espero que tengas un San Valentín muy bonito",
            "💌 Un día para recordar a las personas que quieres"
        ),
        temporaryDrawingId         = Routes.Drawings.HEART,
        temporaryDrawingCategoryId = Routes.Category.CARTOONS
    ),

    // ── Independencia de México 🇲🇽 (14 sep → 18 sep) ────────────────────────
    SpecialEvent(
        id            = "evento_independencia_mx",
        name          = "Independencia de México",
        startMonth    = 7, startDay = 13,
        endMonth      = 7, endDay   = 14,
        accentColor   = Color(0xFFCE1126),   // rojo bandera mexicana
        particleType  = SpecialParticleType.SPARKLE,
        bannerEmoji   = "🇲🇽",
        bannerMessage = "¡Viva México! 🇲🇽",
        extraWelcomeMessages = listOf(
            "🇲🇽 ¡Viva México! Feliz mes patrio",
            "🎉 Que vivas con orgullo estas fechas",
            "🌮 Feliz día de la Independencia"
        )
    ),
)