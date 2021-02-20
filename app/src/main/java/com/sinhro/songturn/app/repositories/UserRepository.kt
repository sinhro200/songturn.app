package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.api.services.UserService
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import okhttp3.ResponseBody

class UserRepository(accessToken: String) {

    private val us = UserService(accessToken)

    fun currentUserData() =
        AppRequestBuilder(us.getUser, null)
            .withErrorCollectorViewModel()


    fun userDataById(userId: Int) =
        AppRequestBuilder(us.getUserById, userId)
            .withErrorCollectorViewModel()


    fun changeUserData(
        newUserData: RegisterUserInfo
    ): AppRequestBuilder<RegisterUserInfo, FullUserInfo> {
        return AppRequestBuilder(us.changeUser, newUserData)
            .withErrorCollectorViewModel()
    }

    fun logoutUser(): AppRequestBuilder<Nothing?, ResponseBody> =
        AppRequestBuilder(us.logoutUser, null)

}
