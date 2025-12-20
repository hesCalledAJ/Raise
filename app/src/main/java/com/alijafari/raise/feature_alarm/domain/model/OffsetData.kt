package com.alijafari.raise.feature_alarm.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class OffsetData(
    val enabled: Boolean = false,
    val range: IntRange = -10..15,
) : Parcelable