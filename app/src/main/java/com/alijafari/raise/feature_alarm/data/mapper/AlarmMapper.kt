package com.alijafari.raise.feature_alarm.data.mapper
import com.alijafari.raise.feature_alarm.data.local.entity.AlarmEntity
import com.alijafari.raise.feature_alarm.domain.model.Alarm

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    label = label,
    minute = minute,
    vibrate = vibrate,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    snoozeCount = snoozeCount,
    ringtoneData = ringtoneData,
    timeBombData = timeBombData,
    snoozeMinutes = snoozeMinutes,
    ringtoneVolume = ringtoneVolume,
    smartOffsetData = offsetData,
    challengesList = challengesList
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    label = label,
    minute = minute,
    vibrate = vibrate,
    isEnabled = isEnabled,
    repeatDays = repeatDays,
    snoozeCount = snoozeCount,
    ringtoneData = ringtoneData,
    timeBombData = timeBombData,
    offsetData = smartOffsetData,
    snoozeMinutes = snoozeMinutes,
    challengesList = challengesList,
    ringtoneVolume = ringtoneVolume

)