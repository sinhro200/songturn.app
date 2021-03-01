package com.sinhro.songturn.app.media_player

const val Broadcast_PLAY_NEW_AUDIO = "com.sinhro.songturn.app.media_player.PlayNewAudio"
const val Broadcast_PAUSE_RESUME_AUDIO = "com.sinhro.songturn.app.media_player.PauseAudio"
const val Broadcast_CLOSE_PLAYER = "com.sinhro.songturn.app.media_player.CloseAudio"

const val Broadcast_CLOSE_NOTIFICATION = "com.sinhro.songturn.app.media_player.CloseNotification"

const val ACTION_PLAY = "com.sinhro.songturn.app.media_player.ACTION_PLAY"
const val ACTION_PAUSE = "com.sinhro.songturn.app.media_player.ACTION_PAUSE"
const val ACTION_PREVIOUS = "com.sinhro.songturn.app.media_player.ACTION_PREVIOUS"
const val ACTION_NEXT = "com.sinhro.songturn.app.media_player.ACTION_NEXT"
const val ACTION_STOP = "com.sinhro.songturn.app.media_player.ACTION_STOP"


enum class PlaybackStatus {
    PLAYING, PAUSED
}