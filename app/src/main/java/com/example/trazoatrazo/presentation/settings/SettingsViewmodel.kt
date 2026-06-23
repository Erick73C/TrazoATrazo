package com.example.trazoatrazo.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.BackgroundPreferences
import com.example.trazoatrazo.data.FontPreferences
import com.example.trazoatrazo.data.ThemePreferences
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.AppFont
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs = ThemePreferences(application)
    private val backgroundPrefs = BackgroundPreferences(application)
    private val fontPrefs = FontPreferences(application)

    // ── Tema ──────────────────────────────────────────────────────────────────
    private val _themeReady = MutableStateFlow(false)
    val themeReady: StateFlow<Boolean> = _themeReady.asStateFlow()

    val selectedTheme: StateFlow<AppTheme> = themePrefs.getThemeFlow()
        .onEach { theme ->
            AppColors.applyTheme(theme)
            _themeReady.value = true
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppTheme.JJK_DARK
        )

    // ── Fondo dinámico ────────────────────────────────────────────────────────
    // Reacciona automáticamente al cambio de tema y a las preferencias guardadas
    val backgroundConfig: StateFlow<BackgroundConfig> = selectedTheme
        .flatMapLatest { theme -> backgroundPrefs.getConfigFlow(theme) }
        .distinctUntilChanged() // Evita emisiones si el objeto config es idéntico
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = BackgroundConfig()
        )

    // ── Tipografía dinámica ───────────────────────────────────────────────────
    val selectedFont: StateFlow<AppFont> = fontPrefs.getFontFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppFont.QUICKSAND
        )

    // ── Helper para actualizar y guardar la config ────────────────────────────
    private fun updateConfig(update: (BackgroundConfig) -> BackgroundConfig) {
        val current = backgroundConfig.value
        val updated = update(current)
        
        // Optimización: Solo guardamos si realmente hubo un cambio en los datos
        if (current == updated) return

        viewModelScope.launch {
            backgroundPrefs.saveConfig(updated)
        }
    }

    // ── Acciones de UI ───────────────────────────────────────────────────────
    fun selectTheme(theme: AppTheme) {
        if (selectedTheme.value == theme) return
        viewModelScope.launch {
            themePrefs.saveTheme(theme)
        }
    }

    // ── Partículas ────────────────────────────────────────────────────────────
    fun setParticlesEnabled(enabled: Boolean) = updateConfig { 
        it.copy(particles = it.particles.copy(enabled = enabled)) 
    }

    fun setParticlesIntensity(intensity: Float) = updateConfig { 
        it.copy(particles = it.particles.copy(intensity = intensity.coerceIn(0f, 1f))) 
    }

    fun toggleParticleType(type: SpecialParticleType) = updateConfig { current ->
        val newList = if (current.activeTypes.contains(type)) {
            if (current.activeTypes.size > 1) current.activeTypes - type else current.activeTypes
        } else {
            current.activeTypes + type
        }
        current.copy(activeTypes = newList)
    }

    // ── Estrellas ─────────────────────────────────────────────────────────────
    fun setStarsEnabled(enabled: Boolean) = updateConfig { 
        it.copy(stars = it.stars.copy(enabled = enabled)) 
    }

    fun setStarsIntensity(intensity: Float) = updateConfig { 
        it.copy(stars = it.stars.copy(intensity = intensity.coerceIn(0f, 1f))) 
    }

    // ── Pétalos ───────────────────────────────────────────────────────────────
    fun setPetalsEnabled(enabled: Boolean) = updateConfig { 
        it.copy(petals = it.petals.copy(enabled = enabled)) 
    }

    fun setPetalsIntensity(intensity: Float) = updateConfig { 
        it.copy(petals = it.petals.copy(intensity = intensity.coerceIn(0f, 1f))) 
    }

    // ── Grain ─────────────────────────────────────────────────────────────────
    fun setGrainEnabled(enabled: Boolean) = updateConfig { 
        it.copy(grain = it.grain.copy(enabled = enabled)) 
    }

    fun setGrainIntensity(intensity: Float) = updateConfig { 
        it.copy(grain = it.grain.copy(intensity = intensity.coerceIn(0f, 1f))) 
    }

    // ── Brillo ────────────────────────────────────────────────────────────────
    fun setGlowEnabled(enabled: Boolean) = updateConfig { 
        it.copy(glow = it.glow.copy(enabled = enabled)) 
    }

    fun setGlowIntensity(intensity: Float) = updateConfig { 
        it.copy(glow = it.glow.copy(intensity = intensity.coerceIn(0f, 1f))) 
    }

    // ── Global ────────────────────────────────────────────────────────────────
    fun setBackgroundSpeed(speed: Float) = updateConfig { 
        it.copy(speed = speed.coerceIn(0.25f, 2.0f)) 
    }

    fun resetBackgroundToThemeDefault() {
        viewModelScope.launch { backgroundPrefs.resetToThemeDefault() }
    }

    fun selectFont(font: AppFont) {
        if (selectedFont.value == font) return
        viewModelScope.launch {
            fontPrefs.saveFont(font)
        }
    }
}
