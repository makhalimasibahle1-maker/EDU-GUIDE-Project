package com.example.videoconfrence.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class RTCAudioManager private constructor(context: Context) {

    enum class AudioDevice {
        SPEAKER_PHONE, EARPIECE
    }

    private val audioManager: AudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    companion object {
        fun create(context: Context): RTCAudioManager {
            return RTCAudioManager(context)
        }
    }

    fun setDefaultAudioDevice(device: AudioDevice) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        when (device) {
            AudioDevice.SPEAKER_PHONE -> {
                audioManager.isSpeakerphoneOn = true
            }
            AudioDevice.EARPIECE -> {
                audioManager.isSpeakerphoneOn = false
            }
        }
    }
}