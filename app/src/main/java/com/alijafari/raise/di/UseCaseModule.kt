package com.alijafari.raise.di

import com.alijafari.raise.feature_alarm.domain.AlarmScheduler
import com.alijafari.raise.feature_alarm.domain.repository.AlarmRepository
import com.alijafari.raise.feature_alarm.domain.usecases.AlarmUseCases
import com.alijafari.raise.feature_alarm.domain.usecases.CancelAlarmUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.CancelSnoozeAlarmUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.DeleteAlarmUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.GetAlarmByIDUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.GetAllAlarmsUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.ScheduleAlarmUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.SnoozeAlarmUseCase
import com.alijafari.raise.feature_alarm.domain.usecases.UpsertAlarmUseCase
import com.alijafari.raise.feature_ringtone.domain.repository.RingtoneRepository
import com.alijafari.raise.feature_ringtone.domain.usecases.GetDeviceDefaultRingtoneUseCase
import com.alijafari.raise.feature_ringtone.domain.usecases.GetDeviceRingtonesUseCase
import com.alijafari.raise.feature_ringtone.domain.usecases.RingtoneUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideRingtoneUseCases(repo : RingtoneRepository): RingtoneUseCases {
        return RingtoneUseCases(
            getDeviceRingtones = GetDeviceRingtonesUseCase(repo),
            getDeviceDefaultRingtone = GetDeviceDefaultRingtoneUseCase(repo)
        )
    }

    @Provides
    @Singleton
    fun provideAlarmUseCases(repo: AlarmRepository,scheduler: AlarmScheduler): AlarmUseCases {
        return AlarmUseCases(
            getAll = GetAllAlarmsUseCase(repo),
            upsert = UpsertAlarmUseCase(repo),
            delete = DeleteAlarmUseCase(repo),
            schedule = ScheduleAlarmUseCase(scheduler),
            cancel = CancelAlarmUseCase(scheduler),
            snooze = SnoozeAlarmUseCase(scheduler),
            cancelSnooze = CancelSnoozeAlarmUseCase(scheduler),
            getById = GetAlarmByIDUseCase(repo)
        )
    }
}
