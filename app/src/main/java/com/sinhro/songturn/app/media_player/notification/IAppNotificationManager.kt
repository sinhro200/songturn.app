package com.sinhro.songturn.app.media_player.notification

import android.content.Context
import com.sinhro.songturn.app.media_player.PlaybackStatus
import com.sinhro.songturn.rest.model.SongInfo

interface IAppNotificationManager {
    fun notify(playbackStatus: PlaybackStatus)
    fun removeNotification()
    fun songChanged(songInfo: SongInfo?)
    fun initSession(sessionToken: Any?)
    fun init(context: Context)
}