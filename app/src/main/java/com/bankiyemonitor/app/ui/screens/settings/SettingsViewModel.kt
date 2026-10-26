package com.bankiyemonitor.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bankiyemonitor.app.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val summaryFrequency: String = "WEEKLY",
    val notificationsEnabled: Boolean = true,
    val securityAlertsEnabled: Boolean = true,
    val highRiskOnly: Boolean = false,
    val themeMode: String = "SYSTEM"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesManager.summaryFrequency,
                preferencesManager.notificationsEnabled,
                preferencesManager.securityAlertsEnabled,
                preferencesManager.highRiskOnly,
                preferencesManager.themeMode
            ) { frequency, notifications, securityAlerts, highRisk, theme ->
                SettingsUiState(
                    summaryFrequency = frequency,
                    notificationsEnabled = notifications,
                    securityAlertsEnabled = securityAlerts,
                    highRiskOnly = highRisk,
                    themeMode = theme
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSummaryFrequency(frequency: String) {
        viewModelScope.launch { preferencesManager.setSummaryFrequency(frequency) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotificationsEnabled(enabled) }
    }

    fun setSecurityAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setSecurityAlertsEnabled(enabled) }
    }

    fun setHighRiskOnly(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setHighRiskOnly(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }
}
