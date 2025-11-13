package com.alijafari.raise.di

import android.content.Context
import android.media.RingtoneManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRingtoneManager(@ApplicationContext context: Context) : RingtoneManager = RingtoneManager(context)
}