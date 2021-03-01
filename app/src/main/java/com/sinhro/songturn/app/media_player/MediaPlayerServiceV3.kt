package com.sinhro.songturn.app.media_player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.*
import android.util.Log
import com.sinhro.songturn.app.media_player.media_session.AppMediaSessionCallback
import com.sinhro.songturn.app.media_player.media_session.AppMediaSessionHelper
import com.sinhro.songturn.app.media_player.notification.AppNotificationManagerFactory
import com.sinhro.songturn.app.media_player.notification.IAppNotificationManager
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.repositories.PlaylistRepositorySimple
import com.sinhro.songturn.rest.model.SongInfo


class MediaPlayerServiceV3 : Service(),
    AudioManager.OnAudioFocusChangeListener {

    private var mMediaSessionHelper: AppMediaSessionHelper? = null

    private lateinit var mAppPlayer: AppMusicPlayer
    private lateinit var mAppAudioFocusManager: AppAudioFocusRequestManager
    private lateinit var mIncomingCallsHandler: AppHandlerIncomingCalls
    private lateinit var mIAppNotificationManager: IAppNotificationManager
    private lateinit var mAppBroadcastReceiversManager: AppBroadcastReceiversManager

    private val iBinder: IBinder = LocalBinder()

    private var activeAudio: SongInfo? = null


    private fun initMPlayer() {
        mAppPlayer.init(this)
        mAppPlayer.reset()
        songChanged()
    }

    private fun songChanged() {
        mMediaSessionHelper?.updateSongMetadata(activeAudio)
        mIAppNotificationManager.songChanged(activeAudio)
        activeAudio?.link?.let {
            Log.d(
                "Media player service",
                "set song to ${activeAudio.toString()} and prepare with play"
            )
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
                    mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
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

    override fun onCreate() {
        super.onCreate()

        mAppPlayer = AppMusicPlayer().also {
            it.setOnComplete {
                val successfullyInvoked =
                    ApplicationData.onCompleteInPlayerServiceCallback.invoke()
                if (!successfullyInvoked) {
                    Log.d(
                        "OnCompleteCallback",
                        "On complete in application data invoked with false. " +
                                "Creating playlist repo in player service and " +
                                "refreshing playlist data"
                    )
                    PlaylistRepositorySimple()
                        .playlistSongs()
                        .withOnSuccessCallback {
                            ApplicationData.songsInQueue = it.playlistSongs.songsInQueue
                            ApplicationData.songCurrent = ApplicationData.songsInQueue[0]
                            mAppPlayer.reset()
                            songChanged()
                        }
                        .withOnErrorCallback {
                            mAppPlayer.stopMedia()
                            this.stopSelf()
                        }
                        .run()
                }
            }
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

        mIAppNotificationManager =
            AppNotificationManagerFactory.buildNotificationManager().also {
                it.init(this)
            }

        mAppBroadcastReceiversManager = AppBroadcastReceiversManager().also {
            it.addReceiver(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY,
                { c, i ->
                    pausePlayer()
                })
                .addReceiver(
                    Broadcast_PLAY_NEW_AUDIO,
                    { c, i ->
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

                        mIAppNotificationManager.notify(PlaybackStatus.PLAYING)

                    })
                .addReceiver(Broadcast_PAUSE_RESUME_AUDIO,
                    {c,i->
                        if (mAppPlayer.playing) {
                        mAppPlayer.pauseMedia()
                        mIAppNotificationManager.notify(PlaybackStatus.PAUSED)
                    } else {
                        mAppPlayer.playMedia()
                        mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
                    }
                    })
                .addReceiver(Broadcast_CLOSE_AUDIO,
                    {c,i->
                        Log.d(
                            this@MediaPlayerServiceV3::class.java.simpleName,
                            "close audio intent"
                        )
                        mIAppNotificationManager.removeNotification()
                        mAppPlayer.stopMedia()
                        this@MediaPlayerServiceV3.stopSelf()
                    })


        }

        mAppBroadcastReceiversManager.registerReceivers(this)
    }

    fun pausePlayer() {
        mAppPlayer.pauseMedia()
        mIAppNotificationManager.notify(PlaybackStatus.PAUSED)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(this::class.java.simpleName, "on destroy")
        if (mAppPlayer.initialized) {
            mAppPlayer.stopMedia()
            mAppPlayer.release()
        }
        mAppAudioFocusManager.removeAudioFocus()

        mIncomingCallsHandler.disable()

        mIAppNotificationManager.removeNotification()

        mAppBroadcastReceiversManager.unregisterReceivers(this)
    }

    private val appMediaSessionCallback: AppMediaSessionCallback =
        AppMediaSessionCallback.Builder()
            .withOnPlayCallback {
                Log.d("Media session", "playing")
                mAppPlayer.resumeMedia()
                mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
            }
            .withOnPauseCallback {
                Log.d("Media session", "paused")
                mAppPlayer.pauseMedia()
                mIAppNotificationManager.notify(PlaybackStatus.PAUSED)
            }
/*            .withOnStopCallback {
                Log.d("Media session", "stopped")
                mIAppNotificationManager.removeNotification()
                stopSelf()
            }
            .withOnSkipToNextCallback {
                Log.d("Media session","skippedToNext")
                skipToNext()
                mMediaSessionHelper?.updateSongMetadata(activeAudio)
                mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
            }
            .withOnSkipToPreviousCallback  {
                Log.d("Media session","skippedToPrevious")
                skipToPrevious()
                mMediaSessionHelper?.updateSongMetadata(activeAudio)
                mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
            }*/
            .build()


    @Throws(RemoteException::class)
    private fun initMediaSession() {
//        if (mMediaSessionManager != null) return  //mediaSessionManager exists
        mMediaSessionHelper = AppMediaSessionHelper()
            .also {
                it.init(this, appMediaSessionCallback)
                it.updateSongMetadata(activeAudio)
                mIAppNotificationManager.initSession(mMediaSessionHelper?.sessionToken())
            }

    }

    private fun skipToNext() {
//        if (audioIndex === audioList.size() - 1) {
//            activeAudio = audioList.get(audioIndex)
//        } else {
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

    //When called startService() from activity
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (ApplicationData.songCurrent != null) {
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
            mIAppNotificationManager.notify(PlaybackStatus.PLAYING)
        }

        mMediaSessionHelper?.handleIncomingActions(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    inner class LocalBinder : Binder() {
        val service: MediaPlayerServiceV3
            get() = this@MediaPlayerServiceV3
    }
}