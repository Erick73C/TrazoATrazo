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
    val PETALS_ENABLED      = booleanPreferencesKey("bg_petals_enabled")
    val PETALS_INTENSITY    = floatPreferencesKey("bg_petals_intensity")
    val GRAIN_ENABLED       = booleanPreferencesKey("bg_grain_enabled")
    val GRAIN_INTENSITY     = floatPreferencesKey("bg_grain_intensity")
    val GLOW_ENABLED        = booleanPreferencesKey("bg_glow_enabled")
    val GLOW_INTENSITY      = floatPreferencesKey("bg_glow_intensity")
    val SPEED               = floatPreferencesKey("bg_speed")
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
                    petals = EffectConfig(
                        enabled   = prefs[BgKeys.PETALS_ENABLED]      ?: themeDefault.petals.enabled,
                        intensity = prefs[BgKeys.PETALS_INTENSITY]    ?: themeDefault.petals.intensity
                    ),
                    grain = EffectConfig(
                        enabled   = prefs[BgKeys.GRAIN_ENABLED]       ?: themeDefault.grain.enabled,
                        intensity = prefs[BgKeys.GRAIN_INTENSITY]     ?: themeDefault.grain.intensity
                    ),
                    glow = EffectConfig(
                        enabled   = prefs[BgKeys.GLOW_ENABLED]        ?: themeDefault.glow.enabled,
                        intensity = prefs[BgKeys.GLOW_INTENSITY]      ?: themeDefault.glow.intensity
                    ),
                    speed = prefs[BgKeys.SPEED] ?: themeDefault.speed
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
            prefs[BgKeys.PETALS_ENABLED]      = config.petals.enabled
            prefs[BgKeys.PETALS_INTENSITY]    = config.petals.intensity
            prefs[BgKeys.GRAIN_ENABLED]       = config.grain.enabled
            prefs[BgKeys.GRAIN_INTENSITY]     = config.grain.intensity
            prefs[BgKeys.GLOW_ENABLED]        = config.glow.enabled
            prefs[BgKeys.GLOW_INTENSITY]      = config.glow.intensity
            prefs[BgKeys.SPEED]               = config.speed
        }
    }

    suspend fun resetToThemeDefault() {
        context.bgDataStore.edit { prefs ->
            prefs[BgKeys.USER_CUSTOMIZED] = false
        }
    }
}
