package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural retro sound synthesizer and haptic manager for Dash Snowball Fight.
 * Produces crisp, nostalgic 2013-style arcade audio without external assets.
 */
class SoundEffects(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isMuted: Boolean = false
    private var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setVibrationEnabled(enabled: Boolean) {
        isVibrationEnabled = enabled
    }

    /**
     * Snowball throwing swoosh effect (descending frequency chirp with soft noise)
     */
    fun playThrow() {
        if (isVibrationEnabled) {
            vibrate(30, 80)
        }
        if (isMuted) return
        scope.launch {
            playChirp(startFreq = 520f, endFreq = 260f, durationMs = 120, noiseMix = 0.35f)
        }
    }

    /**
     * Snowball hitting target (crunchy snow splat sound)
     */
    fun playHit() {
        if (isVibrationEnabled) {
            vibrate(60, 220)
        }
        if (isMuted) return
        scope.launch {
            playSnowSplat()
        }
    }

    /**
     * Player got hit by snowball (squish / thud)
     */
    fun playPlayerHit() {
        if (isVibrationEnabled) {
            vibratePattern(longArrayOf(0, 50, 40, 90), intArrayOf(0, 200, 0, 255))
        }
        if (isMuted) return
        scope.launch {
            playChirp(startFreq = 180f, endFreq = 80f, durationMs = 200, noiseMix = 0.6f)
        }
    }

    /**
     * Power-up collected or special snowball ready (sparkly upward arpeggio)
     */
    fun playPowerUp() {
        if (isVibrationEnabled) {
            vibrate(40, 120)
        }
        if (isMuted) return
        scope.launch {
            playToneSequence(listOf(523.25f, 659.25f, 783.99f, 1046.50f), noteDurationMs = 60)
        }
    }

    /**
     * Combo or target ring chime
     */
    fun playChime() {
        if (isVibrationEnabled) {
            vibrate(25, 100)
        }
        if (isMuted) return
        scope.launch {
            playToneSequence(listOf(880f, 1174.66f), noteDurationMs = 70)
        }
    }

    /**
     * Duck / dodge move
     */
    fun playDodge() {
        if (isVibrationEnabled) {
            vibrate(20, 60)
        }
        if (isMuted) return
        scope.launch {
            playChirp(startFreq = 300f, endFreq = 600f, durationMs = 90, noiseMix = 0.1f)
        }
    }

    /**
     * Victory fanfare
     */
    fun playVictory() {
        if (isVibrationEnabled) {
            vibrate(100, 200)
        }
        if (isMuted) return
        scope.launch {
            playToneSequence(listOf(523f, 659f, 784f, 1046f, 1318f), noteDurationMs = 90)
        }
    }

    /**
     * Defeat / Out of snowballs sound
     */
    fun playDefeat() {
        if (isMuted) return
        scope.launch {
            playToneSequence(listOf(440f, 415f, 392f, 349f), noteDurationMs = 120)
        }
    }

    private fun playChirp(startFreq: Float, endFreq: Float, durationMs: Int, noiseMix: Float) {
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val deltaPhase = 2.0 * PI * currentFreq / sampleRate
            phase += deltaPhase

            val envelope = (1.0 - progress) * (if (progress < 0.1) progress / 0.1 else 1.0)
            val tone = sin(phase)
            val noise = (Random.nextFloat() * 2.0 - 1.0)
            val sample = (tone * (1.0 - noiseMix) + noise * noiseMix) * envelope
            buffer[i] = (sample * 16000).toInt().coerceIn(-32767, 32767).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playSnowSplat() {
        val sampleRate = 22050
        val durationMs = 180
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val decay = exp(-progress * 6.0)
            val noise = (Random.nextFloat() * 2.0 - 1.0)
            val lowThud = sin(2.0 * PI * 120.0 * (i.toDouble() / sampleRate))
            val sample = (noise * 0.75 + lowThud * 0.25) * decay
            buffer[i] = (sample * 22000).toInt().coerceIn(-32767, 32767).toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playToneSequence(frequencies: List<Float>, noteDurationMs: Int) {
        val sampleRate = 22050
        val noteSamples = (sampleRate * (noteDurationMs / 1000.0)).toInt()
        val totalSamples = noteSamples * frequencies.size
        val buffer = ShortArray(totalSamples)

        var offset = 0
        for (freq in frequencies) {
            var phase = 0.0
            val deltaPhase = 2.0 * PI * freq / sampleRate
            for (i in 0 until noteSamples) {
                val progress = i.toDouble() / noteSamples
                val envelope = sin(PI * progress) // smooth bell envelope
                val sample = sin(phase) * envelope
                phase += deltaPhase
                buffer[offset + i] = (sample * 18000).toInt().coerceIn(-32767, 32767).toShort()
            }
            offset += noteSamples
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            // Track will release after playing
            scope.launch {
                val playDuration = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 100
                kotlinx.coroutines.delay(playDuration)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
            // AudioTrack initialization fallback
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 120) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
