package com.alijafari.raise.feature_ringtone.data.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.alijafari.raise.feature_ringtone.domain.infrastructure.RingtonePreviewPlayer
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class RingtonePreviewPlayerImpl @Inject constructor(@ApplicationContext private val context: Context) :
    RingtonePreviewPlayer {
    private var mediaPlayer: MediaPlayer? = null

    override fun play(ringtone: RingtoneData, volume: Float) {
        try {
            stop()
            mediaPlayer = MediaPlayer.create(context, ringtone.uri)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(volume, volume)
                start()
            }
        } catch (_: Exception){}
    }

    override fun stop() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception){}
    }

    override fun setVolume(volume: Float) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (_: Exception){}
    }
}