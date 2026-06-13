package com.example.trazoatrazo.data

import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import kotlin.collections.emptyList
import androidx.compose.runtime.Immutable

// Modelos centralizados
@Immutable
data class DrawingItem(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val description: String,
    val bgColor:     Color = AppColors.Sombra,
    val accentColor: Color = AppColors.FlowersAccent
)

// Catálogo completo
val drawingCatalog: Map<String, List<DrawingItem>> = mapOf(

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
            id          = Routes.Drawings.IMPROVED_SUNFLOWER,
            emoji       = "🌞",
            title       = "Girasol Mejorado",
            description = "Con tallo y hojas animados",
            bgColor     = Color(0xFFFFF3CD),
            accentColor = Color(0xFF8B6914)
        ),
    ),

    Routes.Category.CARTOONS to listOf(
        DrawingItem(
            id          = Routes.Drawings.HEART,
            emoji       = "🤍",
            title       = "Corazón",
            description = "14 de febrero",
            bgColor     = Color(0xFFFFEBEE),
            accentColor = Color(0xFFE91E63)
        ),
        DrawingItem(
            id          = Routes.Drawings.BATMAN,
            emoji       = "🦇",
            title       = "Batman",
            description = "El caballero de la noche",
            accentColor = Color(0xFFFFD700)
        ),
        DrawingItem(
            id          = Routes.Drawings.TROFEO,
            emoji       = "🏆",
            title       = "Trofeo Inovatec",
            description = "Felicidades por tu logro",
            accentColor = Color(0xFFFFA000)
        ),
        DrawingItem(
            id          = Routes.Drawings.HARLEY,
            emoji       = "🤡",
            title       = "Harley Quinn",
            description = "Princesa del crimen",
            accentColor = Color(0xFFD32F2F)
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
            accentColor = Color(0xFF0288D1)
        ),
        DrawingItem(
            id          = Routes.Drawings.CHRISTMAS_TREE,
            emoji       = "🎄",
            title       = "Árbol de Navidad",
            description = "Luces y estrellas",
            accentColor = Color(0xFF2E7D32)
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