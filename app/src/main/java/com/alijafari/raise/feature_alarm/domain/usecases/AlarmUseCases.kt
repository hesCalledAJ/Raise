package com.alijafari.raise.feature_alarm.domain.usecases

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.alijafari.raise.feature_alarm.domain.AlarmScheduler
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow

data class AlarmUseCases(
    val getById: GetAlarmByIDUseCase,
    val getAll: GetAllAlarmsUseCase,
    val upsert: UpsertAlarmUseCase,
    val delete: DeleteAlarmUseCase,
    val schedule: ScheduleAlarmUseCase,
    val snooze: SnoozeAlarmUseCase,
    val cancelSnooze: CancelSnoozeAlarmUseCase,
    val cancel: CancelAlarmUseCase
)

class GetAlarmByIDUseCase(private val repo: AlarmRepository) {
    suspend operator fun invoke(id : Int): Alarm = repo.getAlarmByID(id)
}

class GetAllAlarmsUseCase(private val repo: AlarmRepository) {
    operator fun invoke(): Flow<List<Alarm>> = repo.getAllAlarms()
}

class UpsertAlarmUseCase(private val repo: AlarmRepository) {
    suspend operator fun invoke(alarm: Alarm) = repo.upsertAlarm(alarm)
}

class DeleteAlarmUseCase(private val repo: AlarmRepository) {
    suspend operator fun invoke(alarm: Alarm) = repo.deleteAlarm(alarm)
}

class ScheduleAlarmUseCase(private val scheduler: AlarmScheduler) {
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    operator fun invoke(alarm: Alarm) = scheduler.schedule(alarm)
}

class CancelAlarmUseCase(private val scheduler: AlarmScheduler) {
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    operator fun invoke(alarm: Alarm) = scheduler.cancel(alarm)
}

class SnoozeAlarmUseCase(private val scheduler: AlarmScheduler) {
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    operator fun invoke(alarm: Alarm) = scheduler.snooze(alarm)
}

class CancelSnoozeAlarmUseCase(private val scheduler: AlarmScheduler) {
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    operator fun invoke(alarm: Alarm) = scheduler.cancelSnooze(alarm)
}

