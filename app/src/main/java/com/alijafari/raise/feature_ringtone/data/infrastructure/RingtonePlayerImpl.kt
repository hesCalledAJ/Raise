package com.alijafari.raise.feature_ringtone.data.infrastructure

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.alijafari.raise.core.utils.VolumeUtils
import com.alijafari.raise.feature_ringtone.domain.infrastructure.RingtonePlayer
import com.alijafari.raise.feature_ringtone.domain.model.RingtoneData
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RingtonePlayerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RingtonePlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun play(ringtone: RingtoneData, volume: Float, fadeInMs: Long?) {
        stop()

        val mp = MediaPlayer.create(context, ringtone.uri) ?: return
        mediaPlayer = mp

        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mp.isLooping = true

        if (fadeInMs != null && fadeInMs > 0) {
            mp.setVolume(0f, 0f)
            mp.start()
            startFadeIn(target = volume.coerceIn(0f, 1f), duration = fadeInMs)
        } else {
            mp.setVolume(volume, volume)
            mp.start()
        }
    }

    override fun stop() {
        fadeJob?.cancel()
        fadeJob = null
        mediaPlayer?.run {
            try { stop() } catch (_: Exception) {}
            release()
        }
        mediaPlayer = null
    }

    override fun setVolume(volume: Float) {
        val perceivedVolume = VolumeUtils.linearToPerceivedVolume(volume.coerceIn(0f, 1f))
        mediaPlayer?.setVolume(perceivedVolume, perceivedVolume)
    }

    private fun startFadeIn(target: Float, duration: Long) {
        fadeJob?.cancel()
        val mp = mediaPlayer ?: return

        fadeJob = scope.launch {
            val steps = 50
            val stepDur = duration / steps
            for (i in 1..steps) {
                if (!mp.isPlaying) break
                val vol = target * (i / steps.toFloat())
                Log.e("TAG", "fade: $vol", )
                mp.setVolume(vol, vol)
                delay(stepDur)
            }
            mp.setVolume(target, target)
        }
    }
}
