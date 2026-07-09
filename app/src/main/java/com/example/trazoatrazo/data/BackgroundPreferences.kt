package com.example.trazoatrazo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.trazoatrazo.ui.background.BackgroundConfig
import com.example.trazoatrazo.ui.background.EffectConfig
import com.example.trazoatrazo.ui.background.SpecialParticleType
import com.example.trazoatrazo.ui.background.defaultBackgroundConfigFor
import com.example.trazoatrazo.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// ── DataStore ────────────
private val Context.bgDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trazoatrazo_background"
)

// ── Keys ─────────────────
private object BgKeys {
    val USER_CUSTOMIZED     = booleanPreferencesKey("bg_user_customized")
    val PARTICLES_ENABLED   = booleanPreferencesKey("bg_particles_enabled")
    val PARTICLES_INTENSITY = floatPreferencesKey("bg_particles_intensity")
    val ACTIVE_TYPES        = stringPreferencesKey("bg_active_particle_types")
    val STARS_ENABLED       = booleanPreferencesKey("bg_stars_enabled")
    val STARS_INTENSITY     = floatPreferencesKey("bg_stars_intensity")
    val GRAIN_ENABLED       = booleanPreferencesKey("bg_grain_enabled")
    val GRAIN_INTENSITY     = floatPreferencesKey("bg_grain_intensity")
    val GLOW_ENABLED        = booleanPreferencesKey("bg_glow_enabled")
    val GLOW_INTENSITY      = floatPreferencesKey("bg_glow_intensity")
    val VIGNETTE_ENABLED    = booleanPreferencesKey("bg_vignette_enabled")
    val VIGNETTE_INTENSITY  = floatPreferencesKey("bg_vignette_intensity")
    val CHROMATIC_ENABLED   = booleanPreferencesKey("bg_chromatic_enabled")
    val CHROMATIC_INTENSITY = floatPreferencesKey("bg_chromatic_intensity")
    val SCANLINES_ENABLED   = booleanPreferencesKey("bg_scanlines_enabled")
    val SCANLINES_INTENSITY = floatPreferencesKey("bg_scanlines_intensity")
    val KALEIDOSCOPE_ENABLED   = booleanPreferencesKey("bg_kaleidoscope_enabled")
    val KALEIDOSCOPE_INTENSITY = floatPreferencesKey("bg_kaleidoscope_intensity")
    val WAVES_ENABLED          = booleanPreferencesKey("bg_waves_enabled")
    val WAVES_INTENSITY        = floatPreferencesKey("bg_waves_intensity")
    val SPEED               = floatPreferencesKey("bg_speed")
    val PARTICLE_SIZE       = floatPreferencesKey("bg_particle_size")
}

class BackgroundPreferences(private val context: Context) {

    fun getConfigFlow(currentTheme: AppTheme): Flow<BackgroundConfig> =
        context.bgDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { prefs ->
                val userCustomized = prefs[BgKeys.USER_CUSTOMIZED] ?: false
                val themeDefault   = defaultBackgroundConfigFor(currentTheme)

                if (!userCustomized) return@map themeDefault

                BackgroundConfig(
                    particles = EffectConfig(
                        enabled   = prefs[BgKeys.PARTICLES_ENABLED]   ?: themeDefault.particles.enabled,
                        intensity = prefs[BgKeys.PARTICLES_INTENSITY] ?: themeDefault.particles.intensity
                    ),
                    activeTypes = prefs[BgKeys.ACTIVE_TYPES]?.split(",")?.mapNotNull { name ->
                        try { SpecialParticleType.valueOf(name) } catch (e: Exception) { null }
                    } ?: themeDefault.activeTypes,
                    stars = EffectConfig(
                        enabled   = prefs[BgKeys.STARS_ENABLED]       ?: themeDefault.stars.enabled,
                        intensity = prefs[BgKeys.STARS_INTENSITY]     ?: themeDefault.stars.intensity
                    ),
                    grain = EffectConfig(
                        enabled   = prefs[BgKeys.GRAIN_ENABLED]       ?: themeDefault.grain.enabled,
                        intensity = prefs[BgKeys.GRAIN_INTENSITY]     ?: themeDefault.grain.intensity
                    ),
                    glow = EffectConfig(
                        enabled   = prefs[BgKeys.GLOW_ENABLED]        ?: themeDefault.glow.enabled,
                        intensity = prefs[BgKeys.GLOW_INTENSITY]      ?: themeDefault.glow.intensity
                    ),
                    vignette = EffectConfig(
                        enabled   = prefs[BgKeys.VIGNETTE_ENABLED]    ?: themeDefault.vignette.enabled,
                        intensity = prefs[BgKeys.VIGNETTE_INTENSITY]  ?: themeDefault.vignette.intensity
                    ),
                    chromatic = EffectConfig(
                        enabled   = prefs[BgKeys.CHROMATIC_ENABLED]   ?: themeDefault.chromatic.enabled,
                        intensity = prefs[BgKeys.CHROMATIC_INTENSITY] ?: themeDefault.chromatic.intensity
                    ),
                    scanlines = EffectConfig(
                        enabled   = prefs[BgKeys.SCANLINES_ENABLED]   ?: themeDefault.scanlines.enabled,
                        intensity = prefs[BgKeys.SCANLINES_INTENSITY] ?: themeDefault.scanlines.intensity
                    ),
                    kaleidoscope = EffectConfig(
                        enabled   = prefs[BgKeys.KALEIDOSCOPE_ENABLED]   ?: themeDefault.kaleidoscope.enabled,
                        intensity = prefs[BgKeys.KALEIDOSCOPE_INTENSITY] ?: themeDefault.kaleidoscope.intensity
                    ),
                    waves = EffectConfig(
                        enabled   = prefs[BgKeys.WAVES_ENABLED]   ?: themeDefault.waves.enabled,
                        intensity = prefs[BgKeys.WAVES_INTENSITY] ?: themeDefault.waves.intensity
                    ),
                    speed = prefs[BgKeys.SPEED] ?: themeDefault.speed,
                    particleSize = prefs[BgKeys.PARTICLE_SIZE] ?: themeDefault.particleSize
                )
            }

    suspend fun saveConfig(config: BackgroundConfig) {
        context.bgDataStore.edit { prefs ->
            prefs[BgKeys.USER_CUSTOMIZED]     = true
            prefs[BgKeys.PARTICLES_ENABLED]   = config.particles.enabled
            prefs[BgKeys.PARTICLES_INTENSITY] = config.particles.intensity
            prefs[BgKeys.ACTIVE_TYPES]        = config.activeTypes.joinToString(",") { it.name }
            prefs[BgKeys.STARS_ENABLED]       = config.stars.enabled
            prefs[BgKeys.STARS_INTENSITY]     = config.stars.intensity
            prefs[BgKeys.GRAIN_ENABLED]       = config.grain.enabled
            prefs[BgKeys.GRAIN_INTENSITY]     = config.grain.intensity
            prefs[BgKeys.GLOW_ENABLED]        = config.glow.enabled
            prefs[BgKeys.GLOW_INTENSITY]      = config.glow.intensity
            prefs[BgKeys.VIGNETTE_ENABLED]    = config.vignette.enabled
            prefs[BgKeys.VIGNETTE_INTENSITY]  = config.vignette.intensity
            prefs[BgKeys.CHROMATIC_ENABLED]   = config.chromatic.enabled
            prefs[BgKeys.CHROMATIC_INTENSITY] = config.chromatic.intensity
            prefs[BgKeys.SCANLINES_ENABLED]   = config.scanlines.enabled
            prefs[BgKeys.SCANLINES_INTENSITY] = config.scanlines.intensity
            prefs[BgKeys.KALEIDOSCOPE_ENABLED]   = config.kaleidoscope.enabled
            prefs[BgKeys.KALEIDOSCOPE_INTENSITY] = config.kaleidoscope.intensity
            prefs[BgKeys.WAVES_ENABLED]          = config.waves.enabled
            prefs[BgKeys.WAVES_INTENSITY]        = config.waves.intensity
            prefs[BgKeys.SPEED]               = config.speed
            prefs[BgKeys.PARTICLE_SIZE]       = config.particleSize
        }
    }

    suspend fun resetToThemeDefault() {
        context.bgDataStore.edit { prefs ->
            prefs[BgKeys.USER_CUSTOMIZED] = false
        }
    }
}
