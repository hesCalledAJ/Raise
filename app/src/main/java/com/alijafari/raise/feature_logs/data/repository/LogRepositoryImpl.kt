package com.alijafari.raise.feature_logs.data.repository

import android.util.Log
import com.alijafari.raise.feature_logs.domain.model.EventLog
import com.alijafari.raise.feature_logs.domain.repository.LogRepository

class LogRepositoryImpl : LogRepository {
    override fun logEvent(eventLog: EventLog) {
        Log.println(
            Log.INFO,
            eventLog.event,
            eventLog.info
        )
    }

    override fun logError(reason : String ,error: Throwable) {
        Log.println(
            Log.ERROR,
            reason,
            error.message + "\n"+ error.stackTrace.toString()
        )
    }
}