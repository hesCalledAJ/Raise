package com.alijafari.raise.feature_alarm.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TimeBombData(
    val enabled: Boolean = false,
    val delaySeconds: Int = 50,// 30..120
) : Parcelable