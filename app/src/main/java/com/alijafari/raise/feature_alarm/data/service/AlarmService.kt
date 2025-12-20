package com.alijafari.raise.feature_alarm.data.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AlarmStatus {
    object Ringing : AlarmStatus
    data class Snoozed(val remainingMs: Long, val totalMs: Long,val targetTimeMs: Long) : AlarmStatus
    data class TimeBombCountDown(val remainingMs: Long, val totalMs: Long) : AlarmStatus
    object TimeBombRinging : AlarmStatus
}

class AlarmState(
    val alarm: Alarm,
    val scope: CoroutineScope,
    val useCases: AlarmUseCases,
    val systemVolumeManager: SystemVolumeManager,
    val ringtonePlayer: RingtonePlayer,
    val onFatalError: (e: Exception) -> Unit,
    val updateNotification: () -> Unit,
    val log: ((String) -> Unit)? = null
) {
    private val _status = MutableStateFlow<AlarmStatus>(AlarmStatus.Ringing)
    val status: StateFlow<AlarmStatus> = _status.asStateFlow()

    private var countdownJob: Job? = null

    fun ring() {
        countdownJob?.cancel()
        _status.value = AlarmStatus.Ringing

        if (alarm.timeBombData.enabled) {
            startTimeBomb(alarm.timeBombData.delaySeconds * 1000L)
        }

        scope.launch {
            try {
                updateNotification()
                playRingtone()
            } catch (e: Exception) {
                onFatalError(e)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun snooze() {
        if (alarm.snoozeMinutes == 0) return
        countdownJob?.cancel()
        ringtonePlayer.stop()

        val snoozedUntil = useCases.snooze(alarm)
        val totalSnoozeTime = snoozedUntil - System.currentTimeMillis()

        startCountdown(
            totalMs = totalSnoozeTime,
            onTick = { remaining -> _status.value = AlarmStatus.Snoozed(remaining, totalSnoozeTime,snoozedUntil) },
            onFinished = {
//                ring()
            }
        )

        updateNotification()
        log?.invoke("Snooze pressed for id ${alarm.id}")
    }

    fun startTimeBomb(timeoutMs: Long) {
        countdownJob?.cancel()
        startCountdown(
            totalMs = timeoutMs,
            onTick = { remaining -> _status.value = AlarmStatus.TimeBombCountDown(remaining, timeoutMs) },
            onFinished = {
                log?.invoke("TimeBomb finished")
                _status.value = AlarmStatus.TimeBombRinging
                ringtonePlayer.playTimeBombSound()
            }
        )
    }

    private fun startCountdown(
        totalMs: Long,
        onTick: (Long) -> Unit,
        onFinished: () -> Unit
    ) {
        countdownJob = scope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + totalMs

            while (System.currentTimeMillis() < endTime) {
                val currentRemaining = (endTime - System.currentTimeMillis()).coerceAtLeast(0)
                onTick(currentRemaining)
                delay(1000)
            }
            onFinished()
        }
    }

    fun cancelTimeBomb() {
        if (_status.value is AlarmStatus.TimeBombRinging) {
            _status.value = AlarmStatus.Ringing
            ringtonePlayer.stopTimeBombSound()
        }
        else if (_status.value is AlarmStatus.TimeBombCountDown) {
            countdownJob?.cancel()
            _status.value = AlarmStatus.Ringing
        }
    }

    fun playRingtone() {
        if (alarm.ringtoneData == null) return
        systemVolumeManager.setMaxVolumeForType()
        log?.invoke("ring alarm")
        ringtonePlayer.play(alarm.ringtoneData!!, alarm.ringtoneVolume, 30000, alarm.vibrate)
    }
}
@AndroidEntryPoint
class AlarmService : Service() {

    var isPreview: Boolean = false

    @Inject
    lateinit var useCases: AlarmUseCases

    @Inject
    lateinit var ringtonePlayer: RingtonePlayer

    @Inject
    lateinit var systemVolumeManager: SystemVolumeManager

    lateinit var alarm: Alarm

    lateinit var state: AlarmState

    var alarmId: Int = -1

    var actualTriggerMillis : Long = 0 // is used for in calculation of next trigger time correctly when smart offset has been used

    private val scope = CoroutineScope(Dispatchers.IO)


    @Inject
    lateinit var logRepository: LogRepository


    inner class AlarmBinder : Binder() {
        fun getService(): AlarmService = this@AlarmService
    }

    private val binder = AlarmBinder()

    override fun onBind(intent: Intent?): IBinder = binder


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        alarmId = intent?.getIntExtra(AlarmIntentExtra.ID(), -1) ?: -1
        actualTriggerMillis = intent?.getLongExtra(AlarmIntentExtra.ACTUAL_TRIGGER_MILLIS(), -1) ?: -1

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
            AlarmBroadcastEvent.PREVIEW() -> handlePreview(intent)
            AlarmBroadcastEvent.RING() -> handleRing(intent)
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

    private fun handleRing(intent: Intent?) {
        scope.launch {
            initializeAlarm(intent)
            state.ring()
            startActivity(
                Intent(
                    applicationContext, RingActivity::class.java
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    private suspend fun initializeAlarm(intent: Intent?) {
        if (::alarm.isInitialized.not()) {
            val alarmFromIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(
                    AlarmIntentExtra.PREVIEW_ALARM_OBJECT(),
                    Alarm::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent?.getParcelableExtra(AlarmIntentExtra.PREVIEW_ALARM_OBJECT())
            }
            alarm = alarmFromIntent ?: useCases.getById(alarmId)
        }
        state = AlarmState(
            alarm = alarm,
            scope = scope,
            useCases = useCases,
            systemVolumeManager = systemVolumeManager,
            ringtonePlayer = ringtonePlayer,
            updateNotification = {updateNotification()},
            log = { logStep(it) },
            onFatalError = {abort(it.message?: "Error initializing alarm")}
        )
        logStep("Alarm Initialized $alarm")
        if (!isPreview) rescheduleAlarmRepeats()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun rescheduleAlarmRepeats() {
        logStep("Repeat Scheduled ${alarm.repeatDays}")
        if (alarm.repeatDays.isEmpty()) {
            useCases.schedule(alarm,actualTriggerMillis)
        }else {
            scope.launch {
                useCases.upsert(alarm.copy(isEnabled = false))
            }
        }
    }

    private fun handlePreview(intent: Intent?) {
        isPreview = true
        handleRing(intent)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun handleSnooze() {
        state.snooze()
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
        handleDismiss()
    }

    private fun updateNotification(hideHeadsUp: Boolean = false) {
        startForeground(
            alarmId,
            getAlarmNotification(applicationContext, alarm, state.status.value is AlarmStatus.Snoozed, hideHeadsUp)
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
