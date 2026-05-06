package com.zammy.app.domain.repository

interface SettingsRepository {
    // ── Credentials / Server ─────────────────────────────────────────────────
    fun getServerUrl(): String
    fun setServerUrl(url: String)
    fun getUsername(): String
    fun setUsername(username: String)
    fun getPassword(): String
    fun setPassword(password: String)
    fun isLoggedIn(): Boolean
    fun clearCredentials()

    // ── Sync ─────────────────────────────────────────────────────────────────
    fun getNotificationIntervalMinutes(): Int
    fun setNotificationIntervalMinutes(minutes: Int)
    fun isTrustAllCerts(): Boolean
    fun setTrustAllCerts(trust: Boolean)
    fun getLastSyncTimestamp(): Long
    fun setLastSyncTimestamp(timestamp: Long)

    // ── Display settings ─────────────────────────────────────────────────────
    fun getThemeMode(): String          // LIGHT | DARK | SYSTEM
    fun setThemeMode(mode: String)
    fun getAccentColorHex(): String
    fun setAccentColorHex(hex: String)
    fun getListLayout(): String         // cards | rows | compact
    fun setListLayout(layout: String)
    fun getSortBy(): String             // updated | created | priority | id
    fun setSortBy(sort: String)
    fun getDensity(): String            // compact | regular | comfy
    fun setDensity(density: String)
    fun getShowAvatars(): Boolean
    fun setShowAvatars(show: Boolean)
    fun getShowTags(): Boolean
    fun setShowTags(show: Boolean)
    fun getShowPriority(): Boolean
    fun setShowPriority(show: Boolean)
    fun getShowTicketId(): Boolean
    fun setShowTicketId(show: Boolean)
    fun getBoldUnread(): Boolean
    fun setBoldUnread(bold: Boolean)
    fun getHighlightEscalated(): Boolean
    fun setHighlightEscalated(highlight: Boolean)
    fun getBubbleStyle(): String        // rounded | chat | square
    fun setBubbleStyle(style: String)
    fun getShowTimestamps(): Boolean
    fun setShowTimestamps(show: Boolean)
    fun getShowCustomerAvatar(): Boolean
    fun setShowCustomerAvatar(show: Boolean)
    fun getShowInternalBadge(): Boolean
    fun setShowInternalBadge(show: Boolean)
    fun getLanguage(): String           // system | en | de
    fun setLanguage(lang: String)
}
