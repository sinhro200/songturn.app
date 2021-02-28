package com.sinhro.songturn.app.api.services

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import com.sinhro.songturn.rest.request_response.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class PlaylistService(
    bearerToken: String
) {

    interface PlaylistAPI {
        @POST("/playlist/getsongs")
        fun getSongs(
            @Body getPlaylistsReqData: PlaylistSongsReqData
        ): Observable<Response<PlaylistSongsRespBody>>

        @POST("/playlist/getsongsvoted")
        fun getSongsVoted(
            @Body getPlaylistsReqData: PlaylistSongsReqData
        ): Observable<Response<PlaylistSongsVotedRespBody>>

        @POST("/playlist/ordersong")
        fun orderSong(
            @Body orderSongReqData: OrderSongReqData
        ): Observable<Response<OrderSongRespBody>>

        @POST("/playlist/currentPlayingSong")
        fun getCurrentPlayingSong(
            @Body currentPlayingSongReqData: CurrentPlayingSongReqData
        ): Observable<Response<CurrentPlayingSongRespBody>>

        @POST("/playlist/voteforsong")
        fun voteForSong(
            @Body voteForSongReqData: VoteForSongReqData
        ): Observable<Response<VoteForSongRespBody>>

        @POST("/playlist/stoplisten")
        fun stopListenSong(
            @Body stopListenPlaylistRespBody: StopListenPlaylistReqData
        ): Observable<Response<StopListenPlaylistRespBody>>

        @POST("/playlist/wannalisten")
        fun wannaListenSong(
            @Body listenPlaylistReqData: ListenPlaylistReqData
        ): Observable<Response<ListenPlaylistRespBody>>

        @POST("/playlist/setCurrentPlayingSong")
        fun setCurrentPlayingSong(
            @Body setCurrentPlayingSongReqData: SetCurrentPlayingSongReqData
        ): Observable<Response<SetCurrentPlayingSongRespBody>>
    }

    private val playlistApi =
        AppRetrofitProvider.AuthorizedInstance(bearerToken)
            .create(PlaylistAPI::class.java)

    val playlistSongs = AppNetService.Builder<PlaylistSongsReqData, PlaylistSongsRespBody>()
        .observableFrom { playlistApi.getSongs(it) }
        .build()

    val playlistSongsVoted = AppNetService.Builder<PlaylistSongsReqData, PlaylistSongsVotedRespBody>()
        .observableFrom { playlistApi.getSongsVoted(it) }
        .build()

    val orderSong = AppNetService.Builder<OrderSongReqData, OrderSongRespBody>()
        .observableFrom { playlistApi.orderSong(it) }
        .build()

    val getCurrentPlayingSong =
        AppNetService.Builder<CurrentPlayingSongReqData, CurrentPlayingSongRespBody>()
            .observableFrom { playlistApi.getCurrentPlayingSong(it) }
            .build()

    val setCurrentPlayingSong =
        AppNetService.Builder<SetCurrentPlayingSongReqData, SetCurrentPlayingSongRespBody>()
            .observableFrom { playlistApi.setCurrentPlayingSong(it) }
            .build()

    val voteForSong = AppNetService.Builder<VoteForSongReqData, VoteForSongRespBody>()
        .observableFrom { playlistApi.voteForSong(it) }
        .build()

    val stopListen = AppNetService.Builder<StopListenPlaylistReqData, StopListenPlaylistRespBody>()
        .observableFrom { playlistApi.stopListenSong(it) }
        .build()

    val wannaListen = AppNetService.Builder<ListenPlaylistReqData, ListenPlaylistRespBody>()
        .observableFrom { playlistApi.wannaListenSong(it) }
        .build()
}