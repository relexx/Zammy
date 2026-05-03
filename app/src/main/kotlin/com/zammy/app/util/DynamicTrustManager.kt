package com.zammy.app.util

import com.zammy.app.domain.repository.SettingsRepository
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@Singleton
class DynamicTrustManager @Inject constructor(
    private val settingsRepository: SettingsRepository
) : X509TrustManager {

    private val systemTrustManager: X509TrustManager by lazy {
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(null as KeyStore?)
        factory.trustManagers.filterIsInstance<X509TrustManager>().first()
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        if (!settingsRepository.isTrustAllCerts()) {
            systemTrustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (!settingsRepository.isTrustAllCerts()) {
            systemTrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        if (settingsRepository.isTrustAllCerts()) arrayOf() else systemTrustManager.acceptedIssuers
}
