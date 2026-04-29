package com.example.trazoatrazo.navigation

object Routes {

    // ── Pantallas principales ────────────────────────────────────────────────
    const val HOME     = "home"
    const val CATEGORY = "category/{categoryId}"

    // Helper para construir la ruta con parámetro
    fun category(categoryId: String) = "category/$categoryId"

    // ── Categorías ───────────────────────────────────────────────────────────
    object Category {
        const val FLOWERS  = "flowers"
        const val CARTOONS = "cartoons"
        const val ANIMALS  = "animals"
    }

    // ── Dibujos ──────────────────────────────────────────────────────────────
    // Cada dibujo tiene su propia ruta: "drawing/{categoryId}/{drawingId}"
    const val DRAWING = "drawing/{categoryId}/{drawingId}"

    fun drawing(categoryId: String, drawingId: String) =
        "drawing/$categoryId/$drawingId"

    // IDs de dibujos — flowers
    object Drawings {
        const val GIRASOL = "girasol"
        const val FLORES  = "flores"
    }
}