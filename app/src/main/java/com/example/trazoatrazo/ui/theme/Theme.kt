package com.example.trazoatrazo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun TrazoATrazoTheme(
    content: @Composable () -> Unit
) {
    // Se recalcula solo cuando cambia AppColors.Vacio (es decir, cuando cambia el tema)
    val colorScheme = remember(AppColors.Vacio, AppColors.Tecnica, AppColors.Maldicion) {
        val isLight = AppColors.Vacio.red * 0.299f +
                AppColors.Vacio.green * 0.587f +
                AppColors.Vacio.blue * 0.114f > 0.5f

        if (isLight) {
            lightColorScheme(
                primary      = AppColors.Tecnica,
                secondary    = AppColors.Maldicion,
                tertiary     = AppColors.KiEspiritual,
                background   = AppColors.Vacio,
                surface      = AppColors.Sombra,
                onPrimary    = Color.White,
                onSecondary  = Color.White,
                onBackground = AppColors.Reversa,
                onSurface    = AppColors.Reversa
            )
        } else {
            darkColorScheme(
                primary      = AppColors.Tecnica,
                secondary    = AppColors.Maldicion,
                tertiary     = AppColors.KiEspiritual,
                background   = AppColors.Vacio,
                surface      = AppColors.Sombra,
                onPrimary    = Color.White,
                onSecondary  = Color.White,
                onBackground = AppColors.Reversa,
                onSurface    = AppColors.Reversa
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}