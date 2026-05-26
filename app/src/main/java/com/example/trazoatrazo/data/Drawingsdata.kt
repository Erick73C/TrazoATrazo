package com.example.trazoatrazo.data

import androidx.compose.ui.graphics.Color
import com.example.trazoatrazo.navigation.Routes
import com.example.trazoatrazo.ui.theme.AppColors
import kotlin.collections.emptyList

// Modelos centralizados

data class Category(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val accentColor: Color
)

data class DrawingItem(
    val id:          String,
    val emoji:       String,
    val title:       String,
    val description: String,
    val bgColor:     Color = AppColors.Sombra,
    val accentColor: Color = AppColors.FlowersAccent
)

// Lista de categorías
val allCategories = listOf(
    Category(Routes.Category.FLOWERS,  "🌸", "Flores",   AppColors.FlowersAccent),
    Category(Routes.Category.CARTOONS, "🎭", "Cartoons", AppColors.CartoonsAccent),
    Category(Routes.Category.ANIMALS,  "🐾", "Animales", AppColors.AnimalsAccent),
    Category(Routes.Category.SPECIAL,  "⭐", "Especial", Color(0xFF9333EA)),
    Category(Routes.Category.GALLERY,  "📷", "Galería",  Color(0xFFF06292)),
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
            bgColor     = Color(0xFFF5F5F5),
            accentColor = Color(0xFF1C1C1C)
        ),
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