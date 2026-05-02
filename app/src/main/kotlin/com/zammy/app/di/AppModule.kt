package com.zammy.app.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zammy.app.data.api.ZammadApiService
import com.zammy.app.data.local.ZammyDatabase
import com.zammy.app.data.local.dao.TicketDao
import com.zammy.app.data.repository.SettingsRepositoryImpl
import com.zammy.app.data.repository.TicketRepositoryImpl
import com.zammy.app.data.repository.UserRepositoryImpl
import com.zammy.app.domain.repository.SettingsRepository
import com.zammy.app.domain.repository.TicketRepository
import com.zammy.app.domain.repository.UserRepository
import com.zammy.app.util.AuthInterceptor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("encrypted_prefs")
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "zammy_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @Named("encrypted_prefs") prefs: SharedPreferences
    ): SettingsRepository = SettingsRepositoryImpl(prefs)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        settingsRepository: SettingsRepository
    ): Retrofit {
        val baseUrl = settingsRepository.getServerUrl().let {
            if (it.isBlank()) "https://placeholder.example.com" else it
        }.trimEnd('/') + "/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideZammadApiService(retrofit: Retrofit): ZammadApiService =
        retrofit.create(ZammadApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ZammyDatabase =
        Room.databaseBuilder(context, ZammyDatabase::class.java, ZammyDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTicketDao(database: ZammyDatabase): TicketDao = database.ticketDao()

    @Provides
    @Singleton
    fun provideTicketRepository(
        api: ZammadApiService,
        ticketDao: TicketDao
    ): TicketRepository = TicketRepositoryImpl(api, ticketDao)

    @Provides
    @Singleton
    fun provideUserRepository(api: ZammadApiService): UserRepository =
        UserRepositoryImpl(api)
}
