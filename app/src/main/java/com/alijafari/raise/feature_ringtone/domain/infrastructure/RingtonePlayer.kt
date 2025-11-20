package com.alijafari.raise.feature_ringtone.domain.infrastructure

import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData

interface RingtonePlayer {
    fun play(ringtone: RingtoneData, volume: Float , fadeInMs: Long? = null,vibrate:Boolean=false)
    fun stop()
    fun setVolume(volume: Float)
}