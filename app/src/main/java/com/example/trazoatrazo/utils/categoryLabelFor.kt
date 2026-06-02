package com.example.trazoatrazo.utils

import androidx.compose.runtime.Stable
import com.example.trazoatrazo.navigation.Routes
@Stable
fun categoryLabelFor(categoryId: String): String = when (categoryId) {
    Routes.Category.FLOWERS  -> "Flores"
    Routes.Category.CARTOONS -> "Cartoons"
    Routes.Category.ANIMALS  -> "Animales"
    Routes.Category.SPRING   -> "Primavera"
    Routes.Category.WINTER   -> "Invierno"
    Routes.Category.SPECIAL  -> "Especial"
    else -> ""
}