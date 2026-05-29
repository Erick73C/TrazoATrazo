package com.example.trazoatrazo.domain.model

import androidx.compose.runtime.Immutable

enum class MemoryCategory(val label: String, val emoji: String) {
    PERSONAL("Nosotros",          "💜"),
    INOVATEC("Inovatec",          "🚀"),
    PROGRAMAS("Programas",        "🎨"),
    ESPECIALES("Especiales", "✨")
}

@Immutable
data class Memory(
    val id:          Int,
    val drawableRes: Int,
    val title:       String,
    val description: String,
    val category:    MemoryCategory = MemoryCategory.PERSONAL
)