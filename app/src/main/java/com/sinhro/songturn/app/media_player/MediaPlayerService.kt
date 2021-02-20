package com.sinhro.songturn.app.media_player

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.media.MediaSessionManager
import androidx.media.session.MediaButtonReceiver
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.model.SongInfo
import java.io.IOException

/**
 * To use it you should add this service into manifest
 */
class MediaPlayerService : Service(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    //AudioPlayer notification ID
    private val NOTIFICATION_ID = 101
    private val NOTIFICATION_CHANNEL_ID = "songturn_notification_channel"

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    // Binder given to clients
    private val iBinder: IBinder = LocalBinder()

    //Used to pause/resume MediaPlayer
    private var resumePosition: Int = 0

    private var activeAudio: SongInfo? = null


    private fun initMediaPlayer() {
        Log.i("LifecycleMediaPlayer", "initMediaPlayer")
        mediaPlayer = MediaPlayer().also { mediaPlayer ->

            mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            //Set up MediaPlayer event listeners
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnBufferingUpdateListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setOnInfoListener(this)
            //Reset so that the MediaPlayer is not pointing to another data source
            mediaPlayer.reset()
            //##deprecated
//        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                // Set the data source to the mediaFile location
                mediaPlayer.setDataSource(activeAudio?.link)
                Log.i("LifecycleMediaPlayer", "set data source: ${activeAudio?.link}")
            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
            mediaPlayer.prepareAsync()
        }

    }

    private fun playMedia() {
        Log.i("LifecycleMediaPlayer", "playMedia")
        mediaPlayer?.let {
            if (!it.isPlaying)
                mediaPlayer?.start()
        }

    }

    private fun stopMedia() {
        Log.i("LifecycleMediaPlayer", "stopMedia")
        mediaPlayer?.let {
            if (it.isPlaying)
                it.stop()
        }

    }

    private fun pauseMedia() {
        Log.i("LifecycleMediaPlayer", "pauseMedia")
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
            }
        }

    }

    private fun resumeMedia() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.seekTo(resumePosition)
                it.start()
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.i("LifecycleMediaPlayer", "onCompletion")
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.i("LifecycleMediaPlayer", "onError")
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.i("LifecycleMediaPlayer", "onPrepared. MediaPlayer - ${mp.toString()}")
        //Invoked when the media source is ready for playback.
        playMedia()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback

                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying)
                    mediaPlayer?.start()

                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player

                if (mediaPlayer!!.isPlaying)
                    mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying)
                    mediaPlayer?.setVolume(0.1f, 0.1f)
        }
    }

    private var audioFocusRequest: AudioFocusRequest? = null

    private fun requestAudioFocus(): Boolean {
        val result: Int
        val audioFocusRequest: AudioFocusRequest
        audioManager = (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .also { audioMngr ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
//                        .setAcceptsDelayedFocusGain(true)
                        .build()
                    result = audioMngr.requestAudioFocus(audioFocusRequest)
                } else {
                    result = audioMngr.requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                }
            }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus(): Boolean {
        var abandonAudioFocusResult: Int? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                abandonAudioFocusResult = audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            abandonAudioFocusResult = audioManager?.abandonAudioFocus(this)
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == abandonAudioFocusResult
    }

    //Becoming noisy
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)

    }

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK,
                    TelephonyManager.CALL_STATE_RINGING ->
                        if (mediaPlayer != null) {
                            pauseMedia()
                            ongoingCall = true
                        }
                    TelephonyManager.CALL_STATE_IDLE ->                   // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager?.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    private val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (ApplicationData.songCurrent != null) {
                //index is in a valid range
                activeAudio = ApplicationData.songCurrent
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia()
            mediaPlayer?.reset()
            initMediaPlayer()
            updateMetaData()
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
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer?.release()
        }
        removeAudioFocus()
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return  //mediaSessionManager exists

        mediaSessionManager = getSystemService<MediaSessionManager>()
        //IDK ### IDE did not allows to do like next line
        //(getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager)

        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "tag1")
        //Get MediaSessions transport controls
        transportControls = mediaSession?.controller?.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession?.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession?.setFlags(
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    and MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        )

        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                Log.i("MediaSessionCallbacks", "onPlay")
                super.onPlay()
                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                Log.i("MediaSessionCallbacks", "onPause")
                super.onPause()
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                Log.i("MediaSessionCallbacks", "onNext")
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                Log.i("MediaSessionCallbacks", "onPrevious")
                super.onSkipToPrevious()
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                Log.i("MediaSessionCallbacks", "onStop")
                super.onStop()
                removeNotification()
                //Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun updateMetaData() {
//        val albumArt = BitmapFactory.decodeResource(
//            resources,
//            R.drawable.image
//        ) //replace with medias albumArt
        // Update the current metadata
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio?.artist)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio?.title)
                .build()
        )
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

        stopMedia()
        //reset mediaPlayer
        mediaPlayer?.reset()
        initMediaPlayer()
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

        stopMedia()
        //reset mediaPlayer
        mediaPlayer?.reset()
        initMediaPlayer()
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
            NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false)
                // Set the Notification style
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession!!.sessionToken)
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
        startForeground(NOTIFICATION_ID,notificationBuilder.build())

//        notificationManager.notify(
//            NOTIFICATION_ID,
//            notificationBuilder.build()
//        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): NotificationChannel {
        Log.i("AdditionalMethods","createNotificationChannel")
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = NOTIFICATION_CHANNEL_ID //getString(R.string.channel_name)
        val descriptionText = NOTIFICATION_CHANNEL_ID //getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        return NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(false)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

    }

    private fun removeNotification() {
        Log.i("AdditionalMethods","removeNotification")
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_ID)
        }
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, MediaPlayerService::class.java)
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

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action
        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls?.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls?.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls?.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls?.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls?.stop()
        }
    }

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
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }
        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            buildNotification(PlaybackStatus.PLAYING)
        }

        //Handle Intent action from MediaSession.TransportControls
        MediaButtonReceiver.handleIntent(mediaSession, intent)
//        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        return false
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        Log.i("LifecycleMediaPlayer", "onBufferingUpdate $percent")
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }
}