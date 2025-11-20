package com.alijafari.raise.feature_alarm.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.alijafari.raise.feature_alarm.data.AlarmBroadcastEvent
import com.alijafari.raise.feature_alarm.data.AlarmIntentExtra
import com.alijafari.raise.feature_alarm.data.service.NotificationHelper.getAlarmNotification
import com.alijafari.raise.feature_alarm.data.service.NotificationHelper.getBaseNotification
import com.alijafari.raise.feature_alarm.domain.model.Alarm
import com.alijafari.raise.feature_alarm.domain.usecases.AlarmUseCases
import com.alijafari.raise.feature_alarm.presentation.ring.ACTION_FINISH_RING_ACTIVITY
import com.alijafari.raise.feature_alarm.presentation.ring.RingActivity
import com.alijafari.raise.feature_logs.domain.model.EventLog
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import com.alijafari.raise.feature_ringtone.domain.infrastructure.RingtonePlayer
import com.alijafari.raise.feature_ringtone.domain.infrastructure.SystemVolumeManager
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var useCases: AlarmUseCases

    @Inject
    lateinit var ringtonePlayer: RingtonePlayer

    @Inject
    lateinit var systemVolumeManager: SystemVolumeManager

    lateinit var alarm: Alarm

    var alarmId: Int = -1

    var isPreview: Boolean = false

    private val _isSnoozed = MutableStateFlow(false)
    val isSnoozed: StateFlow<Boolean> = _isSnoozed.asStateFlow()

    @Inject
    lateinit var logRepository: LogRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    inner class AlarmBinder : Binder() {
        fun getService(): AlarmService = this@AlarmService
    }

    private val binder = AlarmBinder()

    override fun onBind(intent: Intent?): IBinder = binder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        alarmId = intent?.getIntExtra(AlarmIntentExtra.ID(), -1) ?: -1
        logStep("Start command with intent ${intent?.action} for id $alarmId")

        if (intent?.action == null || alarmId == -1) {
            abort(
                "bad intent , Action ${intent?.action} for id $alarmId"
            )
            return START_NOT_STICKY
        }

        startForeground(alarmId, getBaseNotification(applicationContext).also {
            logStep("Base Notification Sent")
        })

        when (intent.action) {
            AlarmBroadcastEvent.PREVIEW() -> handlePreview()
            AlarmBroadcastEvent.RING() -> handleRing()
            AlarmBroadcastEvent.SNOOZE() -> handleSnooze()
            AlarmBroadcastEvent.KILL() -> handleDismiss()
            else -> {
                abort("Unknown action: ${intent.action}")
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }

    private fun abort(reason: String) {
        logStep(tag = "Service Abort", step = "Reason : $reason")
        stopService()
    }

    private fun handleRing() {
        logStep("Ring started for id $alarmId")
        startActivity(
            Intent(
                applicationContext, RingActivity::class.java
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        _isSnoozed.value = false
        scope.launch {
            try {
                initializeAlarm()
                updateNotification()
                ringAlarm()
            } catch (e: Exception) {
                logError("Error initializing alarm: ${e.message}", e)
                abort(e.message ?: "Error initializing alarm")
            }
        }
    }

    private suspend fun initializeAlarm() {
        try {
            alarm.toString()
        } catch (_: UninitializedPropertyAccessException) {
            alarm = useCases.getById(alarmId)
        }
        logStep("Alarm Initialized $alarm")
        rescheduleAlarmRepeats()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleAlarmRepeats() {

        logStep("Repeat Scheduled ${alarm.repeatDays}")
        if (alarm.repeatDays.isEmpty()) {
            useCases.schedule(alarm)
        }else {
            scope.launch {
                useCases.upsert(alarm.copy(isEnabled = false))
            }
        }
    }

    private fun handlePreview() {
        isPreview = true
        handleRing()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun handleSnooze() {
        _isSnoozed.value = true
        logStep("Snooze pressed for id ${alarm.id}")

        ringtonePlayer.stop()
        useCases.snooze(alarm)

        updateNotification()
    }

    fun handleDismiss() {
        logStep("Dismiss pressed for id $alarm.id")
        stopService()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun handleSkipSnooze() {
        logStep("handleSkipSnooze")
        useCases.cancelSnooze(alarm)
        handleRing()
    }

    private fun ringAlarm() {
        if (alarm.ringtoneData == null) return
        systemVolumeManager.setMaxVolumeForType()

        logStep("ring alarm")
        ringtonePlayer.play(
            alarm.ringtoneData!!, alarm.ringtoneVolume, 30000 , alarm.vibrate
        )
    }

    private fun updateNotification(hideHeadsUp: Boolean = false) {
        startForeground(
            1,
            getAlarmNotification(applicationContext, alarm, isSnoozed.value, hideHeadsUp)
        )
    }

    fun hideHeadsUpNotification() {
        logStep("hideHeadsUpNotification")
        updateNotification(true)
    }

    fun stopService() {
        logStep("Alarm Service Stop")
        sendFinishRingActivityBroadcast()
        ringtonePlayer.stop()
        stopSelf()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun sendFinishRingActivityBroadcast() {
        val intent = Intent(ACTION_FINISH_RING_ACTIVITY).apply {
            setPackage(applicationContext.packageName)
        }
        logStep("Alarm Service Stop")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        logStep("Alarm Service Destroyed")
        ringtonePlayer.stop()
        scope.cancel()
        super.onDestroy()
    }

    fun logError(reason: String, error: Throwable) {
        logRepository.logError(reason, error)
    }

    fun logStep(step: String, tag: String = "Service Step") {
        logRepository.logEvent(
            EventLog(
                event = ">$tag",
                info = step
            )
        )
    }
}
