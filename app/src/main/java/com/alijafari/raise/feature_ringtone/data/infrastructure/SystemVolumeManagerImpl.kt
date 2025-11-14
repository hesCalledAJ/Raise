package com.alijafari.raise.feature_ringtone.data.infrastructure

import android.media.AudioManager
import com.alijafari.raise.feature_ringtone.domain.infrastructure.SystemVolumeManager
import javax.inject.Inject

class SystemVolumeManagerImpl @Inject constructor(
    private val audioManager: AudioManager
) : SystemVolumeManager {
    override fun setVolumeForType(volume: Int, type: Int, showUi: Boolean) {
        audioManager.setStreamVolume(type,volume,if (showUi) AudioManager.FLAG_SHOW_UI else AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }
    override fun setMaxVolumeForType(type: Int, showUi: Boolean) {
        val maxVolume = getMaxVolumeForType(type)
        audioManager.setStreamVolume(type,maxVolume,if (showUi) AudioManager.FLAG_SHOW_UI else AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

    override fun getMaxVolumeForType(type: Int) : Int {
        return audioManager.getStreamMaxVolume(type)
    }

    override fun getVolumeForType(type: Int): Int {
        return audioManager.getStreamVolume(type)
    }
}