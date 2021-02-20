package com.sinhro.songturn.app.media_player.media_session

import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.getSystemService
import androidx.media.MediaSessionManager
import com.sinhro.songturn.app.media_player.*
import com.sinhro.songturn.rest.model.SongInfo

class AppMediaSessionHelperLegacy(
    private val sessionTag: String = "songturn_mediasession_tag"
) {
    private lateinit var mMediaSession: MediaSessionCompat
    private lateinit var transportControls: MediaControllerCompat.TransportControls
    private var mMediaSessionManager: MediaSessionManager? = null

    fun init(context: Context, callback: AppMediaSessionCallback) {
        if (mMediaSessionManager != null) return
        //IDK ### IDE did not allows to do like next line
//        (getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager)
        mMediaSessionManager =
            context.getSystemService<MediaSessionManager>()

        // Create a new MediaSession
        mMediaSession = MediaSessionCompat(context, sessionTag)

        //Get MediaSessions transport controls
        transportControls = mMediaSession.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mMediaSession.isActive = true

        mMediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    and MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
        )

        // Attach Callback to receive MediaSession updates
        mMediaSession.setCallback(callback.appMediaSessionCompatCallback())
    }

    fun updateSongMetadata(songInfo: SongInfo?) {

//        val albumArt = BitmapFactory.decodeResource(
//            resources,
//            R.drawable.image
//        ) //replace with medias albumArt
        // Update the current metadata
        mMediaSession.setMetadata(
            MediaMetadataCompat.Builder()
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songInfo?.artist)
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songInfo?.title)
                .build()
        )

    }

    fun sessionToken(): MediaSessionCompat.Token {
        return mMediaSession.sessionToken
    }

    fun handleIncomingActions(playbackAction: Intent?) {
        //###       Method 1
        //MediaButtonReceiver.handleIntent(mMediaSession, playbackAction)

        //###       Method 2
        if (playbackAction == null || playbackAction.action == null) return
        val actionString = playbackAction.action

        if (actionString.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            transportControls.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            transportControls.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls.stop()
        }
    }
}