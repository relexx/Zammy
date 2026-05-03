package com.zammy.app.util

import com.zammy.app.domain.repository.SettingsRepository
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BaseUrlInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val serverUrl = settingsRepository.getServerUrl()
        if (serverUrl.isBlank()) return chain.proceed(chain.request())

        val newBase = serverUrl.trimEnd('/').toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())

        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(newBase.scheme)
            .host(newBase.host)
            .port(newBase.port)
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
