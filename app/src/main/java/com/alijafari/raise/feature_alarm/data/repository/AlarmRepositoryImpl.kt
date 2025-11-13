package com.alijafari.raise.feature_alarm.data.repository

import com.alijafari.raise.feature_alarm.data.local.dao.AlarmDao
import com.alijafari.raise.feature_alarm.data.mapper.toDomain
import com.alijafari.raise.feature_alarm.data.mapper.toEntity
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlarmRepositoryImpl(private val dao: AlarmDao) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return dao.getAll().map { alarms ->
            alarms.map { it.toDomain() }
        }
    }

    override suspend fun getAlarmByID(id: Int) = dao.getAlarmById(id).toDomain()


    override suspend fun insertAlarm(alarm: Alarm) {
        dao.insertAll(alarm.toEntity())
    }

    override suspend fun upsertAlarm(alarm: Alarm) {
        dao.upsertAll(alarm.toEntity())
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        dao.delete(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
    }
}