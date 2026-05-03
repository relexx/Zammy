package com.zammy.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.workers.TicketSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit
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
    @ApplicationContext private val context: Context
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
        rescheduleSync(state.notificationIntervalMinutes)
        _uiState.update { it.copy(savedMessage = "Settings saved") }
    }

    fun logout() {
        WorkManager.getInstance(context).cancelUniqueWork(TicketSyncWorker.WORK_NAME)
        settingsRepository.clearCredentials()
    }

    fun clearMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }

    private fun rescheduleSync(intervalMinutes: Int) {
        val request = PeriodicWorkRequestBuilder<TicketSyncWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TicketSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
