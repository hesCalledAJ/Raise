package com.alijafari.raise.di

import android.content.Context
import android.media.RingtoneManager
import com.alijafari.raise.feature_alarm.data.local.dao.AlarmDao
import com.alijafari.raise.feature_alarm.data.repository.AlarmRepositoryImpl
import com.alijafari.raise.feature_alarm.domain.repository.AlarmRepository
import com.alijafari.raise.feature_logs.data.repository.LogRepositoryImpl
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import com.alijafari.raise.feature_ringtone.data.repository.RingtoneRepositoryImpl
import com.alijafari.raise.feature_ringtone.domain.repository.RingtoneRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAlarmRepository(dao: AlarmDao): AlarmRepository = AlarmRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideLogRepository(): LogRepository = LogRepositoryImpl()

    @Provides
    @Singleton
    fun provideRingtoneRepository(
        @ApplicationContext context: Context,
        ringtoneManager: RingtoneManager
    ): RingtoneRepository = RingtoneRepositoryImpl(context, ringtoneManager)
}
