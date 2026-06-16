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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinatraDatabase =
        Room.databaseBuilder(context, FinatraDatabase::class.java, "finatra.db")
            .addMigrations(*com.jinatra.finatra.data.local.ALL_MIGRATIONS)
            .build()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAiEndpoints(): com.jinatra.finatra.data.ai.AiEndpoints =
        com.jinatra.finatra.data.ai.AiEndpoints()
}
