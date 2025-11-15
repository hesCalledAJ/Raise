package com.alijafari.raise.feature_alarm.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.alijafari.raise.MainActivity
import com.alijafari.raise.feature_alarm.domain.AlarmScheduler
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_logs.domain.model.EventLog
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
class AndroidScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logRepository: LogRepository,
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    private fun buildPendingIntent(alarm: Alarm, offset: Int = 0, isMainActivityIntent: Boolean = false): PendingIntent {
        val intent = if (isMainActivityIntent) {
            Intent(context, MainActivity::class.java).putExtra(AlarmIntentExtra.ID(), alarm.id)
        } else {
            Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmBroadcastEvent.RING()
                putExtra(AlarmIntentExtra.ID(), alarm.id)
            }
        }
        val requestCode = alarm.id + offset
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        return if (isMainActivityIntent) PendingIntent.getActivity(context, requestCode, intent, flag)
        else PendingIntent.getBroadcast(context, requestCode, intent, flag)
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val triggerTime = alarm.getNextTriggerAtMillis()
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime, buildPendingIntent(alarm, 6000, true)),
            buildPendingIntent(alarm)
        )
        logRepository.logEvent(EventLog("Alarm Scheduled", "AndroidScheduler scheduled $alarm for $triggerTime"))
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun snooze(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val triggerTime = System.currentTimeMillis() + 60_000 * alarm.snoozeMinutes
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            buildPendingIntent(alarm, 5000)
        )
        logRepository.logEvent(EventLog("Alarm Snooze Scheduled", "AndroidScheduler scheduled $alarm for $triggerTime"))
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun cancelSnooze(alarm: Alarm) {
        if (!alarm.isEnabled) return
        alarmManager.cancel(buildPendingIntent(alarm, 5000))
        logRepository.logEvent(EventLog("Snooze Cancelled", "AndroidScheduler snooze cancelled for $alarm"))
    }

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(buildPendingIntent(alarm))
        logRepository.logEvent(EventLog("Alarm Cancelled", "AndroidScheduler cancelled $alarm"))
    }
}
