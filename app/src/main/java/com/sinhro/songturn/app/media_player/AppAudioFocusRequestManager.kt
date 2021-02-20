package com.sinhro.songturn.app.media_player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

class AppAudioFocusRequestManager {
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioManager: AudioManager? = null

    fun init(audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener) {
        this.audioFocusChangeListener = audioFocusChangeListener
    }

    fun requestAudioFocus(context: Context): Boolean {
        val result: Int
        audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    result = requestAudioFocusResult(it)
                } else {
                    result = requestAudioFocusResultLegacy(it)
                }
            }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun removeAudioFocus(): Boolean {
        var abandonAudioFocusResult: Int? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                abandonAudioFocusResult = audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            abandonAudioFocusResult = audioManager?.abandonAudioFocus(audioFocusChangeListener)
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == abandonAudioFocusResult
    }

    private fun requestAudioFocusResultLegacy(audioManager: AudioManager): Int {
        return audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusResult(audioManager: AudioManager): Int {
        val audioFocusRequestBuilder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

        audioFocusChangeListener?.let {
            audioFocusRequestBuilder.setOnAudioFocusChangeListener(it)
        }

        audioFocusRequest = audioFocusRequestBuilder.build()

        return audioManager.requestAudioFocus(audioFocusRequest!!)
    }
}