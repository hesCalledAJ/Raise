package com.alijafari.raise.feature_ringtone.domain.infrastructure

import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

interface RingtonePreviewPlayer {
    fun play(ringtone: RingtoneData, volume: Float)
    fun stop()
    fun setVolume(volume: Float)
}