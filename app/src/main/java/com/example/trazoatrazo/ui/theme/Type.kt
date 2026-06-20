package com.example.trazoatrazo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Typography dinámica según la fuente elegida ────────────────────────────────
// Antes era un val fijo; ahora es una función que genera la Typography
// correcta dependiendo del AppFont activo.
fun typographyFor(font: AppFont): Typography {
    val family = fontFamilyFor(font)

    return Typography(
        bodyLarge = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.Normal,
            fontSize      = 16.sp,
            lineHeight    = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.Normal,
            fontSize      = 14.sp,
            lineHeight    = 20.sp,
            letterSpacing = 0.25.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize   = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize   = 16.sp,
            lineHeight = 24.sp
        ),
        labelSmall = TextStyle(
            fontFamily    = family,
            fontWeight    = FontWeight.Medium,
            fontSize      = 11.sp,
            lineHeight    = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

// ── Typography por defecto (compatibilidad con código que aún la referencie) ───
val Typography = typographyFor(AppFont.QUICKSAND)