package com.example.trazoatrazo.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trazoatrazo.data.BackgroundPreferences
import com.example.trazoatrazo.data.FontPreferences
import com.example.trazoatrazo.data.SystemPreferences
import com.example.trazoatrazo.data.ThemePreferences
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.AppFont
import com.example.trazoatrazo.ui.theme.AppTheme
import com.example.trazoatrazo.ui.theme.MessageStyle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs = ThemePreferences(application)
    private val backgroundPrefs = BackgroundPreferences(application)
    private val fontPrefs = FontPreferences(application)
    private val systemPrefs = SystemPreferences(application)

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

    // ── Fondo dinámico (Optimizado con Cache Local) ───────────────────────────
    private val _backgroundConfig = MutableStateFlow(BackgroundConfig())
    val backgroundConfig: StateFlow<BackgroundConfig> = _backgroundConfig.asStateFlow()

    init {
        viewModelScope.launch {
            selectedTheme.flatMapLatest { theme ->
                backgroundPrefs.getConfigFlow(theme)
            }.collect { config ->
                if (persistJob == null || !persistJob!!.isActive) {
                    _backgroundConfig.value = config
                }
            }
        }
    }

    // ── Tipografía dinámica ───────────────────────────────────────────────────
    val selectedFont: StateFlow<AppFont> = fontPrefs.getFontFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppFont.QUICKSAND
        )

    val selectedMessageStyle: StateFlow<MessageStyle> = fontPrefs.getMessageStyleFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MessageStyle.NORMAL
        )

    // ── Sistema ───────────────────────────────────────────────────────────────
    val immersiveMode: StateFlow<Boolean> = systemPrefs.getImmersiveModeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    private var persistJob: Job? = null
    
    private fun updateConfig(update: (BackgroundConfig) -> BackgroundConfig) {
        val current = _backgroundConfig.value
        val updated = update(current)
        
        if (current == updated) return

        _backgroundConfig.value = updated

        persistJob?.cancel()
        persistJob = viewModelScope.launch {
            delay(300) 
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

    // ── Efectos Individuales ───────────────────────────────────────────────────
    fun setStarsEnabled(enabled: Boolean) = updateConfig { it.copy(stars = it.stars.copy(enabled = enabled)) }
    fun setStarsIntensity(intensity: Float) = updateConfig { it.copy(stars = it.stars.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setGrainEnabled(enabled: Boolean) = updateConfig { it.copy(grain = it.grain.copy(enabled = enabled)) }
    fun setGrainIntensity(intensity: Float) = updateConfig { it.copy(grain = it.grain.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setGlowEnabled(enabled: Boolean) = updateConfig { it.copy(glow = it.glow.copy(enabled = enabled)) }
    fun setGlowIntensity(intensity: Float) = updateConfig { it.copy(glow = it.glow.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setVignetteEnabled(enabled: Boolean) = updateConfig { it.copy(vignette = it.vignette.copy(enabled = enabled)) }
    fun setVignetteIntensity(intensity: Float) = updateConfig { it.copy(vignette = it.vignette.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setChromaticEnabled(enabled: Boolean) = updateConfig { it.copy(chromatic = it.chromatic.copy(enabled = enabled)) }
    fun setChromaticIntensity(intensity: Float) = updateConfig { it.copy(chromatic = it.chromatic.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setScanlinesEnabled(enabled: Boolean) = updateConfig { it.copy(scanlines = it.scanlines.copy(enabled = enabled)) }
    fun setScanlinesIntensity(intensity: Float) = updateConfig { it.copy(scanlines = it.scanlines.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setKaleidoscopeEnabled(enabled: Boolean) = updateConfig { it.copy(kaleidoscope = it.kaleidoscope.copy(enabled = enabled)) }
    fun setKaleidoscopeIntensity(intensity: Float) = updateConfig { it.copy(kaleidoscope = it.kaleidoscope.copy(intensity = intensity.coerceIn(0f, 1f))) }

    fun setWavesEnabled(enabled: Boolean) = updateConfig { it.copy(waves = it.waves.copy(enabled = enabled)) }
    fun setWavesIntensity(intensity: Float) = updateConfig { it.copy(waves = it.waves.copy(intensity = intensity.coerceIn(0f, 1f))) }

    // ── Global ────────────────────────────────────────────────────────────────
    fun setBackgroundSpeed(speed: Float) = updateConfig { it.copy(speed = speed.coerceIn(0.25f, 2.0f)) }
    fun setParticleSize(size: Float) = updateConfig { it.copy(particleSize = size.coerceIn(0.5f, 2.5f)) }

    fun resetBackgroundToThemeDefault() {
        viewModelScope.launch { backgroundPrefs.resetToThemeDefault() }
    }

    fun selectFont(font: AppFont) {
        if (selectedFont.value == font) return
        viewModelScope.launch { fontPrefs.saveFont(font) }
    }

    fun selectMessageStyle(style: MessageStyle) {
        if (selectedMessageStyle.value == style) return
        viewModelScope.launch { fontPrefs.saveMessageStyle(style) }
    }

    fun setImmersiveMode(enabled: Boolean) {
        viewModelScope.launch { systemPrefs.saveImmersiveMode(enabled) }
    }
}
