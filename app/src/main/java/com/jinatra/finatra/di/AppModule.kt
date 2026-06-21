package com.jinatra.finatra.di

import android.content.Context
import androidx.room.Room
import com.jinatra.finatra.data.local.FinatraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing application-wide singletons: the Room database, application
 * [Context], the shared OkHttp client, and the AI endpoint configuration.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Builds the Room database "finatra.db" with all registered schema migrations applied. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinatraDatabase =
        Room.databaseBuilder(context, FinatraDatabase::class.java, "finatra.db")
            .addMigrations(*com.jinatra.finatra.data.local.ALL_MIGRATIONS)
            .build()

    /** Exposes the application [Context] for injection where a context is needed. */
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    /** Shared OkHttp client used for AI calls, with connect/read timeouts tuned for model latency. */
    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    /** Provides the AI endpoint configuration. */
    @Provides
    @Singleton
    fun provideAiEndpoints(): com.jinatra.finatra.data.ai.AiEndpoints =
        com.jinatra.finatra.data.ai.AiEndpoints()
}
