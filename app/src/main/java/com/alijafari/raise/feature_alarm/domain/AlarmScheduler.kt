package com.alijafari.raise.feature_alarm.domain

import com.alijafari.raise.feature_alarm.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(
        alarm: Alarm,
        actualTriggerMillis: Long ? = null
    )
    fun snooze(
        alarm: Alarm
    ) : Long
    fun cancelSnooze(
        alarm: Alarm
    )
    fun cancel(
        alarm: Alarm
    )
}