package com.alijafari.raise.di

import android.app.AlarmManager
import android.content.Context
import com.alijafari.raise.feature_alarm.data.AndroidScheduler
import com.alijafari.raise.feature_alarm.domain.AlarmScheduler
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context,
        logRepository: LogRepository,
        alarmManager : AlarmManager
    ): AlarmScheduler {
        return AndroidScheduler(context, logRepository , alarmManager)
    }
}
