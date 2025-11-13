package com.alijafari.raise.di

import android.app.Application
import androidx.room.Room
import com.alijafari.raise.core.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "wakee.db").build()

    @Provides
    fun provideAlarmDao(db: AppDatabase) = db.alarmDao()
}