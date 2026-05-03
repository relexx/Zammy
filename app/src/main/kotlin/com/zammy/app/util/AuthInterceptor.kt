package com.zammy.app.util

import com.zammy.app.domain.repository.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Base64
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val username = settingsRepository.getUsername()
        val password = settingsRepository.getPassword()

        val request = if (username.isNotBlank() && password.isNotBlank()) {
            val credentials = "$username:$password"
            val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            chain.request().newBuilder()
                .header("Authorization", "Basic $encoded")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
