package com.liquidglass.calculator.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.liquidglass.calculator.data.HapticStrength
import java.util.Locale
import kotlin.math.exp
import kotlin.math.sin

class FeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        mgr?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val clickPcm: ShortArray by lazy { generateCrispClickPcm() }

    fun performKeyFeedback(
        hapticEnabled: Boolean,
        hapticStrength: HapticStrength,
        soundEnabled: Boolean
    ) {
        if (hapticEnabled) {
            triggerHaptic(hapticStrength)
        }
        if (soundEnabled) {
            playCrispClick()
        }
    }

    private fun triggerHaptic(strength: HapticStrength) {
        val vib = vibrator ?: return
        try {
            if (!vib.hasVibrator()) return
            val durationMs = when (strength) {
                HapticStrength.LIGHT -> 10L
                HapticStrength.MEDIUM -> 18L
                HapticStrength.STRONG -> 28L
            }
            val amplitude = when (strength) {
                HapticStrength.LIGHT -> 65
                HapticStrength.MEDIUM -> 130
                HapticStrength.STRONG -> 220
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: SecurityException) {
            // Vibrator permission unavailable; ignore gracefully
        }
    }

    private fun playCrispClick() {
        try {
            val sampleRate = 22050
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
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
                .setBufferSizeInBytes(clickPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(clickPcm, 0, clickPcm.size)
            track.play()
            track.setNotificationMarkerPosition(clickPcm.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    t?.release()
                }
                override fun onPeriodicNotification(t: AudioTrack?) = Unit
            })
        } catch (_: Exception) {
            // Audio hardware unavailable or muted; ignore gracefully
        }
    }

    private fun generateCrispClickPcm(): ShortArray {
        val sampleRate = 22050
        val count = (sampleRate * 0.012).toInt() // 12ms subtle tactile tap
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 480.0)
            val wave = sin(2.0 * Math.PI * 1450.0 * t) * 0.5 + sin(2.0 * Math.PI * 2900.0 * t) * 0.25
            (wave * env * Short.MAX_VALUE * 0.35).toInt().toShort()
        }
    }

    fun speakResult(text: String) {
        if (text.isBlank()) return
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    ttsReady = true
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "calc_res")
                }
            }
        } else if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "calc_res")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }
}
