package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.api.services.PlaylistService
import com.sinhro.songturn.rest.request_response.*

open class PlaylistRepository(
    bearerToken: String
) {
    private val ps: PlaylistService by lazy {
        PlaylistService(bearerToken)
    }

    fun playlistSongs(playlistSongsReqData: PlaylistSongsReqData)
            : AppRequestBuilder<PlaylistSongsReqData,PlaylistSongsRespBody> {
        return AppRequestBuilder(ps.playlistSongs, playlistSongsReqData)
    }

    fun playlistSongsVoted(playlistSongsReqData: PlaylistSongsReqData)
            : AppRequestBuilder<PlaylistSongsReqData,PlaylistSongsVotedRespBody> {
        return AppRequestBuilder(ps.playlistSongsVoted, playlistSongsReqData)
    }

    fun orderSong(orderSongReqData: OrderSongReqData)
            : AppRequestBuilder<OrderSongReqData, OrderSongRespBody> {
        return AppRequestBuilder(ps.orderSong, orderSongReqData)
    }

    fun currentPlayingSong(currentPlayingSongReqData: CurrentPlayingSongReqData)
            : AppRequestBuilder<CurrentPlayingSongReqData, CurrentPlayingSongRespBody> {
        return AppRequestBuilder(ps.getCurrentPlayingSong, currentPlayingSongReqData)
    }

    fun setCurrentPlayingSong(setCurrentPlayingSongReqData: SetCurrentPlayingSongReqData)
            : AppRequestBuilder<SetCurrentPlayingSongReqData, SetCurrentPlayingSongRespBody> {
        return AppRequestBuilder(ps.setCurrentPlayingSong, setCurrentPlayingSongReqData)
    }

    fun voteForSong(voteForSongReqData: VoteForSongReqData)
            : AppRequestBuilder<VoteForSongReqData, VoteForSongRespBody> {
        return AppRequestBuilder(ps.voteForSong, voteForSongReqData)
    }
}