package com.sinhro.songturn.app.media_player.notification

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.sinhro.songturn.app.media_player.PlaybackStatus
import com.sinhro.songturn.rest.model.SongInfo

class AppNotificationManagerFactory {
    companion object{
        fun buildNotificationManager() : IAppNotificationManager {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                build()
            } else {
                buildLegacy()
            }
        }

        fun buildLegacy(): AppNotificationManagerLegacy {
            return AppNotificationManagerLegacy()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun build(): AppNotificationManager {
            return AppNotificationManager()
        }
    }
}