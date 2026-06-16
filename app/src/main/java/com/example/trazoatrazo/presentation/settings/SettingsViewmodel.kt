package com.example.trazoatrazo.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.ThemePreferences
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.trazoatrazo.data.BackgroundPreferences
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.EffectConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.background.defaultBackgroundConfigFor
import kotlinx.coroutines.flow.flatMapLatest


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs        = ThemePreferences(application)
    private val backgroundPrefs   = BackgroundPreferences(application)

    // ── Tema ──────────────────────────────────────────────────────────────────
    private val _selectedTheme = MutableStateFlow(AppTheme.JJK_DARK)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()

    private val _themeReady = MutableStateFlow(false)
    val themeReady: StateFlow<Boolean> = _themeReady.asStateFlow()

    // ── Fondo dinámico ────────────────────────────────────────────────────────
    private val _backgroundConfig = MutableStateFlow(BackgroundConfig())
    val backgroundConfig: StateFlow<BackgroundConfig> = _backgroundConfig.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────────────────
    init {
        // ── Cargar tema guardado ──────────────────────────────────────────────
        viewModelScope.launch {
            themePrefs.getThemeFlow().collect { savedTheme ->
                _selectedTheme.value = savedTheme
                AppColors.applyTheme(savedTheme)
                _themeReady.value = true
            }
        }

        // ── Config de fondo reactivo — se actualiza con tema Y con toggles ────
        viewModelScope.launch {
            _selectedTheme
                .flatMapLatest { theme ->
                    backgroundPrefs.getConfigFlow(theme)
                }
                .collect { config ->
                    _backgroundConfig.value = config
                }
        }
    }

    // ── Helper para actualizar y guardar la config completa ───────────────────
    private fun updateConfig(update: (BackgroundConfig) -> BackgroundConfig) {
        val current = _backgroundConfig.value
        val updated = update(current)
        viewModelScope.launch {
            backgroundPrefs.saveConfig(updated)
        }
    }

    // ── Seleccionar tema ──────────────────────────────────────────────────────
    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            _selectedTheme.value = theme
            AppColors.applyTheme(theme)
            themePrefs.saveTheme(theme)
        }
    }

    // ── Partículas ────────────────────────────────────────────────────────────
    fun setParticlesEnabled(enabled: Boolean) {
        updateConfig { it.copy(particles = it.particles.copy(enabled = enabled)) }
    }

    fun setParticlesIntensity(intensity: Float) {
        updateConfig { 
            it.copy(particles = it.particles.copy(intensity = intensity.coerceIn(0f, 1f))) 
        }
    }

    fun toggleParticleType(type: SpecialParticleType) {
        updateConfig { current ->
            val newList = if (current.activeTypes.contains(type)) {
                // Si ya está, lo quitamos (pero dejamos al menos uno si es posible)
                if (current.activeTypes.size > 1) current.activeTypes - type else current.activeTypes
            } else {
                current.activeTypes + type
            }
            current.copy(activeTypes = newList)
        }
    }

    // ── Estrellas ─────────────────────────────────────────────────────────────
    fun setStarsEnabled(enabled: Boolean) {
        updateConfig { it.copy(stars = it.stars.copy(enabled = enabled)) }
    }

    fun setStarsIntensity(intensity: Float) {
        updateConfig { 
            it.copy(stars = it.stars.copy(intensity = intensity.coerceIn(0f, 1f))) 
        }
    }

    // ── Pétalos ───────────────────────────────────────────────────────────────
    fun setPetalsEnabled(enabled: Boolean) {
        updateConfig { it.copy(petals = it.petals.copy(enabled = enabled)) }
    }

    fun setPetalsIntensity(intensity: Float) {
        updateConfig { 
            it.copy(petals = it.petals.copy(intensity = intensity.coerceIn(0f, 1f))) 
        }
    }

    // ── Grain ─────────────────────────────────────────────────────────────────
    fun setGrainEnabled(enabled: Boolean) {
        updateConfig { it.copy(grain = it.grain.copy(enabled = enabled)) }
    }

    fun setGrainIntensity(intensity: Float) {
        updateConfig { 
            it.copy(grain = it.grain.copy(intensity = intensity.coerceIn(0f, 1f))) 
        }
    }

    // ── Brillo ────────────────────────────────────────────────────────────────
    fun setGlowEnabled(enabled: Boolean) {
        updateConfig { it.copy(glow = it.glow.copy(enabled = enabled)) }
    }

    fun setGlowIntensity(intensity: Float) {
        updateConfig { 
            it.copy(glow = it.glow.copy(intensity = intensity.coerceIn(0f, 1f))) 
        }
    }

    // ── Global ────────────────────────────────────────────────────────────────
    fun setBackgroundSpeed(speed: Float) {
        updateConfig { it.copy(speed = speed.coerceIn(0.25f, 2.0f)) }
    }

    fun resetBackgroundToThemeDefault() {
        viewModelScope.launch { backgroundPrefs.resetToThemeDefault() }
    }
}
