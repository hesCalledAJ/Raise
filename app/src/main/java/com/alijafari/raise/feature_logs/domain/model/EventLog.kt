package com.alijafari.raise.feature_logs.domain.model

data class EventLog(
    val event : String,
    val info : String,
    val time : Long = System.currentTimeMillis()
)