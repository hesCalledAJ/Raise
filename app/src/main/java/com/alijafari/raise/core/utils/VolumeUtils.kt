package com.alijafari.raise.core.utils

import kotlin.math.pow

object VolumeUtils {
    fun linearToPerceivedVolume(linear: Float, gamma: Float = 2.0f): Float {
        return linear.coerceIn(0f, 1f).pow(gamma)
    }
}