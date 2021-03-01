package com.sinhro.songturn.app.media_player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import java.io.IOException

class AppMusicPlayer :
    MediaPlayer.OnCompletionListener,
//    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener
//    MediaPlayer.OnSeekCompleteListener,
//    MediaPlayer.OnInfoListener,
//    MediaPlayer.OnBufferingUpdateListener
{

    var mediaPlayer: MediaPlayer? = null

    private var resumePosition: Int = 0
    var onCompletionCallback: (() -> Unit)? = null

    val playing: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    val initialized: Boolean
        get() = mediaPlayer != null

    fun init(context: Context) {
        mediaPlayer = MediaPlayer().also { mediaPlayer ->
            Log.i("AppMusicPlayer", "Created media player : $mediaPlayer")
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)

            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
//            mediaPlayer.setOnPreparedListener(this)
//            mediaPlayer.setOnBufferingUpdateListener(this)
//            mediaPlayer.setOnSeekCompleteListener(this)
//            mediaPlayer.setOnInfoListener(this)
            //Reset so that the MediaPlayer is not pointing to another data source
//            mediaPlayer.reset()
            //##deprecated
//        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
    }

    /**
     * reset()
     * ...
     * setDatasource()
     * prepare()
     *
     *  ### again
     */
    fun reset() {
        mediaPlayer?.reset()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @Throws(IOException::class)
    fun setDataSource(link: String) {
        /*try {*/

        mediaPlayer?.setDataSource(link)

        /*} catch (e: IOException) {
            e.printStackTrace()
            mediaPlayer?.stopSelf()
        }*/
    }

    fun prepareAndPlay(whenPlay: (() -> Unit)?=null) {
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener {
            playMedia()
            whenPlay?.invoke()
        }
    }


    fun playMedia() {
        Log.i("LifecycleMediaPlayer", "playMedia")
        mediaPlayer?.let {
            if (!it.isPlaying)
                it.start()
        }
    }

    fun stopMedia() {
        Log.i("LifecycleMediaPlayer", "stopMedia")
        mediaPlayer?.let {
            if (it.isPlaying)
                it.stop()
        }

    }

    fun pauseMedia() {
        Log.i("LifecycleMediaPlayer", "pauseMedia")
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
            }
        }
    }

    fun becomeQuiet() {
        mediaPlayer?.setVolume(0.1f, 0.1f)
    }

    fun becomeNormal() {
        mediaPlayer?.setVolume(1.0f, 1.0f)
    }

    fun resumeMedia() {
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.seekTo(resumePosition)
                mp.start()
//                mp.setOnSeekCompleteListener {
//                    mp.start()
//                }
            }
        }
    }


    override fun onCompletion(mp: MediaPlayer?) {
        this.onCompletionCallback?.invoke()
//        stopMedia()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.i("LifecycleAppMusicPlayer", "onError")
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
}