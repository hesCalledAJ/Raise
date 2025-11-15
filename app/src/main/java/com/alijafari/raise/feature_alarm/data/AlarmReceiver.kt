package com.alijafari.raise.feature_alarm.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alijafari.raise.feature_alarm.data.service.AlarmService
import com.alijafari.raise.feature_logs.data.repository.LogRepositoryImpl
import com.alijafari.raise.feature_logs.domain.model.EventLog

enum class AlarmBroadcastEvent(val value: String) {
    HIDE_HEADS_UP("hide_heads_up"), // to hide the pop up notification when the activity is open
    PREVIEW("preview"),
    SNOOZE("snooze"),
    KILL("kill"),
    RING("ring");
    operator fun invoke() = value
}
enum class AlarmIntentExtra(val value: String) {
    ID("id");
    operator fun invoke() = value
}

class AlarmReceiver : BroadcastReceiver() {
    val logRepository = LogRepositoryImpl()
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val id = intent.getIntExtra(AlarmIntentExtra.ID(),-1)
        logRepository.logEvent(
            EventLog(
                event = "AlarmReceiver",
                info = "Received ${intent.action} Action for Alarm with id $id"
            )
        )
        context?.startService(
            Intent(
                context, AlarmService::class.java
            ).apply {
                action = intent.action
                putExtra(AlarmIntentExtra.ID(),id)
            }
        )
    }
}