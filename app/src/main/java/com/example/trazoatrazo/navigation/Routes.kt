package com.example.trazoatrazo.navigation

object Routes {

    // ── Pantallas principales ────────────────────────────────────────────────
    const val HOME     = "home"
    const val CATEGORY = "category/{categoryId}"

    // Helper para construir la ruta con parámetro
    fun category(categoryId: String) = "category/$categoryId"

    const val SETTINGS = "settings"
    const val LETTER   = "letter"
    const val LETTER_CONTENT = "letter_content"

    const val MY_CREATIONS = "my_creations"

    const val PIXEL_EDITOR_NEW  = "pixel_editor_new"

    const val PIXEL_EDITOR_EDIT = "pixel_editor_edit/{artworkId}"
    const val PIXEL_REPLAY = "pixel_replay/{artworkId}"

    fun pixelEditorEdit(artworkId: String) = "pixel_editor_edit/$artworkId"
    fun pixelReplay(artworkId: String) = "pixel_replay/$artworkId"

    // ── Categorías ───────────────────────────────────────────────────────────
    object Category {
        const val FLOWERS  = "flowers"
        const val CARTOONS = "cartoons"
        const val ANIMALS  = "animals"
        const val SPECIAL  = "special"
        const val SPRING   = "spring"
        const val WINTER   = "winter"

        const val GALLERY  = "gallery"
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
        const val HEART   = "heart"
        const val BATMAN  = "batman"
        const val TROFEO  = "trofeo"
        const val HARLEY  = "harley"

        const val TURTLE = "turtle"

        const val IMPROVED_SUNFLOWER = "improved_sunflower"

        const val CAT_BLACK = "cat_black"

        const val CHERRY_TREE = "cherry_tree"

        const val SNOWMAN = "snowman"

        const val CHRISTMAS_TREE = "christmas_tree"
        const val COPONIEVE = "copo"

        const val BUTTERFLY = "butterfly"

        const val PIXEL_EDITOR = "pixel_editor"

        const val carta = "Carta"
    }
}