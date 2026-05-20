package com.example.trazoatrazo.navigation

object Routes {

    // ── Pantallas principales ────────────────────────────────────────────────
    const val HOME     = "home"
    const val CATEGORY = "category/{categoryId}"

    // Helper para construir la ruta con parámetro
    fun category(categoryId: String) = "category/$categoryId"

    const val CARTA_SCREEN = "carta_screen"

    const val LETTER_CONTENT = "letter_content"

    // ── Categorías ───────────────────────────────────────────────────────────
    object Category {
        const val FLOWERS  = "flowers"
        const val CARTOONS = "cartoons"
        const val ANIMALS  = "animals"
        const val SHAPES   = "shapes"
        const val SPECIAL  = "special"
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
        const val CARTOON = "cartoon"
        const val HEART   = "heart"

        const val TURTLE = "turtle"

        const val IMPROVED_SUNFLOWER = "improved_sunflower"

        const val CAT_BLACK = "cat_black"

        const val carta = "Carta"
    }
}