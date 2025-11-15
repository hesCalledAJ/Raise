package com.alijafari.raise.di

import android.app.AlarmManager
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import com.alijafari.raise.feature_ringtone.data.infrastructure.RingtonePlayerImpl
import com.alijafari.raise.feature_ringtone.data.infrastructure.SystemVolumeManagerImpl
import com.alijafari.raise.feature_ringtone.domain.infrastructure.RingtonePlayer
import com.alijafari.raise.feature_ringtone.domain.infrastructure.SystemVolumeManager
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

    @Provides
    @Singleton
    fun provideRingtonePreviewPlayerImpl(@ApplicationContext context: Context) : RingtonePlayer = RingtonePlayerImpl(context)

    @Provides
    @Singleton
    fun provideSystemAudioManager(audioManager: AudioManager) : SystemVolumeManager = SystemVolumeManagerImpl(audioManager)

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context) : AudioManager = context.getSystemService(AudioManager::class.java)


    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context) : AlarmManager = context.getSystemService(AlarmManager::class.java)
}