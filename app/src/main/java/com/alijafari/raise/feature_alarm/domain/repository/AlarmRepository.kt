package com.alijafari.raise.feature_alarm.domain.repository

import com.alijafari.raise.feature_alarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms() : Flow<List<Alarm>>
    suspend fun getAlarmByID(id : Int) : Alarm
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun upsertAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
}