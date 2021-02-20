package com.sinhro.songturn.app.media_player

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sinhro.songturn.app.media_player.media_session.AppMediaSessionCallback
import com.sinhro.songturn.app.media_player.media_session.AppMediaSessionHelperLegacy
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.model.SongInfo

/**
 * To use it you should add this service into manifest
 */
class MediaPlayerServiceV2 : Service(),
    AudioManager.OnAudioFocusChangeListener {

    private var mMediaSessionHelper: AppMediaSessionHelperLegacy? = null

    private lateinit var mAppPlayer: AppMusicPlayer
    private lateinit var mAppAudioFocusManager: AppAudioFocusRequestManager
    private lateinit var mIncomingCallsHandler: AppHandlerIncomingCalls

    private val NOTIFICATION_ID = 101
    private val NOTIFICATION_CHANNEL_ID = "songturn_notification_channel"

    private val iBinder: IBinder = LocalBinder()

    private var activeAudio: SongInfo? = null


    private fun initMPlayer() {
        mAppPlayer.init(applicationContext)
        mAppPlayer.reset()
        songChanged()
    }

    private fun songChanged() {
        activeAudio?.link?.let {
            mAppPlayer.setDataSource(it)
            mAppPlayer.prepareAndPlay()
        }
    }

    /*override fun onCompletion(mp: MediaPlayer?) {
        Log.i("LifecycleMediaPlayer", "onCompletion")
        //Invoked when playback of a media source has completed.
        stopMedia()
        //stop the service
        stopSelf()
    }*/

    override fun onAudioFocusChange(focusChange: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback

                if (!mAppPlayer.initialized) {
                    initMPlayer()
                } else if (!mAppPlayer.playing) {
                    mAppPlayer.playMedia()
                }
                mAppPlayer.becomeNormal()
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                mAppPlayer.stopMedia()
                mAppPlayer.release()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mAppPlayer.playing)
                    mAppPlayer.pauseMedia()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mAppPlayer.playing)
                    mAppPlayer.becomeQuiet()
        }
    }

    //Becoming noisy
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            mAppPlayer.pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("Media player", "Play new audio")

            if (ApplicationData.songCurrent != null)
                activeAudio = ApplicationData.songCurrent
            else
                stopSelf()


            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            mAppPlayer.stopMedia()
            mAppPlayer.reset()
            songChanged()
//            initMPlayer()

            mMediaSessionHelper?.updateSongMetadata(activeAudio)

            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    override fun onCreate() {
        super.onCreate()

        mAppPlayer = AppMusicPlayer().also {
            it.init(this)
        }
        mAppAudioFocusManager = AppAudioFocusRequestManager().also {
            it.init(this)
        }

        mIncomingCallsHandler = AppHandlerIncomingCalls().also {
            it.init(this,
                onIncomingCall = {
                    if (mAppPlayer.initialized)
                        mAppPlayer.pauseMedia()
                },
                onHangup = {
                    if (mAppPlayer.initialized) {
                        mAppPlayer.resumeMedia()
                    }
                })
        }

        //ACTION_AUDIO_BECOMING_NOISY
        // -- change in audio outputs
        // -- BroadcastReceiver
        registerBecomingNoisyReceiver()

        //Listen for new Audio to play
        // -- BroadcastReceiver
        register_playNewAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mAppPlayer.initialized) {
            mAppPlayer.stopMedia()
            mAppPlayer.release()
        }
        mAppAudioFocusManager.removeAudioFocus()

        mIncomingCallsHandler.disable()

        removeNotification()

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }

    private val appMediaSessionCallback: AppMediaSessionCallback =
        AppMediaSessionCallback.Builder()
            .withOnPlayCallback {
                mAppPlayer.resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }
            .withOnPauseCallback {
                mAppPlayer.pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }
            .withOnStopCallback {
                removeNotification()
                stopSelf()
            }
            .withOnSkipToNextCallback {
                skipToNext()
                mMediaSessionHelper?.updateSongMetadata(activeAudio)
                buildNotification(PlaybackStatus.PLAYING)
            }
            .build()


    @Throws(RemoteException::class)
    private fun initMediaSession() {
//        if (mMediaSessionManager != null) return  //mediaSessionManager exists
        mMediaSessionHelper = AppMediaSessionHelperLegacy()
            .also {
                it.init(this, appMediaSessionCallback)
                it.updateSongMetadata(activeAudio)
            }

    }

    private fun skipToNext() {
//        if (audioIndex === audioList.size() - 1) {
//            //if last in playlist
//            audioIndex = 0
//            activeAudio = audioList.get(audioIndex)
//        } else {
//            //get next in playlist
//            activeAudio = audioList.get(++audioIndex)
//        }

        mAppPlayer.stopMedia()
        //reset mediaPlayer
        mAppPlayer.reset()
        songChanged()
//        initMPlayer()
    }

    private fun skipToPrevious() {
//        if (audioIndex === 0) {
//            //if first in playlist
//            //set index to the last of audioList
//            audioIndex = audioList.size() - 1
//            activeAudio = audioList.get(audioIndex)
//        } else {
//            //get previous in playlist
//            activeAudio = audioList.get(--audioIndex)
//        }

        mAppPlayer.stopMedia()
        //reset mediaPlayer
        mAppPlayer.reset()
        songChanged()
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {
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
        val largeIcon: Bitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_media_ff
        ) //replace with your own image

        // Create a new Notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false)
                // Set the Notification style
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mMediaSessionHelper?.sessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2)
                ) // Set the Notification color
//            .setColor(
//                resources.getColor(R.color.colorPrimary)) // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.stat_sys_headset) // Set Notification content information
                .setContentText(activeAudio?.artist)
                .setContentTitle(activeAudio?.title)
                .setContentInfo(activeAudio?.title) // Add playback actions
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(
                    R.drawable.ic_media_next,
                    "next",
                    playbackAction(2)
                )

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = createNotificationChannel()
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }

        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)

        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun removeNotification() {
        Log.i("AdditionalMethods", "removeNotification")
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerServiceV2::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

//    private fun handleIncomingActions(playbackAction: Intent?) {
//        if (playbackAction == null || playbackAction.action == null) return
//        val actionString = playbackAction.action
//        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
//            transportControls?.play()
//        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
//            transportControls?.pause()
//        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
//            transportControls?.skipToNext()
//        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
//            transportControls?.skipToPrevious()
//        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
//            transportControls?.stop()
//        }
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (ApplicationData.songCurrent != null) {
                //index is in a valid range
                activeAudio = ApplicationData.songCurrent
            } else {
                stopSelf()
            }
        } catch (e: NullPointerException) {
            stopSelf()
        }

        //Request audio focus
        if (!mAppAudioFocusManager.requestAudioFocus(this)) {
            //Could not gain focus
            stopSelf()
        }
        if (mMediaSessionHelper == null) {
            try {
                initMediaSession()
                if (!mAppPlayer.initialized)
                    initMPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            songChanged()
            buildNotification(PlaybackStatus.PLAYING)
        }

        mMediaSessionHelper?.handleIncomingActions(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerServiceV2
            get() = this@MediaPlayerServiceV2
    }
}