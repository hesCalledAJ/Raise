package com.alijafari.raise.feature_logs.domain.repository

import com.alijafari.raise.feature_logs.domain.model.EventLog

interface LogRepository {
    fun logEvent(
        eventLog: EventLog
    )
    fun logError(
        reason : String ,error : Throwable
    )
}