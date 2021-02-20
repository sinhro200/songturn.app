package com.sinhro.songturn.app.media_player.notification

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sinhro.songturn.app.R as Songturn_R
import com.sinhro.songturn.app.media_player.*
import com.sinhro.songturn.rest.model.SongInfo

class AppNotificationManagerLegacy  : IAppNotificationManager {
    private lateinit var context: Context
    private var sessionToken: MediaSessionCompat.Token? = null

    private val NOTIFICATION_ID = 101
    private val NOTIFICATION_CHANNEL_ID = "songturn_notification_channel"

    private var audio: SongInfo? = null

    override fun init(context: Context) {
        this.context = context
    }

    override fun initSession(sessionToken: Any?) {
        this.sessionToken = sessionToken as MediaSessionCompat.Token?
    }

    override fun songChanged(songInfo: SongInfo?) {
        this.audio = songInfo
    }


    override fun notify(
        playbackStatus: PlaybackStatus
    ) {
        var notificationAction = R.drawable.ic_media_pause
        //needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        // Create a new Notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false)
                // Set the Notification style
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(sessionToken)
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0)
                ) // Set the Notification color
//            .setColor(
//                resources.getColor(R.color.colorPrimary)) // Set the large and small icons
                .setSmallIcon(R.drawable.stat_sys_headset)
                .setContentText(audio?.artist)
                .setContentTitle(audio?.title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .addAction(R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
//                .addAction(
//                    R.drawable.ic_media_next,
//                    "next",
//                    playbackAction(2)
//                )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)

        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }


    override fun removeNotification() {
        Log.i("AdditionalMethods", "removeNotification")
        val notificationManager: NotificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(context, MediaPlayerServiceV3::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(context, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }
}