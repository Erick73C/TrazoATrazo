package com.example.trazoatrazo


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.trazoatrazo.navigation.AppNavigation
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.TrazoATrazoTheme

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val immersiveMode by settingsViewModel.immersiveMode.collectAsStateWithLifecycle()
            
            // Aplicar modo inmersivo cada vez que cambie el estado
            androidx.compose.runtime.LaunchedEffect(immersiveMode) {
                applyImmersiveMode(immersiveMode)
            }

            TrazoATrazoTheme {
                AppNavigation()
            }
        }
    }

    private fun applyImmersiveMode(enabled: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        windowInsetsController.let { controller ->
            if (enabled) {
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
}
