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


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themePrefs = ThemePreferences(application)

    private val _selectedTheme = MutableStateFlow(AppTheme.JJK_DARK)
    val selectedTheme: StateFlow<AppTheme> = _selectedTheme.asStateFlow()


    private val _themeReady = MutableStateFlow(false)
    val themeReady: StateFlow<Boolean> = _themeReady.asStateFlow()

    init {
        viewModelScope.launch {
            themePrefs.getThemeFlow().collect { savedTheme ->
                _selectedTheme.value = savedTheme
                AppColors.applyTheme(savedTheme)
                _themeReady.value = true   // primer valor recibido, ya es seguro dibujar
            }
        }
    }

    fun selectTheme(theme: AppTheme) {
        viewModelScope.launch {
            _selectedTheme.value = theme
            AppColors.applyTheme(theme)
            themePrefs.saveTheme(theme)
        }
    }
}