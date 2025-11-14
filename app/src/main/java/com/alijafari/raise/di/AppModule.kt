package com.alijafari.raise.di

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import com.alijafari.raise.feature_ringtone.data.infrastructure.RingtonePreviewPlayerImpl
import com.alijafari.raise.feature_ringtone.data.infrastructure.SystemVolumeManagerImpl
import com.alijafari.raise.feature_ringtone.domain.infrastructure.RingtonePreviewPlayer
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
    fun provideRingtonePreviewPlayerImpl(@ApplicationContext context: Context) : RingtonePreviewPlayer = RingtonePreviewPlayerImpl(context)

    @Provides
    @Singleton
    fun provideSystemAudioManager(audioManager: AudioManager) : SystemVolumeManager = SystemVolumeManagerImpl(audioManager)

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context) : AudioManager = context.getSystemService(AudioManager::class.java)
}