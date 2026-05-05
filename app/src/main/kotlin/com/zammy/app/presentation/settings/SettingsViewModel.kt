package com.zammy.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.util.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "",
    val username: String = "",
    val notificationIntervalMinutes: Int = 30,
    val trustAllCerts: Boolean = false,
    val savedMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                serverUrl = settingsRepository.getServerUrl(),
                username = settingsRepository.getUsername(),
                notificationIntervalMinutes = settingsRepository.getNotificationIntervalMinutes(),
                trustAllCerts = settingsRepository.isTrustAllCerts()
            )
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
    }

    fun onNotificationIntervalChange(minutes: Int) {
        _uiState.update { it.copy(notificationIntervalMinutes = minutes) }
    }

    fun onTrustAllCertsChange(trust: Boolean) {
        _uiState.update { it.copy(trustAllCerts = trust) }
    }

    fun saveSettings() {
        val state = _uiState.value
        settingsRepository.setServerUrl(state.serverUrl)
        settingsRepository.setNotificationIntervalMinutes(state.notificationIntervalMinutes)
        settingsRepository.setTrustAllCerts(state.trustAllCerts)
        syncScheduler.schedule(state.notificationIntervalMinutes, ExistingPeriodicWorkPolicy.UPDATE)
        _uiState.update { it.copy(savedMessage = "Settings saved") }
    }

    fun logout() {
        syncScheduler.cancel()
        settingsRepository.clearCredentials()
    }

    fun clearMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }
}
