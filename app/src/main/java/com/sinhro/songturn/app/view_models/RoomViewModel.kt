package com.sinhro.songturn.app.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.repositories.AppRequestBuilder
import com.sinhro.songturn.app.repositories.RoomRepositorySimple
import com.sinhro.songturn.rest.model.PlaylistInfo
import com.sinhro.songturn.rest.model.PublicUserInfo
import com.sinhro.songturn.rest.model.RoomInfo
import com.sinhro.songturn.rest.model.RoomSettings
import com.sinhro.songturn.rest.request_response.*

class RoomViewModel : ViewModel() {

    private val roomRepository: RoomRepositorySimple by lazy {
        RoomRepositorySimple(
            ApplicationData.room_token_ref
        )
    }

    val roomLiveData: MutableLiveData<RoomInfo> by lazy {
        //if user not in room, and want create/enter one
        //in the appData will no token, that's why MLD without .also{} block
        MutableLiveData<RoomInfo>()
    }

    val usersInRoomLiveData: MutableLiveData<List<PublicUserInfo>> by lazy {
        MutableLiveData<List<PublicUserInfo>>().also {
            usersInRoom()
                .run()
        }
    }

    val playlistsInRoomLiveData: MutableLiveData<List<PlaylistInfo>> by lazy {
        MutableLiveData<List<PlaylistInfo>>().also {
            updatePlaylistsInRoom()
                .run()
        }
    }

    fun createRoom(createRoomReqData: CreateRoomReqData)
            : AppRequestBuilder<CreateRoomReqData, CreateRoomRespBody> {
        return roomRepository.createRoom(createRoomReqData)
            .withOnSuccessSaveDataCallback {
                ApplicationData.room_token = it.roomInfo.roomToken
                ApplicationData.room_title = it.roomInfo.title
                ApplicationData.roomSettings = it.roomInfo.roomSettings

                roomLiveData.postValue(it.roomInfo)
            }
    }

    fun enterRoom(enterRoomReqData: EnterRoomReqData)
            : AppRequestBuilder<EnterRoomReqData, EnterRoomRespBody> {
        return roomRepository.enterRoom(enterRoomReqData)
            .withOnSuccessSaveDataCallback {
                ApplicationData.room_token = it.roomInfo.roomToken
                ApplicationData.room_title = it.roomInfo.title
                ApplicationData.roomSettings = it.roomInfo.roomSettings
                roomLiveData.postValue(it.roomInfo)
            }
    }

    fun leaveRoom()
            : AppRequestBuilder<LeaveRoomReqData, LeaveRoomRespBody> {
        return roomRepository.leaveRoom()
    }

    fun usersInRoom()
            : AppRequestBuilder<UsersInRoomReqData, UsersInRoomRespBody> {
        return roomRepository.usersInRoom()
            .withOnSuccessSaveDataCallback {
                usersInRoomLiveData.postValue(it.users)
            }
    }

    fun updateRoomInfo()
            : AppRequestBuilder<RoomInfoReqData, RoomInfo> {
        return roomRepository.roomInfo()
            .withOnSuccessSaveDataCallback {
                ApplicationData.room_title = it.title
                ApplicationData.room_token = it.roomToken
                ApplicationData.roomSettings = it.roomSettings
                roomLiveData.postValue(it)
            }
    }

    fun updateFullRoomInfo()
            : AppRequestBuilder<RoomInfoReqData, FullRoomInfoRespBody> {
        return roomRepository.fullRoomInfo()
            .withOnSuccessSaveDataCallback {
                ApplicationData.room_token = it.roomInfo.roomToken
                ApplicationData.room_title = it.roomInfo.title
                ApplicationData.roomSettings = it.roomInfo.roomSettings
                if (it.playlists.isNotEmpty())
                    ApplicationData.playlist_title = it.playlists[0].title
                roomLiveData.postValue(it.roomInfo)
                usersInRoomLiveData.postValue(it.usersInRoom)

            }
    }

    fun updatePlaylistsInRoom()
            : AppRequestBuilder<GetPlaylistsReqData, GetPlaylistsRespBody> {
        return roomRepository.playlistsInRoom()
            .withOnSuccessSaveDataCallback {
                playlistsInRoomLiveData.postValue(it.playlists)
                if (it.playlists.isNotEmpty())
                    ApplicationData.playlist_title = it.playlists[0].title
            }
    }

}