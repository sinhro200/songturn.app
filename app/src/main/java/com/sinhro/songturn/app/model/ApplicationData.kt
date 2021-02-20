package com.sinhro.songturn.app.model

import com.sinhro.musicord.storage.Storage
import com.sinhro.songturn.app.storage.StorageService
import com.sinhro.songturn.rest.model.PublicUserInfo
import com.sinhro.songturn.rest.model.RoomSettings
import com.sinhro.songturn.rest.model.SongInfo

object ApplicationData {
    private lateinit var storageService: StorageService

    var access_token: String = ""
        set(value) {
            if (value != field)
                storageService.accessToken = value
            field = value
        }
    val access_token_ref: (() -> String) = fun() = access_token

    var room_token: String = ""
        set(value) {
            if (value != field)
                storageService.roomToken = value
            field = value
        }
    val room_token_ref: (() -> String) = fun() = room_token

    var playlist_title: String = ""
    val playlist_title_ref: (() -> String) = fun() = playlist_title

    var room_title: String = ""

    var roomSettings: RoomSettings = RoomSettings()

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
            this@ApplicationData.room_token = roomToken ?: ""
        }
    }

    fun saveToStorage(storageService: StorageService = this.storageService) {
        storageService.apply {
            accessToken = this@ApplicationData.access_token
            roomToken = this@ApplicationData.room_token
        }
    }
}