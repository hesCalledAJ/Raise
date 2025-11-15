package com.alijafari.raise.feature_alarm.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alijafari.raise.R
import com.alijafari.raise.core.utils.getTimeString
import com.alijafari.raise.feature_alarm.data.AlarmBroadcastEvent
import com.alijafari.raise.feature_alarm.data.AlarmIntentExtra
import com.alijafari.raise.feature_alarm.data.AlarmReceiver
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.presentation.ring.RingActivity

object NotificationHelper {
    const val CHANNEL_ID = "SERVICE_CHANNEL"
    fun getBaseNotification(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(context.getSystemService(NotificationManager::class.java))
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Alarm Service Running").setContentText("Fetching alarm details...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN).setSilent(true).build()
    }

    fun getAlarmNotification(
        context: Context,
        alarm: Alarm,
        isSnoozed: Boolean = false,
        hideHeadsUp : Boolean = false
    ): Notification {
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmBroadcastEvent.SNOOZE()
            putExtra(AlarmIntentExtra.ID(), alarm.id)
        }
        val skipSnoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmBroadcastEvent.RING()
            putExtra(AlarmIntentExtra.ID(), alarm.id)
        }
        val contentIntent = Intent(context, RingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        //DISMISS action must open the ring activity in order for future Challenge requirement , or showing set Notes before dismissing the alarm ; thus user can dismiss the Alarm only through the Activity at least for now
//
//          val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
//              action = AlarmBroadcastEvent.KILL()
//              putExtra(AlarmIntentExtra.ID(), alarm.id)
//          }
//          val dismissPendingIntent = PendingIntent.getBroadcast(
//            context,
//            alarm.id + 1,
//            dismissIntent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id + 2,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val skipSnoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            skipSnoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id + 3,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .run {
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setPriority(NotificationCompat.PRIORITY_HIGH)
                setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).setColorized(true)
                setContentIntent(contentPendingIntent)
                setFullScreenIntent(contentPendingIntent,true)
                if (isSnoozed) {
                    addAction(R.drawable.ic_launcher_foreground, "Skip", skipSnoozePendingIntent)
                    setContentTitle(
                        context.getString(
                            R.string.alarm_snoozed_notification_title, alarm.label
                        )
                    )
                    setContentText(
                        context.getString(
                            R.string.alarm_snoozed_notification_subtitle,
                            alarm.getTimeString()
                        )
                    )
                } else {
                    setContentTitle(alarm.label)
                    addAction(R.drawable.ic_launcher_foreground, "Dismiss", contentPendingIntent)
                    addAction(R.drawable.ic_launcher_foreground, "Snooze", snoozePendingIntent)
                }
                setSound(null)
                setSilent(isSnoozed || hideHeadsUp)
                build()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(notificationManager: NotificationManager) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, "Service Channel", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null,null)
            }
        )
    }
}
