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
import com.alijafari.raise.feature_logs.data.repository.LogRepositoryImpl
import com.alijafari.raise.feature_logs.domain.model.EventLog

class AndroidScheduler (
    private val context: Context
) : AlarmScheduler{
    private val logRepository = LogRepositoryImpl()
    private val alarmManager = context.getSystemService(AlarmManager::class.java)!!

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(alarm: Alarm) {
        if (alarm.isEnabled.not()) return
        val triggerTime = alarm.getNextTriggerAtMillis()
        val intent = Intent(context , AlarmReceiver::class.java).apply {
            action = AlarmBroadcastEvent.RING()
            putExtra(
                AlarmIntentExtra.ID(), alarm.id
            )
        }
        val infoIntent = Intent(context , MainActivity::class.java).apply {
            putExtra(
                AlarmIntentExtra.ID(), alarm.id
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context , alarm.id , intent , PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val infoPendingIntent = PendingIntent.getActivity(
            context , alarm.id + 6000 , infoIntent , PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerTime , infoPendingIntent),
            pendingIntent
        )
        logRepository.logEvent(
            EventLog(
                event = "Alarm Scheduled",
                info = "AndroidScheduler scheduled $alarm for $triggerTime"
            )
        )
    }
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun snooze(alarm: Alarm) {
        if (alarm.isEnabled.not()) return
        val triggerTime = System.currentTimeMillis() + 60 * 1000 * alarm.snoozeMinutes
        val intent = Intent(context , AlarmReceiver::class.java).apply {
            action = AlarmBroadcastEvent.RING()
            putExtra(
                AlarmIntentExtra.ID(), alarm.id
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context , alarm.id + 5000 , intent , PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.setExactAndAllowWhileIdle(
             AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        logRepository.logEvent(
            EventLog(
                event = "Alarm Snooze Scheduled",
                info = "AndroidScheduler scheduled $alarm for $triggerTime "
            )
        )
    }
    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun cancelSnooze(alarm: Alarm) {
        if (alarm.isEnabled.not()) return
        val intent = Intent(context , AlarmReceiver::class.java).apply {
            action = AlarmBroadcastEvent.RING()
            putExtra(
                AlarmIntentExtra.ID(), alarm.id
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context , alarm.id + 5000 , intent , PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        logRepository.logEvent(
            EventLog(
                event = "Snooze Cancelled",
                info = "AndroidScheduler snooze cancelled for $alarm"
            )
        )
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context , AlarmReceiver::class.java).apply {
            putExtra(
                AlarmIntentExtra.ID(), alarm.id
            )
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context , alarm.id , intent , PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(
            pendingIntent
        )
        logRepository.logEvent(
            EventLog(
                event = "Alarm Scheduled",
                info = "AndroidScheduler cancelled $alarm"
            )
        )
    }
}