package com.zammy.app.data.repository

import android.content.SharedPreferences
import com.zammy.app.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @Named("encrypted_prefs") private val encryptedPrefs: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val KEY_SERVER_URL           = "server_url"
        private const val KEY_USERNAME             = "username"
        private const val KEY_PASSWORD             = "password"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
        private const val KEY_TRUST_ALL_CERTS      = "trust_all_certs"
        private const val KEY_LAST_SYNC            = "last_sync"

        private const val KEY_THEME_MODE           = "theme_mode"
        private const val KEY_ACCENT_COLOR         = "accent_color"
        private const val KEY_LIST_LAYOUT          = "list_layout"
        private const val KEY_SORT_BY              = "sort_by"
        private const val KEY_DENSITY              = "density"
        private const val KEY_SHOW_AVATARS         = "show_avatars"
        private const val KEY_SHOW_TAGS            = "show_tags"
        private const val KEY_SHOW_PRIORITY        = "show_priority"
        private const val KEY_SHOW_TICKET_ID       = "show_ticket_id"
        private const val KEY_BOLD_UNREAD          = "bold_unread"
        private const val KEY_HIGHLIGHT_ESCALATED  = "highlight_escalated"
        private const val KEY_BUBBLE_STYLE         = "bubble_style"
        private const val KEY_SHOW_TIMESTAMPS      = "show_timestamps"
        private const val KEY_SHOW_CUSTOMER_AVATAR = "show_customer_avatar"
        private const val KEY_SHOW_INTERNAL_BADGE  = "show_internal_badge"
        private const val KEY_LANGUAGE             = "language"

        private const val DEFAULT_INTERVAL = 30
    }

    // ── Credentials / Server ─────────────────────────────────────────────────

    override fun getServerUrl(): String =
        encryptedPrefs.getString(KEY_SERVER_URL, "") ?: ""

    override fun setServerUrl(url: String) {
        encryptedPrefs.edit().putString(KEY_SERVER_URL, url.trimEnd('/')).apply()
    }

    override fun getUsername(): String =
        encryptedPrefs.getString(KEY_USERNAME, "") ?: ""

    override fun setUsername(username: String) {
        encryptedPrefs.edit().putString(KEY_USERNAME, username).apply()
    }

    override fun getPassword(): String =
        encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""

    override fun setPassword(password: String) {
        encryptedPrefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    override fun isLoggedIn(): Boolean =
        getServerUrl().isNotBlank() && getUsername().isNotBlank() && getPassword().isNotBlank()

    override fun clearCredentials() {
        encryptedPrefs.edit()
            .remove(KEY_SERVER_URL)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

    // ── Sync ─────────────────────────────────────────────────────────────────

    override fun getNotificationIntervalMinutes(): Int =
        encryptedPrefs.getInt(KEY_NOTIFICATION_INTERVAL, DEFAULT_INTERVAL)

    override fun setNotificationIntervalMinutes(minutes: Int) {
        encryptedPrefs.edit().putInt(KEY_NOTIFICATION_INTERVAL, minutes).apply()
    }

    override fun isTrustAllCerts(): Boolean =
        encryptedPrefs.getBoolean(KEY_TRUST_ALL_CERTS, false)

    override fun setTrustAllCerts(trust: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_TRUST_ALL_CERTS, trust).apply()
    }

    override fun getLastSyncTimestamp(): Long =
        encryptedPrefs.getLong(KEY_LAST_SYNC, 0L)

    override fun setLastSyncTimestamp(timestamp: Long) {
        encryptedPrefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    // ── Display settings ─────────────────────────────────────────────────────

    override fun getThemeMode(): String =
        encryptedPrefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"

    override fun setThemeMode(mode: String) {
        encryptedPrefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    override fun getAccentColorHex(): String =
        encryptedPrefs.getString(KEY_ACCENT_COLOR, "#4F8EF7") ?: "#4F8EF7"

    override fun setAccentColorHex(hex: String) {
        encryptedPrefs.edit().putString(KEY_ACCENT_COLOR, hex).apply()
    }

    override fun getListLayout(): String =
        encryptedPrefs.getString(KEY_LIST_LAYOUT, "cards") ?: "cards"

    override fun setListLayout(layout: String) {
        encryptedPrefs.edit().putString(KEY_LIST_LAYOUT, layout).apply()
    }

    override fun getSortBy(): String =
        encryptedPrefs.getString(KEY_SORT_BY, "updated") ?: "updated"

    override fun setSortBy(sort: String) {
        encryptedPrefs.edit().putString(KEY_SORT_BY, sort).apply()
    }

    override fun getDensity(): String =
        encryptedPrefs.getString(KEY_DENSITY, "regular") ?: "regular"

    override fun setDensity(density: String) {
        encryptedPrefs.edit().putString(KEY_DENSITY, density).apply()
    }

    override fun getShowAvatars(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_AVATARS, true)

    override fun setShowAvatars(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_AVATARS, show).apply()
    }

    override fun getShowTags(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_TAGS, true)

    override fun setShowTags(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_TAGS, show).apply()
    }

    override fun getShowPriority(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_PRIORITY, true)

    override fun setShowPriority(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_PRIORITY, show).apply()
    }

    override fun getShowTicketId(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_TICKET_ID, true)

    override fun setShowTicketId(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_TICKET_ID, show).apply()
    }

    override fun getBoldUnread(): Boolean =
        encryptedPrefs.getBoolean(KEY_BOLD_UNREAD, true)

    override fun setBoldUnread(bold: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BOLD_UNREAD, bold).apply()
    }

    override fun getHighlightEscalated(): Boolean =
        encryptedPrefs.getBoolean(KEY_HIGHLIGHT_ESCALATED, true)

    override fun setHighlightEscalated(highlight: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_HIGHLIGHT_ESCALATED, highlight).apply()
    }

    override fun getBubbleStyle(): String =
        encryptedPrefs.getString(KEY_BUBBLE_STYLE, "chat") ?: "chat"

    override fun setBubbleStyle(style: String) {
        encryptedPrefs.edit().putString(KEY_BUBBLE_STYLE, style).apply()
    }

    override fun getShowTimestamps(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_TIMESTAMPS, true)

    override fun setShowTimestamps(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_TIMESTAMPS, show).apply()
    }

    override fun getShowCustomerAvatar(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_CUSTOMER_AVATAR, true)

    override fun setShowCustomerAvatar(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_CUSTOMER_AVATAR, show).apply()
    }

    override fun getShowInternalBadge(): Boolean =
        encryptedPrefs.getBoolean(KEY_SHOW_INTERNAL_BADGE, true)

    override fun setShowInternalBadge(show: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_SHOW_INTERNAL_BADGE, show).apply()
    }

    override fun getLanguage(): String =
        encryptedPrefs.getString(KEY_LANGUAGE, "system") ?: "system"

    override fun setLanguage(lang: String) {
        encryptedPrefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }
}
