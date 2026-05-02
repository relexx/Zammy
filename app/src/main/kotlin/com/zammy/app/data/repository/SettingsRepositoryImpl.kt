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
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
        private const val KEY_TRUST_ALL_CERTS = "trust_all_certs"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val DEFAULT_INTERVAL = 30
    }

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

    override fun isLoggedIn(): Boolean {
        return getServerUrl().isNotBlank() &&
                getUsername().isNotBlank() &&
                getPassword().isNotBlank()
    }

    override fun clearCredentials() {
        encryptedPrefs.edit()
            .remove(KEY_SERVER_URL)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .apply()
    }

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
}
