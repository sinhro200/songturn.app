package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.model.RoomInfo
import com.sinhro.songturn.rest.model.RoomSettings
import com.sinhro.songturn.rest.request_response.*

class RoomRepositorySimple(
    private val roomTokenGetter: (() -> String) = ApplicationData.room_token_ref,
    accessToken: String = ApplicationData.access_token
) : RoomRepository(accessToken) {
    private val roomToken: String
        get() = roomTokenGetter.invoke()

    fun createRoom(
        title: String, roomSettings: RoomSettings
    ): AppRequestBuilder<CreateRoomReqData, CreateRoomRespBody> {
        return super.createRoom(CreateRoomReqData(title, roomSettings))
    }

    fun enterRoom(
        inviteCode: String
    ): AppRequestBuilder<EnterRoomReqData, EnterRoomRespBody> {
        return super.enterRoom(EnterRoomReqData(inviteCode))
    }

    fun leaveRoom(): AppRequestBuilder<LeaveRoomReqData, LeaveRoomRespBody> {
        return super.leaveRoom(LeaveRoomReqData(roomToken))
    }

    fun usersInRoom(): AppRequestBuilder<UsersInRoomReqData, UsersInRoomRespBody> {
        return super.usersInRoom(UsersInRoomReqData(roomToken))
    }

    fun roomInfo(): AppRequestBuilder<RoomInfoReqData, RoomInfo> {
        return super.roomInfo(RoomInfoReqData(roomToken))
    }

    fun fullRoomInfo(): AppRequestBuilder<RoomInfoReqData, FullRoomInfoRespBody> {
        return super.fullRoomInfo(RoomInfoReqData(roomToken))
    }

    fun playlistsInRoom(): AppRequestBuilder<GetPlaylistsReqData, GetPlaylistsRespBody> {
        return super.playlistsInRoom(GetPlaylistsReqData(roomToken))
    }
}