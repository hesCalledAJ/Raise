package com.alijafari.raise.feature_alarm.domain

import com.alijafari.raise.feature_alarm.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(
        alarm: Alarm
    )
    fun snooze(
        alarm: Alarm
    )
    fun cancelSnooze(
        alarm: Alarm
    )
    fun cancel(
        alarm: Alarm
    )
}