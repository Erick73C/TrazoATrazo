package com.example.trazoatrazo


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.trazoatrazo.data.AppUsagePreferences
import com.example.trazoatrazo.navigation.AppNavigation
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.LocalUiTransparency
import com.example.trazoatrazo.ui.theme.TrazoATrazoTheme
import com.example.trazoatrazo.utils.EventDetector
import com.example.trazoatrazo.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var usagePrefs: AppUsagePreferences

    // Launcher para pedir permiso de notificaciones (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkAndShowEventNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usagePrefs = AppUsagePreferences(this)
        
        NotificationHelper.createNotificationChannel(this)
        checkNotificationPermission()

        enableEdgeToEdge()
        setContent {
            val immersiveMode by settingsViewModel.immersiveMode.collectAsStateWithLifecycle()
            val uiTransparency by settingsViewModel.uiTransparency.collectAsStateWithLifecycle()
            
            // Aplicar modo inmersivo cada vez que cambie el estado
            androidx.compose.runtime.LaunchedEffect(immersiveMode) {
                applyImmersiveMode(immersiveMode)
            }

            CompositionLocalProvider(LocalUiTransparency provides uiTransparency) {
                TrazoATrazoTheme {
                    AppNavigation()
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkAndShowEventNotification()
                }
                else -> {
                    // Pide el permiso directamente
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Versiones anteriores no requieren permiso explícito en runtime
            checkAndShowEventNotification()
        }
    }

    private fun checkAndShowEventNotification() {
        lifecycleScope.launch {
            val activeEvent = EventDetector.activeEventToday()
            if (activeEvent != null) {
                val lastNotifiedId = usagePrefs.getNotifiedEventIdFlow().first()
                if (lastNotifiedId != activeEvent.id) {
                    NotificationHelper.showEventNotification(this@MainActivity, activeEvent.name)
                    usagePrefs.markEventAsNotified(activeEvent.id)
                }
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
