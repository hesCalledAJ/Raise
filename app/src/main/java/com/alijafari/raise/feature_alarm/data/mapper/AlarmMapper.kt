package com.alijafari.raise.feature_alarm.data.mapper
import com.alijafari.raise.feature_alarm.data.local.entity.AlarmEntity
import com.alijafari.raise.feature_alarm.domain.model.Alarm

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    snoozeCount = snoozeCount,
    snoozeMinutes = snoozeMinutes,
    ringtoneData = ringtoneData,
    vibrate = vibrate,
    ringtoneVolume = ringtoneVolume
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    snoozeCount = snoozeCount,
    snoozeMinutes = snoozeMinutes,
    ringtoneData = ringtoneData,
    vibrate = vibrate,
    ringtoneVolume = ringtoneVolume
)