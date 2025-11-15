package com.alijafari.raise.core.utils

import android.util.Log

import kotlin.math.pow

object VolumeUtils {
    fun linearToPerceivedVolume(linear: Float, gamma: Float = 2.0f): Float {
        return linear.coerceIn(0f, 1f).pow(gamma).also {
            Log.e("VolumeUtils", "linearToPerceivedVolume: $linear to $it", )
        }
    }
}