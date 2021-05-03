package com.sinhro.songturn.app.api

import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.ui.activities.AuthActivity
import com.sinhro.songturn.app.ui.fragments.EnterCreateRoomFragment
import com.sinhro.songturn.app.utils.replaceActivity
import com.sinhro.songturn.app.utils.replaceFragment
import com.sinhro.songturn.app.view_models.ErrorViewModel
import com.sinhro.songturn.app.view_models.RoomViewModel
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError

object RestErrorHandler {
    var activity: AppCompatActivity? = null

    fun handle(commonError: CommonError) {
        if (isUnauthorized(commonError.errorCode))
            activity?.replaceActivity(AuthActivity())
                ?: Log.e(
                    RestErrorHandler::class.java.simpleName,
                    "Must log out, but activity not set"
                )
        if (isOutOfRoom(commonError.errorCode))
            activity?.apply {
                viewModels<RoomViewModel>().apply {
                    if (isInitialized())
                        value.roomLiveData.postValue(null)
                }
                ApplicationData.playlistInfo = null
                replaceFragment(EnterCreateRoomFragment())
            } ?: Log.e(
                RestErrorHandler::class.java.simpleName,
                "Must leave the room, but activity not set"
            )
    }

    private fun isUnauthorized(errorCode: ErrorCodes): Boolean = errorCode in listOf(
        ErrorCodes.AUTHORIZATION_FAILED,
        ErrorCodes.AUTH_JWT_EXPIRED,
        ErrorCodes.AUTH_JWT_INVALID,
        ErrorCodes.AUTH_USER_NOT_FOUND,
        ErrorCodes.AUTH_USER_NOT_VERIFIED
    )

    private fun isOutOfRoom(errorCode: ErrorCodes): Boolean =
        (errorCode == ErrorCodes.ROOM_NOT_FOUND || errorCode == ErrorCodes.USER_NOT_IN_ROOM)
}