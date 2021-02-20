package com.sinhro.songturn.app.media_player.media_session

import android.media.session.MediaSession
import android.support.v4.media.session.MediaSessionCompat

class AppMediaSessionCallback {
    private var onPlay: (() -> Unit)? = null
    private var onStop: (() -> Unit)? = null
    private var onPause: (() -> Unit)? = null
    private var onSkipToNext: (() -> Unit)? = null
    private var onSkipToPrevious: (() -> Unit)? = null
    private var onSeekTo: ((position: Long) -> Unit)? = null

    fun onPlayCallback() = onPlay?.invoke()
    fun onStopCallback() = onStop?.invoke()
    fun onPauseCallback() = onPause?.invoke()
    fun onSkipToNextCallback() = onSkipToNext?.invoke()
    fun onSkipToPreviousCallback() = onSkipToPrevious?.invoke()
    fun onSeekToCallback(position: Long) = onSeekTo?.invoke(position)

    fun appMediaSessionCompatCallback(): MediaSessionCompat.Callback {
        return object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                onPlayCallback()
            }

            override fun onPause() {
                super.onPause()
                onPauseCallback()
            }

            override fun onStop() {
                super.onStop()
                onStopCallback()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                onSeekToCallback(pos)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                onSkipToNextCallback()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                onSkipToPreviousCallback()
            }
        }
    }

    fun appMediaSessionCallback(): MediaSession.Callback {
        return object : MediaSession.Callback() {
            override fun onPlay() {
                super.onPlay()
                onPlayCallback()
            }

            override fun onPause() {
                super.onPause()
                onPauseCallback()
            }

            override fun onStop() {
                super.onStop()
                onStopCallback()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)
                onSeekToCallback(pos)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                onSkipToNextCallback()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                onSkipToPreviousCallback()
            }
        }
    }

    internal class Builder {
        private val amsc = AppMediaSessionCallback()

        fun withOnPlayCallback(callback: () -> Unit): Builder {
            amsc.onPlay = callback
            return this
        }

        fun withOnPauseCallback(callback: () -> Unit): Builder {
            amsc.onPause = callback
            return this
        }

        fun withOnStopCallback(callback: () -> Unit): Builder {
            amsc.onStop = callback
            return this
        }

        fun withOnSkipToNextCallback(callback: () -> Unit): Builder {
            amsc.onSkipToNext = callback
            return this
        }

        fun withOnSkipToPreviousCallback(callback: () -> Unit): Builder {
            amsc.onSkipToPrevious = callback
            return this
        }

        fun withOnSeekToCallback(callback: (position: Long) -> Unit): Builder {
            amsc.onSeekTo = callback
            return this
        }

        fun build(): AppMediaSessionCallback {
            return amsc
        }
    }
}