package com.sinhro.songturn.app.storage

import com.sinhro.musicord.storage.ROOM_TOKEN
import com.sinhro.musicord.storage.Storage
import com.sinhro.musicord.storage.USER_ACCESS_TOKEN

class StorageService constructor(private val storage: Storage) {

    companion object {
        fun fromGlobalStorageInstance(): StorageService? {
            return Storage.getGlobalInstance()?.let { StorageService(it) }
        }

        fun requireFromGlobalStorageInstance(): StorageService {
            return Storage.getGlobalInstance()?.let { StorageService(it) }
                ?: throw Exception("Storage global instance not found")
        }
    }

    var accessToken: String?
        get() = storage.get(USER_ACCESS_TOKEN)
        set(value) {storage.save(USER_ACCESS_TOKEN, value)}

    /*fun saveAccessToken(accToken: String) = storage.save(USER_ACCESS_TOKEN, accToken)

    fun getAccessToken(): String? = storage.get(USER_ACCESS_TOKEN)*/

//    fun requireAccessToken(): String =
//        getAccessToken() ?: throw Exception("storage dont contains access token")

    var roomToken: String?
        get() = storage.get(ROOM_TOKEN)
        set(value) {storage.save(ROOM_TOKEN, value)}

    /*fun saveRoomToken(roomToken: String) = storage.save(ROOM_TOKEN, roomToken)

    fun getRoomToken(): String? = storage.get(ROOM_TOKEN)*/

//    fun requireRoomToken(): String =
//        getRoomToken() ?: throw Exception("storage dont contains room token")

    /*fun saveMainRoomPlaylist(roomPlaylistTitle: String) =
        storage.save(ROOM_PLAYLIST, roomPlaylistTitle)

    fun getMainRoomPlaylist(): String? = storage.get(ROOM_PLAYLIST)

    fun requireRoomPlaylist(): String =
        getMainRoomPlaylist() ?: throw Exception("storage dont contains room playlist")*/

//    fun saveUser(userInfo: PublicUserInfo){
//        userInfo.apply {
//            id?.let { storage.saveLong(USER_ID,it)  }
//            nickname?.let { storage.save(USER_NICKNAME,it) }
//            email?.let { storage.save(USER_EMAIL,it) }
//        }
//    }
//
//    fun getUser(): UserInfo = UserInfo(
//        storage.getLong(USER_ID),
//        "",
//        storage.get(USER_EMAIL)?:"",
//        "",
//        "",
//        storage.get(USER_NICKNAME)?:""
//    )
//
//    fun clearUser(){
//        storage.clearValues(Arrays.asList(
//            USER_ID,USER_EMAIL, USER_NICKNAME, USER_ACCESS_TOKEN
//        ))
//    }
//
//    fun containsUser(): Boolean {
//        val acc = getAccessToken()
//        return acc != null && !acc.isBlank()
//    }
//
//    fun saveRoom(roomInfo: RoomInfo){
//        storage.apply {
//            roomInfo.title?.let { save(ROOM_TITLE, it) }
//            roomInfo.inviteCode?.let { save(ROOM_INVITE_CODE, it) }
//            roomInfo.roomToken?.let { save(ROOM_TOKEN, it) }
//        }
//    }
//
//    fun getRoom(): RoomInfo = RoomInfo(
//        storage.get(ROOM_TITLE)?:"",
//        storage.get(ROOM_INVITE_CODE)?:"",
//        storage.get(ROOM_TOKEN)?:""
//    )
//
//    fun clearRoom() {
//        storage.clearValues(
//            listOf(
//                ROOM_TITLE, ROOM_INVITE_CODE, ROOM_TOKEN
//            )
//        )
//    }
//
//    fun isInRoom(): Boolean {
//        val roomTok = getRoom().roomToken
//        return roomTok != null && !roomTok.isBlank()
//    }
}