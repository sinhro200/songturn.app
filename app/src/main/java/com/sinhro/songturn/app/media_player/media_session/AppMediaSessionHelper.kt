package com.sinhro.songturn.app.media_player.media_session

import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.getSystemService
import androidx.media.MediaSessionManager
import com.sinhro.songturn.app.media_player.*
import com.sinhro.songturn.rest.model.SongInfo

class AppMediaSessionHelper(
    private val sessionTag: String = "songturn_mediasession_tag"
) {
    private lateinit var mMediaSession: MediaSession
    private lateinit var mediaController: MediaController
    private var mMediaSessionManager: MediaSessionManager? = null
    fun init(context: Context, callback: AppMediaSessionCallback) {
        //IDK ### IDE did not allows to do like next line
//        (getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager)
        mMediaSessionManager =
            context.getSystemService<MediaSessionManager>()

        // Create a new MediaSession
        mMediaSession = MediaSession(context, sessionTag)

        //Get MediaSessions transport controls
        mediaController = mMediaSession.controller
        //set MediaSession -> ready to receive media commands
        mMediaSession.isActive = true

//        mMediaSession.setFlags(
//            MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
//                    and MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
//        )

        // Attach Callback to receive MediaSession updates
        mMediaSession.setCallback(callback.appMediaSessionCallback())

        mMediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY_PAUSE and PlaybackState.ACTION_SKIP_TO_NEXT and
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS
                )
                .build()
        )
    }

    fun updateSongMetadata(songInfo: SongInfo?) {

//        val albumArt = BitmapFactory.decodeResource(
//            resources,
//            R.drawable.image
//        ) //replace with medias albumArt
        // Update the current metadata
        mMediaSession.setMetadata(
            MediaMetadata.Builder()
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, songInfo?.artist)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadata.METADATA_KEY_TITLE, songInfo?.title)
                .build()
        )

    }

    fun sessionToken(): MediaSession.Token {
        return mMediaSession.sessionToken
    }

    fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action

        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            mediaController.transportControls.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            mediaController.transportControls.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            mediaController.transportControls.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            mediaController.transportControls.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            mediaController.transportControls.stop()
        }
    }
}