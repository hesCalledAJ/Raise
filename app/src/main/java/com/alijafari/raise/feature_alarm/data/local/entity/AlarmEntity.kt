package com.alijafari.raise.feature_alarm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alijafari.raise.feature_alarm.domain.model.OffsetData
import com.alijafari.raise.feature_alarm.domain.model.TimeBombData
import com.alijafari.raise.feature_challenge.domain.model.ChallengeModel
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "hour")
    val hour: Int,

    @ColumnInfo(name = "minute")
    val minute: Int,

    @ColumnInfo(name = "label")
    val label: String,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean,

    @ColumnInfo(name = "repeat_days")
    val repeatDays: List<Int>,

    @ColumnInfo(name = "snooze_count")
    val snoozeCount : Int,

    @ColumnInfo(name = "snooze_minutes")
    val snoozeMinutes : Int,

    @ColumnInfo(name = "vibrate")
    val vibrate : Boolean,

    @ColumnInfo(name = "ringtoneData")
    val ringtoneData : RingtoneData?,

    @ColumnInfo(name = "ringtoneVolume", defaultValue = "0.6")
    val ringtoneVolume : Float,

    @ColumnInfo(name = "offsetData")
    val offsetData : OffsetData = OffsetData(),

    @ColumnInfo(name = "timeBombData")
    val timeBombData: TimeBombData = TimeBombData(),

    @ColumnInfo(name = "challenges", defaultValue = "")
    val challengesList: ArrayList<ChallengeModel> = arrayListOf<ChallengeModel>(),
)
