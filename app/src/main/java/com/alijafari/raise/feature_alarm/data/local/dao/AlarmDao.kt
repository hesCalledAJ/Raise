package com.alijafari.raise.feature_alarm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.alijafari.raise.feature_alarm.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms")
    fun getAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM ALARMS WHERE id IN (:alarmIds)")
    fun loadAllByIds(alarmIds: IntArray): List<AlarmEntity>

    @Query("SELECT * FROM ALARMS WHERE id IN (:id)")
    fun getAlarmById(id : Int): AlarmEntity

    @Insert
    fun insertAll(vararg alarms: AlarmEntity)

    @Upsert
    fun upsertAll(vararg alarms: AlarmEntity)

    @Delete
    fun delete(alarm: AlarmEntity)
}