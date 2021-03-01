package com.sinhro.songturn.app.media_player.notification


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import android.R
import com.sinhro.songturn.app.media_player.*
import com.sinhro.songturn.rest.model.SongInfo


@RequiresApi(Build.VERSION_CODES.O)
class AppNotificationManager : IAppNotificationManager {

    private lateinit var context: Context
    private var sessionToken: MediaSession.Token? = null

    private val NOTIFICATION_ID = 101
    private val NOTIFICATION_CHANNEL_ID = "songturn_notification_channel"

    private var audio: SongInfo? = null
    private var notificationManager: NotificationManager? = null

    override fun init(context: Context) {
        this.context = context
    }

    override fun initSession(sToken: Any?) {
        sessionToken = sToken as MediaSession.Token?

        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val channel = createNotificationChannel()
        // Register the channel with the system
        notificationManager?.createNotificationChannel(channel)
    }

    override fun songChanged(audio: SongInfo?) {
        this.audio = audio
    }


    override fun notify(
        playbackStatus: PlaybackStatus
    ) {
        var notificationAction = R.drawable.ic_media_pause //needs to be initialized
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
        val notificationBuilder: Notification.Builder =
            Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false)
                // Set the Notification style
                .setStyle(
                    Notification.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(sessionToken)
                        // Show our playback controls in the compact notification view.
//                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowActionsInCompactView(0)
                ) // Set the Notification color
//            .setColor(
//                resources.getColor(R.color.colorPrimary)) // Set the large and small icons
                .setSmallIcon(R.drawable.stat_sys_headset) // Set Notification content information
                .setContentText(audio?.artist)
                .setContentTitle(audio?.title)
                // Add playback actions
//                .addAction(
//                    Notification.Action.Builder(
//                        Icon.createWithResource(context, R.drawable.ic_media_previous),
//                        "previous",
//                        playbackAction(3)
//                    )
//                        .build()
//                )
                .addAction(
                    Notification.Action.Builder(
                        Icon.createWithResource(context, notificationAction),
                        "pause",
                        play_pauseAction
                    )
                        .build()
                )
//                .addAction(
//                    Notification.Action.Builder(
//                        Icon.createWithResource(context, R.drawable.ic_media_next),
//                        "next",
//                        playbackAction(2)
//                    )
//                        .build()
//                )


        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)

        val closeAudioIntent = Intent(Broadcast_CLOSE_PLAYER)
        val onDismissPendingIntent =
            PendingIntent.getBroadcast(context, 0, closeAudioIntent, 0)

        notificationBuilder.setDeleteIntent(onDismissPendingIntent)

        notificationManager?.notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    private fun createNotificationChannel(): NotificationChannel {
        Log.i("AdditionalMethods", "createNotificationChannel")
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = NOTIFICATION_CHANNEL_ID //getString(R.string.channel_name)
        val descriptionText = NOTIFICATION_CHANNEL_ID //getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        return NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
            enableLights(false)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
    }

    override fun removeNotification() {
        Log.i("AdditionalMethods", "removeNotification")
        val notificationManager: NotificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
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