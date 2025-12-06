package com.alijafari.raise.feature_alarm.domain.model

data class OffsetData(
    val enabled : Boolean = false,
    val range : IntRange = -10..15
)