package com.example.trazoatrazo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TrazoATrazoTheme(
    content: @Composable () -> Unit
) {
    // Verificamos si el fondo es claro u oscuro para elegir el onBackground/onSurface adecuado
    // Usamos una luminancia simple para determinar si el texto debe ser blanco o negro
    val isLightBackground = AppColors.Vacio.red * 0.299 + AppColors.Vacio.green * 0.587 + AppColors.Vacio.blue * 0.114 > 0.5

    // Creamos un ColorScheme que derive de AppColors (nuestro estado global reactivo)
    val colorScheme = if (isLightBackground) {
        lightColorScheme(
            primary      = AppColors.Tecnica,
            secondary    = AppColors.Maldicion,
            tertiary     = AppColors.KiEspiritual,
            background   = AppColors.Vacio,
            surface      = AppColors.Sombra,
            onPrimary    = if (isLightBackground) Color.White else Color.Black,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
