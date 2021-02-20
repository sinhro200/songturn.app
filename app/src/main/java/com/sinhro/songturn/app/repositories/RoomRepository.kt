package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.api.services.RoomService
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.storage.StorageService
import com.sinhro.songturn.rest.model.RoomInfo
import com.sinhro.songturn.rest.request_response.*

open class RoomRepository(
    private val accessToken: String = ApplicationData.access_token
) {
    private val rs: RoomService by lazy {
        RoomService(accessToken)
    }

    open fun createRoom(createRoomReqData: CreateRoomReqData)
            : AppRequestBuilder<CreateRoomReqData, CreateRoomRespBody> {
        return AppRequestBuilder(rs.createRoom, createRoomReqData)
    }

    fun enterRoom(enterRoomReqData: EnterRoomReqData)
            : AppRequestBuilder<EnterRoomReqData, EnterRoomRespBody> {
        return AppRequestBuilder(rs.enterRoom, enterRoomReqData)
    }

    fun leaveRoom(leaveRoomReqData: LeaveRoomReqData)
            : AppRequestBuilder<LeaveRoomReqData, LeaveRoomRespBody> {
        return AppRequestBuilder(rs.leaveRoom, leaveRoomReqData)
    }

    fun usersInRoom(usersInRoomReqData: UsersInRoomReqData)
            : AppRequestBuilder<UsersInRoomReqData, UsersInRoomRespBody> {
        return AppRequestBuilder(rs.usersInRoom, usersInRoomReqData)
    }

    fun roomInfo(roomInfoReqData: RoomInfoReqData)
            : AppRequestBuilder<RoomInfoReqData, RoomInfo> {
        return AppRequestBuilder(rs.roomInfo, roomInfoReqData)
    }

    fun fullRoomInfo(roomInfoReqData: RoomInfoReqData)
            : AppRequestBuilder<RoomInfoReqData, FullRoomInfoRespBody> {
        return AppRequestBuilder(rs.fullRoomInfo, roomInfoReqData)
    }

    fun playlistsInRoom(playlistsReqData: GetPlaylistsReqData)
            : AppRequestBuilder<GetPlaylistsReqData, GetPlaylistsRespBody> {
        return AppRequestBuilder(rs.getPlaylistsInRoom, playlistsReqData)
    }
}