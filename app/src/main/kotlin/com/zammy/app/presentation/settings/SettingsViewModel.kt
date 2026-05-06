package com.zammy.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import com.zammy.app.domain.model.DisplaySettings
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.ui.theme.ThemeMode
import com.zammy.app.util.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SettingsUiState(
    // Account / Server
    val serverUrl: String = "",
    val username: String = "",
    val notificationIntervalMinutes: Int = 30,
    val trustAllCerts: Boolean = false,
    val savedMessage: String? = null,
    // Display
    val display: DisplaySettings = DisplaySettings(),
    // Expanded section
    val expandedSection: String? = null,
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
                serverUrl                 = settingsRepository.getServerUrl(),
                username                  = settingsRepository.getUsername(),
                notificationIntervalMinutes = settingsRepository.getNotificationIntervalMinutes(),
                trustAllCerts             = settingsRepository.isTrustAllCerts(),
                display = DisplaySettings(
                    themeMode             = settingsRepository.getThemeMode(),
                    accentColorHex        = settingsRepository.getAccentColorHex(),
                    listLayout            = settingsRepository.getListLayout(),
                    sortBy                = settingsRepository.getSortBy(),
                    density               = settingsRepository.getDensity(),
                    showAvatars           = settingsRepository.getShowAvatars(),
                    showTags              = settingsRepository.getShowTags(),
                    showPriority          = settingsRepository.getShowPriority(),
                    showTicketId          = settingsRepository.getShowTicketId(),
                    boldUnread            = settingsRepository.getBoldUnread(),
                    highlightEscalated    = settingsRepository.getHighlightEscalated(),
                    bubbleStyle           = settingsRepository.getBubbleStyle(),
                    showTimestamps        = settingsRepository.getShowTimestamps(),
                    showCustomerAvatar    = settingsRepository.getShowCustomerAvatar(),
                    showInternalBadge     = settingsRepository.getShowInternalBadge(),
                    language              = settingsRepository.getLanguage(),
                )
            )
        }
    }

    // ── Account / Server ─────────────────────────────────────────────────────

    fun onServerUrlChange(url: String) = _uiState.update { it.copy(serverUrl = url) }

    fun onNotificationIntervalChange(minutes: Int) =
        _uiState.update { it.copy(notificationIntervalMinutes = minutes) }

    fun onTrustAllCertsChange(trust: Boolean) =
        _uiState.update { it.copy(trustAllCerts = trust) }

    fun saveServerSettings() {
        val state = _uiState.value
        settingsRepository.setServerUrl(state.serverUrl)
        settingsRepository.setNotificationIntervalMinutes(state.notificationIntervalMinutes)
        settingsRepository.setTrustAllCerts(state.trustAllCerts)
        syncScheduler.schedule(state.notificationIntervalMinutes, ExistingPeriodicWorkPolicy.UPDATE)
        _uiState.update { it.copy(savedMessage = "Saved") }
    }

    fun logout() {
        syncScheduler.cancel()
        settingsRepository.clearCredentials()
    }

    fun clearMessage() = _uiState.update { it.copy(savedMessage = null) }

    // ── Section expand/collapse ───────────────────────────────────────────────

    fun toggleSection(key: String) {
        _uiState.update {
            it.copy(expandedSection = if (it.expandedSection == key) null else key)
        }
    }

    // ── Display settings (live-persist) ──────────────────────────────────────

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.setThemeMode(mode.name)
        updateDisplay { it.copy(themeMode = mode.name) }
    }

    fun resolveThemeMode(): ThemeMode = when (uiState.value.display.themeMode) {
        "LIGHT" -> ThemeMode.LIGHT
        "DARK"  -> ThemeMode.DARK
        else    -> ThemeMode.SYSTEM
    }

    fun setAccentColor(hex: String) {
        settingsRepository.setAccentColorHex(hex)
        updateDisplay { it.copy(accentColorHex = hex) }
    }

    fun setListLayout(layout: String) {
        settingsRepository.setListLayout(layout)
        updateDisplay { it.copy(listLayout = layout) }
    }

    fun setSortBy(sort: String) {
        settingsRepository.setSortBy(sort)
        updateDisplay { it.copy(sortBy = sort) }
    }

    fun setDensity(density: String) {
        settingsRepository.setDensity(density)
        updateDisplay { it.copy(density = density) }
    }

    fun setShowAvatars(v: Boolean) { settingsRepository.setShowAvatars(v); updateDisplay { it.copy(showAvatars = v) } }
    fun setShowTags(v: Boolean) { settingsRepository.setShowTags(v); updateDisplay { it.copy(showTags = v) } }
    fun setShowPriority(v: Boolean) { settingsRepository.setShowPriority(v); updateDisplay { it.copy(showPriority = v) } }
    fun setShowTicketId(v: Boolean) { settingsRepository.setShowTicketId(v); updateDisplay { it.copy(showTicketId = v) } }
    fun setBoldUnread(v: Boolean) { settingsRepository.setBoldUnread(v); updateDisplay { it.copy(boldUnread = v) } }
    fun setHighlightEscalated(v: Boolean) { settingsRepository.setHighlightEscalated(v); updateDisplay { it.copy(highlightEscalated = v) } }

    fun setBubbleStyle(style: String) {
        settingsRepository.setBubbleStyle(style)
        updateDisplay { it.copy(bubbleStyle = style) }
    }

    fun setShowTimestamps(v: Boolean) { settingsRepository.setShowTimestamps(v); updateDisplay { it.copy(showTimestamps = v) } }
    fun setShowCustomerAvatar(v: Boolean) { settingsRepository.setShowCustomerAvatar(v); updateDisplay { it.copy(showCustomerAvatar = v) } }
    fun setShowInternalBadge(v: Boolean) { settingsRepository.setShowInternalBadge(v); updateDisplay { it.copy(showInternalBadge = v) } }

    fun setLanguage(lang: String) {
        settingsRepository.setLanguage(lang)
        updateDisplay { it.copy(language = lang) }
    }

    private fun updateDisplay(transform: (DisplaySettings) -> DisplaySettings) {
        _uiState.update { it.copy(display = transform(it.display)) }
    }
}
