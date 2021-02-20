package com.sinhro.songturn.app.api.services

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import com.sinhro.songturn.rest.model.RoomInfo
import com.sinhro.songturn.rest.request_response.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class RoomService(bearerToken: String) {
    interface RoomAPI {
        @POST("/room/create")
        fun createRoom(
            @Body createRoomReqData: CreateRoomReqData
        ): Observable<Response<CreateRoomRespBody>>

        @POST("/room/enter")
        fun enterRoom(
            @Body enterRoomReqData: EnterRoomReqData
        ): Observable<Response<EnterRoomRespBody>>

        @POST("/room/leave")
        fun leaveRoom(
            @Body leaveRoomReqData: LeaveRoomReqData
        ): Observable<Response<LeaveRoomRespBody>>

        @POST("/room/users")
        fun usersInRoom(
            @Body usersInRoomReqData: UsersInRoomReqData
        ): Observable<Response<UsersInRoomRespBody>>

        @POST("/room/info")
        fun roomInfo(
            @Body roomInfoReqData: RoomInfoReqData
        ): Observable<Response<RoomInfo>>

        @POST("/room/fullroominfo")
        fun fullRoomInfo(
            @Body fullRoomInfoReqData: RoomInfoReqData
        ): Observable<Response<FullRoomInfoRespBody>>

        @POST("/room/playlists")
        fun getAllRoomPlaylists(
            @Body getPlaylistsReqData: GetPlaylistsReqData
        ): Observable<Response<GetPlaylistsRespBody>>
    }

    private val roomApi =
        AppRetrofitProvider.AuthorizedInstance(bearerToken)
            .create(RoomAPI::class.java)

    val createRoom = AppNetService.Builder<CreateRoomReqData, CreateRoomRespBody>()
        .observableFrom { roomApi.createRoom(it) }
        .build()

    val enterRoom = AppNetService.Builder<EnterRoomReqData, EnterRoomRespBody>()
        .observableFrom { roomApi.enterRoom(it) }
        .build()

    val leaveRoom = AppNetService.Builder<LeaveRoomReqData, LeaveRoomRespBody>()
        .observableFrom { roomApi.leaveRoom(it) }
        .build()

    val usersInRoom = AppNetService.Builder<UsersInRoomReqData, UsersInRoomRespBody>()
        .observableFrom { roomApi.usersInRoom(it) }
        .build()

    val roomInfo = AppNetService.Builder<RoomInfoReqData, RoomInfo>()
        .observableFrom { roomApi.roomInfo(it) }
        .build()

    val fullRoomInfo = AppNetService.Builder<RoomInfoReqData, FullRoomInfoRespBody>()
        .observableFrom { roomApi.fullRoomInfo(it) }
        .build()

    val getPlaylistsInRoom = AppNetService.Builder<GetPlaylistsReqData, GetPlaylistsRespBody>()
        .observableFrom { roomApi.getAllRoomPlaylists(it) }
        .build()
}