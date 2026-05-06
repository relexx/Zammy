package com.zammy.app.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.domain.repository.UserRepository
import com.zammy.app.util.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val step: Int = 1
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        if (settingsRepository.isLoggedIn()) {
            _uiState.update { it.copy(isLoggedIn = true) }
        }
        _uiState.update {
            it.copy(
                serverUrl = settingsRepository.getServerUrl(),
                username  = settingsRepository.getUsername()
            )
        }
    }

    fun onServerUrlChange(url: String) {
        _uiState.update { it.copy(serverUrl = url, error = null) }
    }

    fun onUsernameChange(username: String) {
        _uiState.update { it.copy(username = username, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun nextStep() {
        if (_uiState.value.serverUrl.isBlank()) {
            _uiState.update { it.copy(error = "Bitte Server-URL eingeben") }
            return
        }
        _uiState.update { it.copy(step = 2, error = null) }
    }

    fun prevStep() {
        _uiState.update { it.copy(step = 1, error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Bitte alle Felder ausfüllen") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            settingsRepository.setServerUrl(state.serverUrl)
            settingsRepository.setUsername(state.username)
            settingsRepository.setPassword(state.password)

            val result = userRepository.getCurrentUser()
            result.fold(
                onSuccess = {
                    syncScheduler.schedule(
                        settingsRepository.getNotificationIntervalMinutes(),
                        ExistingPeriodicWorkPolicy.KEEP
                    )
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                },
                onFailure = { error ->
                    settingsRepository.clearCredentials()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Login fehlgeschlagen: ${error.message ?: "Unbekannter Fehler"}"
                        )
                    }
                }
            )
        }
    }
}
