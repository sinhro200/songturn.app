package com.sinhro.songturn.app.model

import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.storage.StorageService
import com.sinhro.songturn.rest.model.*

object ApplicationData {
    private lateinit var storageService: StorageService

    var access_token: String = ""
        set(value) {
            if (value != field)
                storageService.accessToken = value
            field = value
        }

    val access_token_ref: (() -> String) = fun() = access_token
    val room_token_ref: (() -> String) = fun() = roomInfo?.roomToken ?: ""
    val playlist_title_ref: (() -> String) = fun() = playlistInfo?.title ?: ""

    var roomInfo: RoomInfo? = null
        set(value) {
            if (value != field)
                storageService.roomInfo = value
            field = value
        }
    var userInfo: FullUserInfo? = null
    var playlistInfo: PlaylistInfo? = null

    var songsAlreadyPlayed: List<SongInfo> = listOf()
    var songCurrent: SongInfo? = null
    var songsInQueue: List<SongInfo> = listOf()

    var usersInRoom: List<PublicUserInfo> = listOf()

    var onCompleteInPlayerServiceCallback: (() -> Boolean) = fun() = false

    var isInListenMode = false

    fun initStorage(storage: Storage) {
        this.storageService = StorageService(storage)
    }

    fun loadFromStorage(storageService: StorageService = this.storageService) {
        storageService.apply {
            this@ApplicationData.access_token = accessToken ?: ""
            this@ApplicationData.roomInfo = roomInfo
        }
    }

    fun saveToStorage(storageService: StorageService = this.storageService) {
        storageService.apply {
            accessToken = this@ApplicationData.access_token
            roomInfo = this@ApplicationData.roomInfo
        }
    }
}