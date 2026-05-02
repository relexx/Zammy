package com.zammy.app.domain.repository

interface SettingsRepository {
    fun getServerUrl(): String
    fun setServerUrl(url: String)
    fun getUsername(): String
    fun setUsername(username: String)
    fun getPassword(): String
    fun setPassword(password: String)
    fun isLoggedIn(): Boolean
    fun clearCredentials()
    fun getNotificationIntervalMinutes(): Int
    fun setNotificationIntervalMinutes(minutes: Int)
    fun isTrustAllCerts(): Boolean
    fun setTrustAllCerts(trust: Boolean)
    fun getLastSyncTimestamp(): Long
    fun setLastSyncTimestamp(timestamp: Long)
}
