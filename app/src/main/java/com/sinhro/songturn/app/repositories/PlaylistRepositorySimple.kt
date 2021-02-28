package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.request_response.*

class PlaylistRepositorySimple
    : PlaylistRepository(ApplicationData.access_token) {
    private val roomToken: String
        get() = ApplicationData.room_token_ref.invoke()
    private val playlistTitle: String
        get() = ApplicationData.playlist_title_ref.invoke()

    fun playlistSongs(): AppRequestBuilder<PlaylistSongsReqData, PlaylistSongsRespBody> =
        super.playlistSongs(PlaylistSongsReqData(roomToken, playlistTitle))

    fun playlistSongsVoted(): AppRequestBuilder<PlaylistSongsReqData, PlaylistSongsVotedRespBody> =
        super.playlistSongsVoted(PlaylistSongsReqData(roomToken, playlistTitle))

    fun currentPlayingSong() =
        super.currentPlayingSong(CurrentPlayingSongReqData(roomToken, playlistTitle))

    fun setCurrentPlayingSong(songId: Int) =
        super.setCurrentPlayingSong(SetCurrentPlayingSongReqData(roomToken, playlistTitle, songId))

    fun voteForSong(songId: Int, action: Int) =
        super.voteForSong(VoteForSongReqData(roomToken, playlistTitle, songId, action))

    fun orderSong(songLink: String, musicServiceAuthInfo: String = "") =
        super.orderSong(
            OrderSongReqData(
                roomToken,
                playlistTitle,
                songLink,
                musicServiceAuthInfo
            )
        )

    fun wannaListen() =
        super.wannaListen(ListenPlaylistReqData(roomToken,playlistTitle))

    fun stopListen() =
        super.stopListen(StopListenPlaylistReqData(roomToken,playlistTitle))
}