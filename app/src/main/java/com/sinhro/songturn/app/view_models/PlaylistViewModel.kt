package com.sinhro.songturn.app.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.repositories.AppRequestBuilder
import com.sinhro.songturn.app.repositories.PlaylistRepositorySimple
import com.sinhro.songturn.rest.model.PlaylistSongs
import com.sinhro.songturn.rest.model.PlaylistSongsVoted
import com.sinhro.songturn.rest.model.SongInfo
import com.sinhro.songturn.rest.model.SongInfoVoted
import com.sinhro.songturn.rest.request_response.*

class PlaylistViewModel : ViewModel() {

    private val mainPlaylistRepository: PlaylistRepositorySimple by lazy {
        PlaylistRepositorySimple()
    }

    val playlistSongsMutableLiveData: MutableLiveData<PlaylistSongs> by lazy {
        MutableLiveData<PlaylistSongs>()
    }

    val playlistSongsVotedMutableLiveData: MutableLiveData<PlaylistSongsVoted> by lazy {
        MutableLiveData<PlaylistSongsVoted>()
    }

    val currentListeningSongMutableLiveData: MutableLiveData<SongInfo> by lazy {
        MutableLiveData<SongInfo>()
    }

    fun updatePlaylistSongs()
            : AppRequestBuilder<PlaylistSongsReqData, PlaylistSongsRespBody> {
        return mainPlaylistRepository.playlistSongs()
            .withOnSuccessSaveDataCallback {
                playlistSongsMutableLiveData.postValue(it.playlistSongs)
                currentListeningSongMutableLiveData.postValue(it.playlistSongs.currentSong)
            }
            .withErrorCollectorViewModel()
    }

    fun updatePlaylistSongsVoted()
            : AppRequestBuilder<PlaylistSongsReqData, PlaylistSongsVotedRespBody> {
        return mainPlaylistRepository.playlistSongsVoted()
            .withOnSuccessSaveDataCallback {
                ApplicationData.songsInQueue = it.playlistSongsVoted.songsInQueue.map { it.songInfo }
                ApplicationData.songCurrent = it.playlistSongsVoted.currentSong?.songInfo
                ApplicationData.songsAlreadyPlayed = it.playlistSongsVoted.songsInQueue.map { it.songInfo }

                playlistSongsVotedMutableLiveData.postValue(it.playlistSongsVoted)
                currentListeningSongMutableLiveData.postValue(it.playlistSongsVoted.currentSong?.songInfo)
            }
            .withErrorCollectorViewModel()
    }

    fun updateCurrentPlayingSong()
            : AppRequestBuilder<CurrentPlayingSongReqData, CurrentPlayingSongRespBody> {
        return mainPlaylistRepository.currentPlayingSong()
            .withOnSuccessSaveDataCallback {
                currentListeningSongMutableLiveData.postValue(it.songInfo)
            }
            .withErrorCollectorViewModel()
    }


    fun setCurrentPlayingSong(songId: Int)
            : AppRequestBuilder<SetCurrentPlayingSongReqData, SetCurrentPlayingSongRespBody> {
        return mainPlaylistRepository.setCurrentPlayingSong(songId)
            .withErrorCollectorViewModel()
    }

    fun voteForSong(
        songId: Int,
        action: Int
    ): AppRequestBuilder<VoteForSongReqData, VoteForSongRespBody> {
        return mainPlaylistRepository.voteForSong(songId, action)
            .withErrorCollectorViewModel()
    }

    fun orderSong(songLink: String): AppRequestBuilder<OrderSongReqData, OrderSongRespBody> {
        return mainPlaylistRepository.orderSong(songLink)
            .withErrorCollectorViewModel()
    }
}