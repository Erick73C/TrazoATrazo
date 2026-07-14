package com.example.trazoatrazo.data

import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import kotlin.collections.emptyList
import androidx.compose.runtime.Immutable
import com.example.trazoatrazo.domain.model.UnlockRequirement

// Modelos centralizados
@Immutable
data class DrawingItem(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val description: String,
    val bgColor:     Color = AppColors.Sombra,
    val accentColor: Color = AppColors.FlowersAccent,
    val capsuleId:   String? = null,
    val unlockRequirement: UnlockRequirement? = null
)

// Catálogo completo
val drawingCatalog: Map<String, List<DrawingItem>>
    get() = mapOf(

        Routes.Category.FLOWERS to listOf(
            DrawingItem(
                id          = Routes.Drawings.GIRASOL,
                emoji       = "🌻",
                title       = "Girasol",
                description = "Especial para ti :D",
                bgColor     = Color(0xFFFFF9C4),
                accentColor = Color(0xFFF9A825)
            ),
            DrawingItem(
                id          = Routes.Drawings.FLORES,
                emoji       = "💐",
                title       = "Ramo de Flores",
                description = "Ten un bonito día :D",
                bgColor     = Color(0xFFFCE4EC),
                accentColor = Color(0xFFE75480)
            ),
            DrawingItem(
                id          = Routes.Drawings.ROSA,
                emoji       = "🌹",
                title       = "Rosa",
                description = "Un clásico romántico",
                bgColor     = Color(0xFFFFEBEE),
                accentColor = Color(0xFFD32F2F)
            ),
            DrawingItem(
                id          = Routes.Drawings.TULIPAN,
                emoji       = "🌷",
                title       = "Tulipán",
                description = "Colorido y elegante",
                bgColor     = Color(0xFFF3E5F5),
                accentColor = Color(0xFF9C27B0)
            ),
            DrawingItem(
                id          = Routes.Drawings.IMPROVED_SUNFLOWER,
                emoji       = "🌞",
                title       = "Girasol Mejorado",
                description = "Con tallo y hojas animados",
                bgColor     = Color(0xFFFFF3CD),
                accentColor = Color(0xFF8B6914),
                unlockRequirement = UnlockRequirement(
                    drawingId          = Routes.Drawings.IMPROVED_SUNFLOWER,
                    requiredDaysOpened = 2,
                    lockedMessage      = "Vuelve un par de días más para desbloquearlo 🌞",
                    unlockedMessage    = "🌻 Nuevo recuerdo desbloqueado: Girasol Mejorado"
                )
            ),
        ),

        Routes.Category.CARTOONS to listOf(
            DrawingItem(
                id          = Routes.Drawings.HEART,
                emoji       = "🤍",
                title       = "Corazón",
                description = "14 de febrero",
                bgColor     = Color(0xFFFFEBEE),
                accentColor = Color(0xFFE91E63),
                capsuleId   = "capsula_san_valentin"
            ),
            DrawingItem(
                id          = Routes.Drawings.BATMAN,
                emoji       = "🦇",
                title       = "Batman",
                description = "El caballero de la noche",
                accentColor = Color(0xFFFFD700),
                unlockRequirement = UnlockRequirement(
                    drawingId          = Routes.Drawings.BATMAN,
                    requiredDaysOpened = 10,
                    lockedMessage      = "Este dibujo espera pacientemente en la oscuridad 🦇",
                    unlockedMessage    = "🌻 Nuevo recuerdo desbloqueado: Batman"
                )
            ),
            DrawingItem(
                id = Routes.Drawings.TROFEO,
                emoji = "🏆",
                title = "Trofeo Inovatec",
                description = "Felicidades por tu logro",
                accentColor = Color(0xFFFFA000),
                unlockRequirement = UnlockRequirement(
                    drawingId = Routes.Drawings.TROFEO,
                    requiredDaysOpened = 1,
                    lockedMessage = "Ábrela de nuevo mañana para verlo 🏆",
                    unlockedMessage = "🌻 Nuevo recuerdo desbloqueado: Trofeo"
                )
            ),
            DrawingItem(
                id          = Routes.Drawings.HARLEY,
                emoji       = "🤡",
                title       = "Harley Quinn",
                description = "Princesa del crimen",
                accentColor = Color(0xFFD32F2F),
                unlockRequirement = UnlockRequirement(
                    drawingId          = Routes.Drawings.HARLEY,
                    requiredDaysOpened = 14,
                    lockedMessage      = "Sigue abriendo la app para desbloquearla 🤡",
                    unlockedMessage    = "🌻 Nuevo recuerdo desbloqueado: Harley Quinn"
                )
            ),
        ),

        Routes.Category.ANIMALS to listOf(
            DrawingItem(
                id          = Routes.Drawings.TURTLE,
                emoji       = "🐢",
                title       = "Tortuga",
                description = "Animación paso a paso",
                bgColor     = Color(0xFFE8F5E9),
                accentColor = Color(0xFF2E7D32)
            ),
            DrawingItem(
                id          = Routes.Drawings.PERRITO,
                emoji       = "🐶",
                title       = "Perrito",
                description = "Tu mejor amigo",
                bgColor     = Color(0xFFEFEBE9),
                accentColor = Color(0xFF795548)
            ),
            DrawingItem(
                id          = Routes.Drawings.CAMALEON,
                emoji       = "🦎",
                title       = "Camaleón",
                description = "Cambia de colores",
                bgColor     = Color(0xFFF1F8E9),
                accentColor = Color(0xFF689F38)
            ),
            DrawingItem(
                id          = Routes.Drawings.AXOLOTE,
                emoji       = "🌸",
                title       = "Axolote",
                description = "El monstruo del agua",
                bgColor     = Color(0xFFFCE4EC),
                accentColor = Color(0xFFF06292)
            ),
            DrawingItem(
                id          = Routes.Drawings.CAT_BLACK,
                emoji       = "🐈‍⬛",
                title       = "Gatito negro",
                description = "Un lindo gatito",
                bgColor     = Color(0xFF212121),
                accentColor = Color(0xFFFFD700)
            ),
        ),

        Routes.Category.SPRING to listOf(
            DrawingItem(
                id          = Routes.Drawings.CHERRY_TREE,
                emoji       = "🌸",
                title       = "Cerezo",
                description = "Flor de primavera",
                accentColor = Color(0xFFFF91A4)
            ),
            DrawingItem(
                id          = Routes.Drawings.MARIQUITA,
                emoji       = "🐞",
                title       = "Mariquita",
                description = "Pequeña y roja",
                bgColor     = Color(0xFFFFF3E0),
                accentColor = Color(0xFFE53935)
            ),
            DrawingItem(
                id          = Routes.Drawings.BUTTERFLY,
                emoji       = "🦋",
                title       = "Mariposa",
                description = "Colores vivos",
                accentColor = Color(0xFF00BCD4)
            ),
        ),

        Routes.Category.WINTER to listOf(
            DrawingItem(
                id          = Routes.Drawings.SNOWMAN,
                emoji       = "☃️",
                title       = "Muñeco de Nieve",
                description = "¡Qué frío!",
                accentColor = Color(0xFF0288D1),
                unlockRequirement = UnlockRequirement(
                    drawingId          = Routes.Drawings.SNOWMAN,
                    requiredDaysOpened = 5,
                    lockedMessage      = "Este muñeco de nieve se está formando... ☃️",
                    unlockedMessage    = "🌻 Nuevo recuerdo desbloqueado: Muñeco de Nieve"
                )
            ),
            DrawingItem(
                id          = Routes.Drawings.CHRISTMAS_TREE,
                emoji       = "🎄",
                title       = "Árbol de Navidad",
                description = "Luces y estrellas",
                accentColor = Color(0xFF2E7D32),
                capsuleId   = "capsula_navidad"
            ),
            DrawingItem(
                id = Routes.Drawings.COPONIEVE,
                emoji = "❄️",
                title = "Copo de nieve",
                description = "Cascadas de copos de nieve",
                accentColor = Color(0xFF0288D1)
            )
        ),

        Routes.Category.SPECIAL to listOf(
            DrawingItem(
                id          = Routes.Drawings.carta,
                emoji       = "💌",
                title       = "Carta para ti",
                description = "Un mensaje especial",
                bgColor     = Color(0xFF1E1B2E),
                accentColor = Color(0xFF9333EA)
            ),
        ),

        Routes.Category.GALLERY to emptyList(), // Las imágenes se gestionan en GalleryScreen
    )