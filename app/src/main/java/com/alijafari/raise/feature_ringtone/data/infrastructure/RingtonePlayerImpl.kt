package com.alijafari.raise.feature_ringtone.data.infrastructure

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.alijafari.raise.R
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
    private var vibrator: Vibrator? = null
    private var fadeJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun play(ringtone: RingtoneData, volume: Float, fadeInMs: Long?, vibrate: Boolean) {
        stop()

        val mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(context, ringtone.uri!!)
            isLooping = true
            prepare()
        }

        mediaPlayer = mp

        val level = volume.coerceIn(0f, 1f)
        val targetActual = VolumeUtils.linearToPerceivedVolume(level)
        mp.setVolume(
            if (fadeInMs != null && fadeInMs > 0) 0f else targetActual,
            if (fadeInMs != null && fadeInMs > 0) 0f else targetActual
        )

        mp.start()

        if (fadeInMs != null && fadeInMs > 0) {
            startFadeIn(level = level, duration = fadeInMs)
        }

        if (vibrate) startVibration()
    }

    override fun stop() {
        fadeJob?.cancel()
        fadeJob = null
        mediaPlayer?.run {
            try { stop() } catch (_: Exception) {}
            release()
        }
        mediaPlayer = null
        stopVibration()
    }

    override fun setVolume(volume: Float) {
        val level = volume.coerceIn(0f, 1f)
        val actual = VolumeUtils.linearToPerceivedVolume(level)
        mediaPlayer?.setVolume(actual, actual)
    }

    private fun startFadeIn(level: Float, duration: Long) {
        fadeJob?.cancel()
        val mp = mediaPlayer ?: return

        fadeJob = scope.launch {
            val steps = 50
            val stepDur = duration / steps

            for (i in 1..steps) {
                if (!mp.isPlaying) break
                val progress = i.toFloat() / steps
                val currentLevel = level * progress
                val actual = VolumeUtils.linearToPerceivedVolume(currentLevel)
                mp.setVolume(actual, actual)
                delay(stepDur)
            }
            val finalActual = VolumeUtils.linearToPerceivedVolume(level)
            mp.setVolume(finalActual, finalActual)
        }
    }

    private fun startVibration() {
        stopVibration()

        val vib = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator ?: return
        vibrator = vib

        // Safety checks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!vib.hasVibrator()) return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 150, 200, 150, 1500)
            val amplitudes = intArrayOf(0, 180, 0, 255, 0)

            val effect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            vib.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(longArrayOf(0, 500, 500), 0)
        }
    }
    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    override fun stopTimeBombSound() {
        stop()
    }

    override fun playTimeBombSound() {
        stop()
        val mp = MediaPlayer.create(context, R.raw.loud).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
        }

        mediaPlayer = mp

        mp.setVolume(1.0f, 1.0f)

        mp.start()
        startVibration()
    }
}