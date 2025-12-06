package com.alijafari.raise.feature_alarm.domain.model

data class TimeBombData(
    val enabled: Boolean = false,
    val delaySeconds: Int = 50// 30..120
)