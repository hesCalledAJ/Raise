package com.alijafari.raise.feature_ringtone.domain.infrastructure

import android.app.AlarmManager
import android.media.AudioManager

interface SystemVolumeManager {
    fun setVolumeForType(volume : Int, type : Int,showUi : Boolean = false)
    fun setMaxVolumeForType(type : Int = AudioManager.STREAM_ALARM, showUi : Boolean = false)
    fun getMaxVolumeForType(type : Int = AudioManager.STREAM_ALARM) : Int
    fun getVolumeForType(type : Int = AudioManager.STREAM_ALARM) : Int
}