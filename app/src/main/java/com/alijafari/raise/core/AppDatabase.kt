package com.alijafari.raise.core

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alijafari.raise.feature_alarm.data.local.converter.Converters
import com.alijafari.raise.feature_alarm.data.local.dao.AlarmDao
import com.alijafari.raise.feature_alarm.data.local.entity.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(1,2)]
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}