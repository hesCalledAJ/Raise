package com.alijafari.raise.feature_alarm.presentation.ring

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.alijafari.raise.core.ui.theme.Wakee2Theme
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_logs.domain.model.EventLog
import com.alijafari.raise.feature_logs.domain.repository.LogRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val ACTION_FINISH_RING_ACTIVITY = "com.alijafari.wakee2.ACTION_FINISH_RING_ACTIVITY"

@AndroidEntryPoint
class RingActivity : ComponentActivity() {
    @Inject
    lateinit var logRepository: LogRepository
    private var finishReceiver: BroadcastReceiver? = null
    private var alarmService: AlarmService? = null
    private var isBound = false
    private val viewModel: RingViewModel by viewModels()


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val alarmBinder = binder as AlarmService.AlarmBinder
            alarmService = alarmBinder.getService()
            isBound = true
            alarmService?.let { viewModel.attachService(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            alarmService = null
            viewModel.detachService()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            viewModel.hideHeadsUpNotification()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        }
        super.onCreate(savedInstanceState)

        registerFinishReceiver()

        enableEdgeToEdge()

        setContent {
            val alarm by viewModel.alarm.collectAsState()
            val snoozeRemaining by viewModel.snoozeRemaining.collectAsState(initial = 0L)
            val snoozeUntil by viewModel.snoozedUntil.collectAsState()
            Wakee2Theme {
                val isSnoozed by viewModel.isSnoozed.collectAsState(initial = false)

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    RingScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        alarm = alarm,
                        isSnoozed = isSnoozed,
                        snoozeRemaining = snoozeRemaining,
                        snoozeUntil = snoozeUntil,
                        onDismiss = { viewModel.onDismiss() },
                        onSnooze = { viewModel.onSnooze() },
                        onSkipSnooze = { viewModel.onSkipSnooze() }
                    )
                }
            }
        }
    }

    private fun registerFinishReceiver() {
        finishReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                logRepository.logEvent(
                    EventLog(
                        event = "Ring Activity",
                        info = "Finish Received"
                    )
                )
                finish()
            }

        }
        val filter = IntentFilter(ACTION_FINISH_RING_ACTIVITY)
        val listenToBroadcastsFromOtherApps = false
        val receiverFlags = if (listenToBroadcastsFromOtherApps) {
            ContextCompat.RECEIVER_EXPORTED
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED
        }
        ContextCompat.registerReceiver(this, finishReceiver, filter, receiverFlags)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, AlarmService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        viewModel.detachService()
        finishReceiver?.let {
            unregisterReceiver(it)
        }
    }
}
